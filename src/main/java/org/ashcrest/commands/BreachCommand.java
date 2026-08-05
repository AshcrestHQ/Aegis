package org.ashcrest.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ashcrest.core.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.*;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.Executors;

/**
 * HaveIBeenPwned Breach Lookup Command.
 * Privacy Guarantee: Query email is processed ephemerally and NEVER logged.
 */
public class BreachCommand implements ICommand {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!breach <email>`").queue();
            return;
        }

        String email = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = checkBreach(email);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("email");
        if (opt == null) {
            event.reply("❌ Please provide an email address.").setEphemeral(true).queue();
            return;
        }

        String email = opt.getAsString();
        event.deferReply().setEphemeral(true).queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = checkBreach(email);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder checkBreach(String email) {
        String apiKey = Config.HIBP_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new EmbedBuilder()
                    .setTitle("⚠️ Breach Lookup Not Configured")
                    .setColor(Color.ORANGE)
                    .setDescription("Breach lookup is not configured — `HIBP_API_KEY` missing in environment configuration.")
                    .setTimestamp(Instant.now());
        }

        String cleanEmail = email.trim().toLowerCase();
        String url = "https://haveibeenpwned.com/api/v3/breachedaccount/" + cleanEmail + "?truncateResponse=false";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 Data Breach Audit")
                .setTimestamp(Instant.now());

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("hibp-api-key", apiKey)
                    .header("user-agent", "AshcrestAegis/3.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 404) {
                    embed.setColor(Color.GREEN)
                            .addField("✅ Safe Status", "No known data breaches found on record for this account.", false);
                } else if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = mapper.readTree(response.body().string());
                    embed.setColor(Color.RED)
                            .addField("🚨 Compromised Status", "Account detected in **" + root.size() + "** known data breach(es)!", false);

                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (JsonNode b : root) {
                        if (count++ >= 10) {
                            sb.append("• *...and ").append(root.size() - 10).append(" more breaches*");
                            break;
                        }
                        sb.append("• **").append(b.path("Name").asText()).append("** (").append(b.path("BreachDate").asText()).append(")\n");
                    }
                    embed.addField("🏢 Breached Entities", sb.toString(), false);
                } else {
                    embed.setColor(Color.ORANGE)
                            .addField("⚠️ API Response", "HTTP " + response.code() + " — Unable to complete breach lookup.", false);
                }
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to reach HaveIBeenPwned API: " + e.getMessage(), false);
        }

        embed.setFooter("AshcrestHQ Aegis Privacy Guarantee: Query email was NOT logged.", null);
        return embed;
    }

    @Override
    public String getName() { return "breach"; }

    @Override
    public String getDescription() { return "Check if an email address has been compromised in known data breaches (HaveIBeenPwned API)"; }

    @Override
    public String getUsage() { return "!breach <email> or /breach email:<string>"; }
}
