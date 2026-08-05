package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import org.ashcrest.moderation.ModerationService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Moderation Warn Command.
 */
public class WarnCommand implements ICommand {

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
            event.getChannel().sendMessage("❌ **Usage**: `!warn <@user> <reason>`").queue();
            return;
        }

        Member target = event.getMessage().getMentions().getMembers().get(0);
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        if (!ModerationService.canModerate(mod, target)) {
            event.getChannel().sendMessage("❌ Cannot warn this user (role hierarchy restriction).").queue();
            return;
        }

        ModerationService.executeWarn(event.getGuild(), mod, target, reason).thenAccept(modCase -> {
            event.getChannel().sendMessage("⚠️ User " + target.getAsMention() + " has been warned (Case #" + modCase.id() + "). Reason: `" + reason + "`").queue();
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
        OptionMapping reasonOpt = event.getOption("reason");

        if (userOpt == null || reasonOpt == null) {
            event.reply("❌ Please provide both user and reason.").setEphemeral(true).queue();
            return;
        }

        Member target = userOpt.getAsMember();
        String reason = reasonOpt.getAsString();

        if (target == null || !ModerationService.canModerate(mod, target)) {
            event.reply("❌ Cannot warn this user (role hierarchy restriction).").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        ModerationService.executeWarn(event.getGuild(), mod, target, reason).thenAccept(modCase -> {
            event.getHook().sendMessage("⚠️ User " + target.getAsMention() + " has been warned (Case #" + modCase.id() + "). Reason: `" + reason + "`").queue();
        });
    }

    @Override
    public String getName() { return "warn"; }

    @Override
    public String getDescription() { return "Warn a server member for violating community guidelines"; }

    @Override
    public String getUsage() { return "!warn <@user> <reason> or /warn user:<user> reason:<text>"; }
}
