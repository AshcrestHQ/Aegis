package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.Executors;

/**
 * HTTP Security Headers Audit Command.
 */
public class HeadersCommand implements ICommand {
    private static final OkHttpClient client = new OkHttpClient();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!headers <url>`").queue();
            return;
        }

        String url = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = auditHeaders(url);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("url");
        if (opt == null) {
            event.reply("❌ Please provide a URL.").setEphemeral(true).queue();
            return;
        }

        String url = opt.getAsString();
        event.deferReply().queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = auditHeaders(url);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder auditHeaders(String rawUrl) {
        String targetUrl = rawUrl.startsWith("http://") || rawUrl.startsWith("https://") ? rawUrl : "https://" + rawUrl;
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛡️ HTTP Security Headers Audit — " + targetUrl)
                .setTimestamp(Instant.now());

        try {
            Request request = new Request.Builder().url(targetUrl).header("User-Agent", "AshcrestAegis/3.0").build();
            try (Response response = client.newCall(request).execute()) {
                String csp = response.header("Content-Security-Policy");
                String hsts = response.header("Strict-Transport-Security");
                String xfo = response.header("X-Frame-Options");
                String xcto = response.header("X-Content-Type-Options");
                String rp = response.header("Referrer-Policy");

                int score = 0;
                if (csp != null) score++;
                if (hsts != null) score++;
                if (xfo != null) score++;
                if (xcto != null) score++;
                if (rp != null) score++;

                Color color = score >= 4 ? Color.GREEN : (score >= 2 ? Color.ORANGE : Color.RED);
                embed.setColor(color);
                embed.addField("📊 Security Grade", score + " / 5 Security Headers Present", false);

                embed.addField("🔒 Content-Security-Policy", csp != null ? "✅ `" + truncate(csp, 100) + "`" : "❌ Missing", false);
                embed.addField("🛡️ Strict-Transport-Security (HSTS)", hsts != null ? "✅ `" + hsts + "`" : "❌ Missing", false);
                embed.addField("🖼️ X-Frame-Options", xfo != null ? "✅ `" + xfo + "`" : "❌ Missing", true);
                embed.addField("📄 X-Content-Type-Options", xcto != null ? "✅ `" + xcto + "`" : "❌ Missing", true);
                embed.addField("🔗 Referrer-Policy", rp != null ? "✅ `" + rp + "`" : "❌ Missing", true);
                embed.setFooter("AshcrestHQ Aegis Security Audit", null);
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to connect or fetch headers: " + e.getMessage(), false);
        }

        return embed;
    }

    private String truncate(String text, int max) {
        return text.length() > max ? text.substring(0, max - 3) + "..." : text;
    }

    @Override
    public String getName() { return "headers"; }

    @Override
    public String getDescription() { return "Audit HTTP security response headers (CSP, HSTS, X-Frame-Options)"; }

    @Override
    public String getUsage() { return "!headers <url> or /headers url:<string>"; }
}
