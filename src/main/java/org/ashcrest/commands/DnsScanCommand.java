package org.ashcrest.commands;

import org.ashcrest.security.DnsScanService;
import org.ashcrest.security.DnsScanService.DnsResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.Executors;

/**
 * DNS Scanning & Email Security Audit Command.
 */
public class DnsScanCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!dnsscan <domain>`").queue();
            return;
        }

        String domain = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            DnsResult result = DnsScanService.scan(domain);
            event.getChannel().sendMessageEmbeds(buildDnsEmbed(result).build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("domain");
        if (opt == null) {
            event.reply("❌ Please specify a domain.").setEphemeral(true).queue();
            return;
        }

        String domain = opt.getAsString();
        event.deferReply().queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            DnsResult result = DnsScanService.scan(domain);
            event.getHook().sendMessageEmbeds(buildDnsEmbed(result).build()).queue();
        });
    }

    private EmbedBuilder buildDnsEmbed(DnsResult r) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌐 DNS Security Scan — " + r.domain())
                .setColor(new Color(0x00A8FC))
                .addField("📍 A Records", r.aRecords().isEmpty() ? "*None*" : String.join("\n", r.aRecords()), true)
                .addField("📍 AAAA Records", r.aaaaRecords().isEmpty() ? "*None*" : String.join("\n", r.aaaaRecords()), true)
                .addField("✉️ MX Records", r.mxRecords().isEmpty() ? "*None*" : String.join("\n", r.mxRecords()), false)
                .addField("🌐 NS Records", r.nsRecords().isEmpty() ? "*None*" : String.join("\n", r.nsRecords()), false)
                .addField("🛡️ SPF Policy (`v=spf1`)", r.hasSpf() ? "✅ Present" : "❌ Missing (Spoofing Risk)", true)
                .addField("🔒 DMARC Policy (`_dmarc`)", r.hasDmarc() ? "✅ Present" : "❌ Missing (Spoofing Risk)", true)
                .addField("🔐 DNSSEC (`DNSKEY`)", r.hasDnssec() ? "✅ Enabled" : "⚪ Disabled / Unsigned", true)
                .setFooter("AshcrestHQ Aegis Security Reconnaissance", null)
                .setTimestamp(Instant.now());

        return embed;
    }

    @Override
    public String getName() { return "dnsscan"; }

    @Override
    public String getDescription() { return "Perform DNS record lookup and email security policy audit (SPF, DMARC, DNSSEC)"; }

    @Override
    public String getUsage() { return "!dnsscan <domain> or /dnsscan domain:<string>"; }
}
