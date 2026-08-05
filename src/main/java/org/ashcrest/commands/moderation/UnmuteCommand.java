package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import org.ashcrest.moderation.ModerationService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Moderation Unmute Command.
 */
public class UnmuteCommand implements ICommand {

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

        if (event.getMessage().getMentions().getMembers().isEmpty()) {
            event.getChannel().sendMessage("❌ **Usage**: `!unmute <@user>`").queue();
            return;
        }

        Member target = event.getMessage().getMentions().getMembers().get(0);
        String reason = args.length >= 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Unmuted by moderator";

        ModerationService.executeUnmute(event.getGuild(), mod, target, reason).thenAccept(ok -> {
            event.getChannel().sendMessage("🔊 Mute removed for user " + target.getAsMention() + ".").queue();
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
        if (userOpt == null) {
            event.reply("❌ Please specify a user.").setEphemeral(true).queue();
            return;
        }

        Member target = userOpt.getAsMember();
        if (target == null) {
            event.reply("❌ Target member not found.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        ModerationService.executeUnmute(event.getGuild(), mod, target, "Unmuted by moderator").thenAccept(ok -> {
            event.getHook().sendMessage("🔊 Mute removed for user " + target.getAsMention() + ".").queue();
        });
    }

    @Override
    public String getName() { return "unmute"; }

    @Override
    public String getDescription() { return "Remove timeout (unmute) for a server member"; }

    @Override
    public String getUsage() { return "!unmute <@user> or /unmute user:<user>"; }
}
