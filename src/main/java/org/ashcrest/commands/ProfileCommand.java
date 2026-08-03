package org.ashcrest.commands;

import org.ashcrest.db.EconomyManager;
import org.ashcrest.db.EconomyManager.UserProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Displays player hacking rank, level, XP, credits, and hack statistics.
 */
public class ProfileCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        long userId = event.getAuthor().getIdLong();
        String username = event.getAuthor().getEffectiveName();

        EconomyManager.getInstance().getUserProfile(userId, username).thenAccept(profile -> {
            EmbedBuilder embed = buildProfileEmbed(profile, event.getAuthor().getEffectiveAvatarUrl());
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long userId = event.getUser().getIdLong();
        String username = event.getUser().getEffectiveName();

        EconomyManager.getInstance().getUserProfile(userId, username).thenAccept(profile -> {
            EmbedBuilder embed = buildProfileEmbed(profile, event.getUser().getEffectiveAvatarUrl());
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder buildProfileEmbed(UserProfile profile, String avatarUrl) {
        long reqXp = profile.getXpForNextLevel();
        int progressPercent = (int) Math.min(100, (double) profile.xp() / reqXp * 100);
        String progressBar = "🟦".repeat(Math.max(0, progressPercent / 10)) + "⬜".repeat(Math.max(0, 10 - (progressPercent / 10)));

        return new EmbedBuilder()
                .setTitle("👤 Agent Profile — " + profile.username())
                .setThumbnail(avatarUrl)
                .setColor(new Color(0x00FFC8))
                .addField("🎖️ Hacker Rank", profile.getRankName(), true)
                .addField("📊 Security Level", "Level **" + profile.level() + "**", true)
                .addField("🪙 Ashcrest Vault", "**" + profile.credits() + " 🪙** Credits", true)
                .addField("⚡ XP Progress", "`" + profile.xp() + " / " + reqXp + " XP`\n" + progressBar + " (" + progressPercent + "%)", false)
                .addField("🎯 Successful Breaches", "**" + profile.successfulHacks() + "** Hacks", true)
                .addField("🚨 Failed Attempts", "**" + profile.failedHacks() + "** Busted", true)
                .addField("📈 Win Rate", String.format("%.1f%%", profile.getWinRate()), true)
                .setFooter("Ashcrest Aegis Cyber Operations • Level up by executing /hack", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "profile"; }

    @Override
    public String getDescription() { return "View your Ashcrest hacker rank, level, XP, credits, and hack stats"; }

    @Override
    public String getUsage() { return "!profile or /profile"; }
}
