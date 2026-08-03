package org.ashcrest.commands;

import org.ashcrest.db.EconomyManager;
import org.ashcrest.db.EconomyManager.UserProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * Top Cyber Security Hacker Leaderboard Command.
 */
public class LeaderboardCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        EconomyManager.getInstance().getLeaderboard(10).thenAccept(leaderboard -> {
            EmbedBuilder embed = buildLeaderboardEmbed(leaderboard);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        EconomyManager.getInstance().getLeaderboard(10).thenAccept(leaderboard -> {
            EmbedBuilder embed = buildLeaderboardEmbed(leaderboard);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder buildLeaderboardEmbed(List<UserProfile> leaderboard) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Ashcrest Aegis — Global Cyber Leaderboard")
                .setDescription("Top hackers and security specialists ranked by level, XP, and credits balance.")
                .setColor(new Color(0xFFD700))
                .setFooter("Ashcrest Aegis Cyber Operations • Play /hack to climb rankings", null)
                .setTimestamp(Instant.now());

        if (leaderboard.isEmpty()) {
            embed.addField("Empty Leaderboard", "No hackers found yet. Execute `/hack` to be the first on the board!", false);
            return embed;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leaderboard.size(); i++) {
            UserProfile p = leaderboard.get(i);
            String medal = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> "`#" + (i + 1) + "`";
            };

            sb.append(medal).append(" **").append(p.username()).append("** — ")
                    .append(p.getRankName()).append("\n")
                    .append("   ↳ Level **").append(p.level()).append("** | **")
                    .append(p.credits()).append(" 🪙** | Breaches: `").append(p.successfulHacks()).append("`\n\n");
        }

        embed.addField("Top 10 Hacker Agents", sb.toString(), false);
        return embed;
    }

    @Override
    public String getName() { return "leaderboard"; }

    @Override
    public String getDescription() { return "View the top Ashcrest hacker leaderboard ranked by level and credits"; }

    @Override
    public String getUsage() { return "!leaderboard or /leaderboard"; }
}
