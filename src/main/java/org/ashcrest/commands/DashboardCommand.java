package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.ashcrest.db.DatabaseManager;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * Server activity dashboard for Aegis.
 */
public class DashboardCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        DatabaseManager.getInstance().getRecentActivityStats(event.getGuild().getIdLong()).thenAccept(stats -> {
            event.getChannel().sendMessageEmbeds(buildDashboardEmbed(stats).build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        DatabaseManager.getInstance().getRecentActivityStats(event.getGuild().getIdLong()).thenAccept(stats -> {
            event.replyEmbeds(buildDashboardEmbed(stats).build()).queue();
        });
    }

    private EmbedBuilder buildDashboardEmbed(List<String> stats) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 Aegis Server Activity Dashboard (Last 24h)")
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now());

        if (stats.isEmpty()) {
            embed.setDescription("No significant activity recorded in the last 24 hours.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String stat : stats) {
                sb.append("• ").append(stat).append("\n");
            }
            embed.setDescription(sb.toString());
        }

        return embed;
    }

    @Override
    public String getName() { return "dashboard"; }

    @Override
    public String getDescription() { return "View server activity statistics from the audit log"; }

    @Override
    public String getUsage() { return "!dashboard or /dashboard"; }
}
