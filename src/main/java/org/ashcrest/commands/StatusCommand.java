package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.time.Duration;

/**
 * Status and health diagnostics command for Aegis.
 */
public class StatusCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildStatusEmbed(event.getJDA().getGatewayPing()).build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildStatusEmbed(event.getJDA().getGatewayPing()).build()).queue();
    }

    private EmbedBuilder buildStatusEmbed(long ping) {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration duration = Duration.ofMillis(uptime);

        return new EmbedBuilder()
                .setTitle("🛡️ Aegis System Status")
                .setColor(Color.CYAN)
                .addField("🌐 Gateway Ping", ping + "ms", true)
                .addField("⏱️ Uptime", String.format("%dd %dh %dm", duration.toDays(), duration.toHoursPart(), duration.toMinutesPart()), true)
                .addField("☕ Java Version", System.getProperty("java.version"), true)
                .addField("⚡ Virtual Threads", "Enabled (Java 26)", true)
                .setFooter("AshcrestHQ Aegis | Security Operations")
                .setTimestamp(java.time.Instant.now());
    }

    @Override
    public String getName() { return "status"; }

    @Override
    public String getDescription() { return "Check Aegis bot system health, ping, and uptime"; }

    @Override
    public String getUsage() { return "!status"; }
}
