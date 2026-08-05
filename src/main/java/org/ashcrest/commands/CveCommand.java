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
import java.util.regex.Pattern;

/**
 * NVD Vulnerability Database (CVE) Lookup Command.
 */
public class CveCommand implements ICommand {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Pattern CVE_PATTERN = Pattern.compile("^CVE-\\d{4}-\\d{4,}$", Pattern.CASE_INSENSITIVE);

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!cve CVE-YYYY-NNNN`").queue();
            return;
        }

        String id = args[0].toUpperCase().trim();
        if (!CVE_PATTERN.matcher(id).matches()) {
            event.getChannel().sendMessage("❌ Invalid CVE ID format! Must match `CVE-YYYY-NNNN+` (e.g. `CVE-2024-3094`).").queue();
            return;
        }

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = lookupCve(id);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("id");
        if (opt == null) {
            event.reply("❌ Please provide a CVE ID.").setEphemeral(true).queue();
            return;
        }

        String id = opt.getAsString().toUpperCase().trim();
        if (!CVE_PATTERN.matcher(id).matches()) {
            event.reply("❌ Invalid CVE ID format! Must match `CVE-YYYY-NNNN+` (e.g. `CVE-2024-3094`).").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = lookupCve(id);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder lookupCve(String cveId) {
        String url = "https://services.nvd.nist.gov/rest/json/cves/2.0?cveId=" + cveId;
        String nvdLink = "https://nvd.nist.gov/vuln/detail/" + cveId;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚨 NVD Vulnerability Report — " + cveId, nvdLink)
                .setTimestamp(Instant.now());

        try {
            Request.Builder reqBuilder = new Request.Builder().url(url).header("User-Agent", "AshcrestAegis/3.0");
            if (Config.NVD_API_KEY != null && !Config.NVD_API_KEY.trim().isEmpty()) {
                reqBuilder.header("apiKey", Config.NVD_API_KEY);
            }

            try (Response response = client.newCall(reqBuilder.build()).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = mapper.readTree(response.body().string());
                    JsonNode vulns = root.path("vulnerabilities");

                    if (vulns.isArray() && !vulns.isEmpty()) {
                        JsonNode cveItem = vulns.get(0).path("cve");

                        // Extract description
                        String description = "No description available.";
                        JsonNode descriptions = cveItem.path("descriptions");
                        if (descriptions.isArray() && !descriptions.isEmpty()) {
                            for (JsonNode desc : descriptions) {
                                if ("en".equalsIgnoreCase(desc.path("lang").asText())) {
                                    description = desc.path("value").asText();
                                    break;
                                }
                            }
                        }

                        // Extract CVSS score & severity
                        double cvssScore = 0.0;
                        String severity = "UNKNOWN";
                        JsonNode metrics = cveItem.path("metrics");

                        if (metrics.has("cvssMetricV31")) {
                            JsonNode v31 = metrics.path("cvssMetricV31").get(0).path("cvssData");
                            cvssScore = v31.path("baseScore").asDouble(0.0);
                            severity = v31.path("baseSeverity").asText("UNKNOWN");
                        } else if (metrics.has("cvssMetricV30")) {
                            JsonNode v30 = metrics.path("cvssMetricV30").get(0).path("cvssData");
                            cvssScore = v30.path("baseScore").asDouble(0.0);
                            severity = v30.path("baseSeverity").asText("UNKNOWN");
                        }

                        Color color = cvssScore >= 9.0 ? Color.RED : (cvssScore >= 7.0 ? new Color(0xFF6B00) : (cvssScore >= 4.0 ? Color.ORANGE : Color.GREEN));
                        embed.setColor(color)
                                .addField("📊 CVSS v3 Score", "**" + cvssScore + "** (" + severity + ")", true)
                                .addField("📄 Description", description.length() > 900 ? description.substring(0, 897) + "..." : description, false)
                                .addField("🔗 NVD Reference", "[View Full NVD Entry](" + nvdLink + ")", false);
                    } else {
                        embed.setColor(Color.ORANGE)
                                .addField("❓ Not Found", "No entry found in NVD database for `" + cveId + "`.", false);
                    }
                } else {
                    embed.setColor(Color.ORANGE)
                            .addField("⚠️ NVD Response", "HTTP " + response.code() + " — Unable to query NVD database.", false);
                }
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to query NVD API: " + e.getMessage(), false);
        }

        embed.setFooter("AshcrestHQ Aegis Vulnerability Intelligence", null);
        return embed;
    }

    @Override
    public String getName() { return "cve"; }

    @Override
    public String getDescription() { return "Lookup CVE vulnerability details, CVSS severity scores, and description from NVD"; }

    @Override
    public String getUsage() { return "!cve CVE-YYYY-NNNN or /cve id:<string>"; }
}
