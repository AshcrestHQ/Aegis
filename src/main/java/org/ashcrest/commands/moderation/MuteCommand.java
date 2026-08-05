package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import org.ashcrest.moderation.ModerationService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.time.Duration;

/**
 * Moderation Mute (Timeout) Command.
 */
public class MuteCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Command can only be run in a server.").queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `Moderate Members` permission.").queue();
            return;
        }

        if (event.getMessage().getMentions().getMembers().isEmpty() || args.length < 2) {
            event.getChannel().sendMessage("❌ **Usage**: `!mute <@user> <duration_minutes> <reason>`").queue();
            return;
        }

        Member target = event.getMessage().getMentions().getMembers().get(0);
        long minutes = 10;
        try {
            minutes = Long.parseLong(args[1]);
        } catch (NumberFormatException ignored) {}

        String reason = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "No reason provided";

        if (!ModerationService.canModerate(mod, target)) {
            event.getChannel().sendMessage("❌ Cannot mute this user (role hierarchy restriction).").queue();
            return;
        }

        Duration duration = Duration.ofMinutes(Math.min(28 * 24 * 60, Math.max(1, minutes)));
        ModerationService.executeMute(event.getGuild(), mod, target, duration, reason).thenAccept(modCase -> {
            event.getChannel().sendMessage("🔇 User " + target.getAsMention() + " has been muted for " + duration.toMinutes() + " minutes (Case #" + modCase.id() + "). Reason: `" + reason + "`").queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Command can only be run in a server.").setEphemeral(true).queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ **Access Denied**: You require `Moderate Members` permission.").setEphemeral(true).queue();
            return;
        }

        OptionMapping userOpt = event.getOption("user");
        OptionMapping durationOpt = event.getOption("minutes");
        OptionMapping reasonOpt = event.getOption("reason");

        if (userOpt == null) {
            event.reply("❌ Please specify a user.").setEphemeral(true).queue();
            return;
        }

        Member target = userOpt.getAsMember();
        long minutes = durationOpt != null ? durationOpt.getAsLong() : 10;
        String reason = reasonOpt != null ? reasonOpt.getAsString() : "No reason provided";

        if (target == null || !ModerationService.canModerate(mod, target)) {
            event.reply("❌ Cannot mute this user (role hierarchy restriction).").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        Duration duration = Duration.ofMinutes(Math.min(28 * 24 * 60, Math.max(1, minutes)));
        ModerationService.executeMute(event.getGuild(), mod, target, duration, reason).thenAccept(modCase -> {
            event.getHook().sendMessage("🔇 User " + target.getAsMention() + " has been muted for " + duration.toMinutes() + " minutes (Case #" + modCase.id() + "). Reason: `" + reason + "`").queue();
        });
    }

    @Override
    public String getName() { return "mute"; }

    @Override
    public String getDescription() { return "Timeout (mute) a server member for a specified duration in minutes"; }

    @Override
    public String getUsage() { return "!mute <@user> <minutes> <reason> or /mute user:<user> minutes:<int> reason:<text>"; }
}
