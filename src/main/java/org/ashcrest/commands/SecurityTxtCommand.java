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
 * RFC 9116 security.txt Inspection Command.
 */
public class SecurityTxtCommand implements ICommand {
    private static final OkHttpClient client = new OkHttpClient();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!securitytxt <domain>`").queue();
            return;
        }

        String domain = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = checkSecurityTxt(domain);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("domain");
        if (opt == null) {
            event.reply("❌ Please provide a domain.").setEphemeral(true).queue();
            return;
        }

        String domain = opt.getAsString();
        event.deferReply().queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = checkSecurityTxt(domain);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder checkSecurityTxt(String rawDomain) {
        String cleanDomain = rawDomain.toLowerCase().replaceAll("^https?://", "").replaceAll("/.*$", "").trim();
        String url = "https://" + cleanDomain + "/.well-known/security.txt";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📄 RFC 9116 security.txt Audit — " + cleanDomain)
                .setTimestamp(Instant.now());

        try {
            Request request = new Request.Builder().url(url).header("User-Agent", "AshcrestAegis/3.0").build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String content = response.body().string();
                    embed.setColor(Color.GREEN)
                            .addField("✅ Status", "Found valid `security.txt` at " + url, false)
                            .addField("📜 Policy Content Preview", "```text\n" + (content.length() > 1000 ? content.substring(0, 997) + "..." : content) + "\n```", false);
                } else {
                    embed.setColor(Color.ORANGE)
                            .addField("❌ Status", "HTTP " + response.code() + " — No `security.txt` found at " + url, false);
                }
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to fetch security.txt: " + e.getMessage(), false);
        }

        embed.setFooter("AshcrestHQ Aegis Security Reconnaissance", null);
        return embed;
    }

    @Override
    public String getName() { return "securitytxt"; }

    @Override
    public String getDescription() { return "Check domain RFC 9116 vulnerability disclosure policy (/.well-known/security.txt)"; }

    @Override
    public String getUsage() { return "!securitytxt <domain> or /securitytxt domain:<string>"; }
}
