package org.ashcrest.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Passive IP Geolocation & ASN Lookup Command.
 */
public class IpLookupCommand implements ICommand {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!iplookup <ip_address>`").queue();
            return;
        }

        String ip = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = lookupIp(ip);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("ip");
        if (opt == null) {
            event.reply("❌ Please provide an IP address.").setEphemeral(true).queue();
            return;
        }

        String ip = opt.getAsString();
        event.deferReply().queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = lookupIp(ip);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder lookupIp(String ip) {
        String cleanIp = ip.trim();
        String url = "http://ip-api.com/json/" + cleanIp + "?fields=status,message,country,regionName,city,zip,lat,lon,timezone,isp,org,as,query";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📍 Passive IP Geolocation — " + cleanIp)
                .setTimestamp(Instant.now());

        try {
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = mapper.readTree(response.body().string());
                    if ("success".equalsIgnoreCase(root.path("status").asText())) {
                        embed.setColor(new Color(0x00FFC8))
                                .addField("🌍 Location", root.path("city").asText() + ", " + root.path("regionName").asText() + ", " + root.path("country").asText(), false)
                                .addField("🏢 ISP / Host", root.path("isp").asText() + " (" + root.path("org").asText() + ")", false)
                                .addField("🌐 ASN", root.path("as").asText(), false)
                                .addField("🗺️ Coordinates", root.path("lat").asText() + ", " + root.path("lon").asText(), true)
                                .addField("⏰ Timezone", root.path("timezone").asText(), true);
                    } else {
                        embed.setColor(Color.RED)
                                .addField("❌ Lookup Failed", root.path("message").asText("Invalid IP address"), false);
                    }
                }
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to query IP registry: " + e.getMessage(), false);
        }

        embed.setFooter("AshcrestHQ Aegis Passive Reconnaissance", null);
        return embed;
    }

    @Override
    public String getName() { return "iplookup"; }

    @Override
    public String getDescription() { return "Perform passive IP geolocation and ASN provider lookup"; }

    @Override
    public String getUsage() { return "!iplookup <ip> or /iplookup ip:<string>"; }
}
