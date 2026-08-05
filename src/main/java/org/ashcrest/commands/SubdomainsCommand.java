package org.ashcrest.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.*;

import java.awt.Color;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Passive Subdomain Enumeration via Certificate Transparency Logs (crt.sh).
 */
public class SubdomainsCommand implements ICommand {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!subdomains <domain>`").queue();
            return;
        }

        String domain = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = findSubdomains(domain);
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
            EmbedBuilder embed = findSubdomains(domain);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder findSubdomains(String rawDomain) {
        String cleanDomain = rawDomain.toLowerCase().replaceAll("^https?://", "").replaceAll("/.*$", "").trim();
        String url = "https://crt.sh/?q=" + cleanDomain + "&output=json";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌐 Certificate Transparency Subdomains — " + cleanDomain)
                .setTimestamp(Instant.now());

        try {
            Request request = new Request.Builder().url(url).header("User-Agent", "AshcrestAegis/3.0").build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = mapper.readTree(response.body().string());
                    Set<String> subdomains = new TreeSet<>();

                    if (root.isArray()) {
                        for (JsonNode entry : root) {
                            String nameValue = entry.path("name_value").asText("");
                            String[] names = nameValue.split("\n");
                            for (String name : names) {
                                String trimmed = name.trim().toLowerCase();
                                if (!trimmed.isEmpty() && !trimmed.contains("*") && trimmed.endsWith(cleanDomain)) {
                                    subdomains.add(trimmed);
                                }
                            }
                        }
                    }

                    if (subdomains.isEmpty()) {
                        embed.setColor(Color.GREEN)
                                .addField("✅ Status", "No subdomains found in public Certificate Transparency logs for `" + cleanDomain + "`.", false);
                    } else {
                        embed.setColor(new Color(0x00A8FC))
                                .addField("📊 Total Subdomains Found", "**" + subdomains.size() + "** unique subdomains logged", false);

                        StringBuilder sb = new StringBuilder();
                        int count = 0;
                        for (String sub : subdomains) {
                            if (count++ >= 25) {
                                sb.append("\n• *...and ").append(subdomains.size() - 25).append(" more logged subdomains*");
                                break;
                            }
                            sb.append("• `").append(sub).append("`\n");
                        }
                        embed.addField("📜 Subdomain Samples (Top 25)", sb.toString(), false);
                        embed.addField("ℹ️ Transparency Disclaimer", "Data is collected passively from public SSL/TLS Certificate Transparency logs — target infrastructure was NOT actively scanned.", false);
                    }
                } else {
                    embed.setColor(Color.ORANGE)
                            .addField("⚠️ crt.sh Response", "HTTP " + response.code() + " — Unable to query Certificate Transparency logs.", false);
                }
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to fetch Certificate Transparency logs: " + e.getMessage(), false);
        }

        embed.setFooter("AshcrestHQ Aegis Passive Reconnaissance", null);
        return embed;
    }

    @Override
    public String getName() { return "subdomains"; }

    @Override
    public String getDescription() { return "Enumerate subdomains passively via public Certificate Transparency logs (crt.sh)"; }

    @Override
    public String getUsage() { return "!subdomains <domain> or /subdomains domain:<string>"; }
}
