package org.ashcrest.commands;

import org.ashcrest.db.EconomyManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Daily reward claim command.
 */
public class DailyCommand implements ICommand {
    private static final long DAILY_REWARD_AMOUNT = 250L;

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        long userId = event.getAuthor().getIdLong();
        String username = event.getAuthor().getEffectiveName();

        EconomyManager.getInstance().claimDailyReward(userId, username, DAILY_REWARD_AMOUNT).thenAccept(result -> {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎁 Ashcrest Daily Reward")
                    .setDescription(result.message())
                    .setColor(result.success() ? new Color(0x57F287) : new Color(0xED4245))
                    .setFooter("Ashcrest Aegis Cyber Operations", null)
                    .setTimestamp(Instant.now());

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long userId = event.getUser().getIdLong();
        String username = event.getUser().getEffectiveName();

        EconomyManager.getInstance().claimDailyReward(userId, username, DAILY_REWARD_AMOUNT).thenAccept(result -> {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎁 Ashcrest Daily Reward")
                    .setDescription(result.message())
                    .setColor(result.success() ? new Color(0x57F287) : new Color(0xED4245))
                    .setFooter("Ashcrest Aegis Cyber Operations", null)
                    .setTimestamp(Instant.now());

            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public String getName() { return "daily"; }

    @Override
    public String getDescription() { return "Claim your daily Ashcrest credits (250 🪙)"; }

    @Override
    public String getUsage() { return "!daily or /daily"; }
}
