package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.awt.Color;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Passive DNSBL Blacklist Lookup Command (Spamhaus).
 */
public class BlacklistCommand implements ICommand {
    private static final List<String> BLACKLIST_ZONES = List.of(
            "zen.spamhaus.org",
            "bl.spamcop.net",
            "b.barracudacentral.org"
    );

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!blacklist <ip_or_domain>`").queue();
            return;
        }

        String input = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = checkBlacklist(input);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("target");
        if (opt == null) {
            event.reply("❌ Please provide an IP or domain.").setEphemeral(true).queue();
            return;
        }

        String input = opt.getAsString();
        event.deferReply().queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = checkBlacklist(input);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder checkBlacklist(String rawInput) {
        String target = rawInput.trim().toLowerCase().replaceAll("^https?://", "").replaceAll("/.*$", "");
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛡️ Passive DNSBL Blacklist Lookup — " + target)
                .setTimestamp(Instant.now());

        try {
            String ip = target;
            if (!target.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                InetAddress addr = InetAddress.getByName(target);
                ip = addr.getHostAddress();
                embed.addField("🌐 Resolved IP", "`" + ip + "`", false);
            }

            String[] octets = ip.split("\\.");
            String reversedIp = octets[3] + "." + octets[2] + "." + octets[1] + "." + octets[0];

            List<String> listings = new ArrayList<>();
            for (String zone : BLACKLIST_ZONES) {
                String queryName = reversedIp + "." + zone;
                try {
                    Lookup lookup = new Lookup(queryName, Type.A);
                    Record[] records = lookup.run();
                    if (records != null && records.length > 0) {
                        listings.add(zone);
                    }
                } catch (Exception ignored) {}
            }

            if (listings.isEmpty()) {
                embed.setColor(Color.GREEN)
                        .addField("✅ Clean Status", "Target IP (`" + ip + "`) is NOT listed on any monitored DNSBL zones.", false);
            } else {
                embed.setColor(Color.ORANGE)
                        .addField("🚨 Listed Zones", String.join("\n• ", listings), false);
            }

            embed.addField("ℹ️ Disclaimer", "DNSBL listings represent passive reputation records (e.g. shared IPs, dynamic ranges, or expired flags) and do NOT constitute proof of malicious intent.", false);
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Resolution Error", "Unable to resolve target or execute DNSBL query: " + e.getMessage(), false);
        }

        embed.setFooter("AshcrestHQ Aegis Passive DNSBL Inspection", null);
        return embed;
    }

    @Override
    public String getName() { return "blacklist"; }

    @Override
    public String getDescription() { return "Perform passive DNSBL blacklist lookup against Spamhaus & public RBL zones"; }

    @Override
    public String getUsage() { return "!blacklist <ip|domain> or /blacklist target:<string>"; }
}
