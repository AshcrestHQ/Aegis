package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.ashcrest.core.AegisBot;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * Status and health diagnostics command for Aegis.
 */
public class StatusCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildStatusEmbed(event.getJDA(), event.getJDA().getGatewayPing()).build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildStatusEmbed(event.getJDA(), event.getJDA().getGatewayPing()).build()).queue();
    }

    private EmbedBuilder buildStatusEmbed(JDA jda, long ping) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration duration = Duration.ofMillis(uptime);

        long serverCount = jda != null ? jda.getGuilds().size() : 0;
        long totalMembers = jda != null ? jda.getGuilds().stream().mapToLong(net.dv8tion.jda.api.entities.Guild::getMemberCount).sum() : 0;
        long commandCount = (AegisBot.getInstance() != null && AegisBot.getInstance().getCommands() != null)
                ? AegisBot.getInstance().getCommands().values().stream().map(ICommand::getName).distinct().count()
                : 41;

        NumberFormat nf = NumberFormat.getInstance(Locale.US);

        return new EmbedBuilder()
                .setTitle("🛡️ Aegis System Status & Health")
                .setColor(Color.CYAN)
                .addField("🌐 Gateway Ping", ping + "ms", true)
                .addField("⏱️ Uptime", String.format("%dd %dh %dm", duration.toDays(), duration.toHoursPart(), duration.toMinutesPart()), true)
                .addField("☕ Java Version", System.getProperty("java.version") + " (Virtual Threads)", true)
                .addField("⚡ Total Commands", commandCount + " registered", true)
                .addField("👥 Members Using Me", nf.format(totalMembers) + " members", true)
                .addField("🖥️ Servers I am in", serverCount + " servers", true)
                .setFooter("AshcrestHQ Aegis | Security Operations Engine", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "status"; }

    @Override
    public String getDescription() { return "Check Aegis bot health, ping, uptime, total commands, server count, and member metrics"; }

    @Override
    public String getUsage() { return "!status or /status"; }
}
