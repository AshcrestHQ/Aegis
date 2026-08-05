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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Google Safe Browsing Phishing & Malware Check Command.
 */
public class PhishCheckCommand implements ICommand {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!phishcheck <url>`").queue();
            return;
        }

        String url = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = checkPhishing(url);
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
            EmbedBuilder embed = checkPhishing(url);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder checkPhishing(String rawUrl) {
        String apiKey = Config.GOOGLE_SAFE_BROWSING_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return new EmbedBuilder()
                    .setTitle("⚠️ Google Safe Browsing Check Not Configured")
                    .setColor(Color.ORANGE)
                    .setDescription("Google Safe Browsing check is not configured — `GOOGLE_SAFE_BROWSING_API_KEY` missing in environment configuration.")
                    .setTimestamp(Instant.now());
        }

        String targetUrl = rawUrl.startsWith("http://") || rawUrl.startsWith("https://") ? rawUrl : "http://" + rawUrl;
        String apiEndpoint = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛡️ Google Safe Browsing Threat Check")
                .addField("🔗 Target URL", "`" + targetUrl + "`", false)
                .setTimestamp(Instant.now());

        try {
            Map<String, Object> bodyMap = Map.of(
                    "client", Map.of("clientId", "AshcrestAegis", "clientVersion", "3.0"),
                    "threatInfo", Map.of(
                            "threatTypes", List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                            "platformTypes", List.of("ANY_PLATFORM"),
                            "threatEntryTypes", List.of("URL"),
                            "threatEntries", List.of(Map.of("url", targetUrl))
                    )
            );

            String jsonPayload = mapper.writeValueAsString(bodyMap);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(apiEndpoint).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = mapper.readTree(response.body().string());
                    JsonNode matches = root.path("matches");

                    if (matches.isArray() && !matches.isEmpty()) {
                        String threatType = matches.get(0).path("threatType").asText("SECURITY_THREAT");
                        embed.setColor(Color.RED)
                                .addField("🚨 Threat Detected!", "**WARNING**: URL listed as `" + threatType + "` on Google Safe Browsing lists!", false);
                    } else {
                        embed.setColor(Color.GREEN)
                                .addField("✅ Clear Status", "URL is clean! No threats or phishing signatures detected on Google Safe Browsing databases.", false);
                    }
                } else {
                    embed.setColor(Color.ORANGE)
                            .addField("⚠️ Safe Browsing Response", "HTTP " + response.code() + " — Unable to complete threat query.", false);
                }
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to query Google Safe Browsing API: " + e.getMessage(), false);
        }

        embed.setFooter("AshcrestHQ Aegis Security Intelligence", null);
        return embed;
    }

    @Override
    public String getName() { return "phishcheck"; }

    @Override
    public String getDescription() { return "Check a URL against Google Safe Browsing API for phishing, malware, and social engineering threats"; }

    @Override
    public String getUsage() { return "!phishcheck <url> or /phishcheck url:<string>"; }
}
