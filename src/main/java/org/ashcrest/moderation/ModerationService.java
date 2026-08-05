package org.ashcrest.moderation;

import org.ashcrest.db.GuildSettingsManager;
import org.ashcrest.db.ModerationManager;
import org.ashcrest.db.ModerationManager.ModCase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Service orchestrating moderation permissions, role hierarchy checks, logging, and auto-escalation.
 */
public class ModerationService {

    public static boolean canModerate(Member actor, Member target) {
        if (actor == null || target == null) return false;
        if (actor.getIdLong() == target.getIdLong()) return false;
        if (target.getUser().isBot()) return false;
        if (actor.isOwner()) return true;

        Member botMember = actor.getGuild().getSelfMember();
        if (!botMember.canInteract(target)) return false;

        return actor.canInteract(target);
    }

    public static CompletableFuture<ModCase> executeWarn(Guild guild, Member moderator, Member target, String reason) {
        long guildId = guild.getIdLong();
        long targetId = target.getIdLong();
        long modId = moderator.getIdLong();

        return ModerationManager.getInstance().createCase(guildId, targetId, modId, "WARN", reason)
                .thenCompose(modCase -> {
                    sendDirectMessage(target.getUser(), "⚠️ You received a warning in **" + guild.getName() + "**:\nReason: `" + reason + "`");
                    postModLogEmbed(guild, modCase, target.getUser(), moderator.getUser());

                    // Check for auto-escalation (3 active warnings = 1 hour auto-mute)
                    return ModerationManager.getInstance().getActiveWarningsCount(guildId, targetId)
                            .thenCompose(activeCount -> {
                                if (activeCount >= 3 && target.getGuild().getSelfMember().hasPermission(Permission.MODERATE_MEMBERS)) {
                                    return target.timeoutFor(Duration.ofHours(1))
                                            .reason("Auto-escalation: Reached " + activeCount + " active warnings")
                                            .submit()
                                            .thenCompose(v -> ModerationManager.getInstance().createCase(
                                                    guildId, targetId, guild.getSelfMember().getIdLong(), "MUTE", "Auto-escalation (Reached 3 active warnings)"
                                            ).thenAccept(autoCase -> postModLogEmbed(guild, autoCase, target.getUser(), guild.getSelfMember().getUser())))
                                            .thenApply(v -> modCase);
                                }
                                return CompletableFuture.completedFuture(modCase);
                            });
                });
    }

    public static CompletableFuture<ModCase> executeMute(Guild guild, Member moderator, Member target, Duration duration, String reason) {
        return target.timeoutFor(duration).reason(reason).submit()
                .thenCompose(v -> ModerationManager.getInstance().createCase(guild.getIdLong(), target.getIdLong(), moderator.getIdLong(), "MUTE", reason))
                .thenApply(modCase -> {
                    sendDirectMessage(target.getUser(), "🔇 You have been muted in **" + guild.getName() + "** for " + duration.toMinutes() + " minutes:\nReason: `" + reason + "`");
                    postModLogEmbed(guild, modCase, target.getUser(), moderator.getUser());
                    return modCase;
                });
    }

    public static CompletableFuture<Boolean> executeUnmute(Guild guild, Member moderator, Member target, String reason) {
        return target.removeTimeout().reason(reason).submit()
                .thenCompose(v -> ModerationManager.getInstance().createCase(guild.getIdLong(), target.getIdLong(), moderator.getIdLong(), "UNMUTE", reason))
                .thenApply(modCase -> {
                    postModLogEmbed(guild, modCase, target.getUser(), moderator.getUser());
                    return true;
                });
    }

    public static CompletableFuture<ModCase> executeKick(Guild guild, Member moderator, Member target, String reason) {
        sendDirectMessage(target.getUser(), "🚪 You have been kicked from **" + guild.getName() + "**:\nReason: `" + reason + "`");
        return target.kick().reason(reason).submit()
                .thenCompose(v -> ModerationManager.getInstance().createCase(guild.getIdLong(), target.getIdLong(), moderator.getIdLong(), "KICK", reason))
                .thenApply(modCase -> {
                    postModLogEmbed(guild, modCase, target.getUser(), moderator.getUser());
                    return modCase;
                });
    }

    public static CompletableFuture<ModCase> executeBan(Guild guild, Member moderator, User targetUser, String reason) {
        return guild.ban(targetUser, 0, java.util.concurrent.TimeUnit.DAYS).reason(reason).submit()
                .thenCompose(v -> ModerationManager.getInstance().createCase(guild.getIdLong(), targetUser.getIdLong(), moderator.getIdLong(), "BAN", reason))
                .thenApply(modCase -> {
                    postModLogEmbed(guild, modCase, targetUser, moderator.getUser());
                    return modCase;
                });
    }

    public static CompletableFuture<Boolean> executeUnban(Guild guild, Member moderator, User targetUser, String reason) {
        return guild.unban(targetUser).reason(reason).submit()
                .thenCompose(v -> ModerationManager.getInstance().createCase(guild.getIdLong(), targetUser.getIdLong(), moderator.getIdLong(), "UNBAN", reason))
                .thenApply(modCase -> {
                    postModLogEmbed(guild, modCase, targetUser, moderator.getUser());
                    return true;
                });
    }

    public static void postModLogEmbed(Guild guild, ModCase modCase, User target, User moderator) {
        GuildSettingsManager.getInstance().getSettings(guild.getIdLong()).thenAccept(settings -> {
            if (settings.modLogChannelId() <= 0) return;

            TextChannel logChannel = guild.getTextChannelById(settings.modLogChannelId());
            if (logChannel == null || !logChannel.canTalk()) return;

            Color color = switch (modCase.action()) {
                case "WARN" -> Color.YELLOW;
                case "MUTE" -> Color.ORANGE;
                case "UNMUTE" -> Color.GREEN;
                case "KICK" -> new Color(0xFF6B00);
                case "BAN" -> Color.RED;
                case "UNBAN" -> Color.CYAN;
                default -> Color.BLUE;
            };

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔨 Moderation Action — Case #" + modCase.id() + " (" + modCase.action() + ")")
                    .setColor(color)
                    .addField("👤 User", target.getAsTag() + " (`" + target.getId() + "`)", true)
                    .addField("🛡️ Moderator", moderator.getAsTag() + " (`" + moderator.getId() + "`)", true)
                    .addField("📄 Reason", modCase.reason(), false)
                    .setFooter("AshcrestHQ Aegis Moderation Engine", null)
                    .setTimestamp(Instant.now());

            logChannel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    private static void sendDirectMessage(User user, String message) {
        user.openPrivateChannel().queue(pc -> pc.sendMessage(message).queue(null, throwable -> {}), throwable -> {});
    }
}
