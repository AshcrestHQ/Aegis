package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import org.ashcrest.moderation.ModerationService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Moderation Ban & Unban Command.
 */
public class BanCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Command can only be run in a server.").queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.BAN_MEMBERS)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `Ban Members` permission.").queue();
            return;
        }

        if (args.length >= 1 && "unban".equalsIgnoreCase(args[0]) && args.length >= 2) {
            try {
                long targetId = Long.parseLong(args[1]);
                event.getJDA().retrieveUserById(targetId).queue(user -> {
                    ModerationService.executeUnban(event.getGuild(), mod, user, "Unbanned by moderator").thenAccept(ok -> {
                        event.getChannel().sendMessage("🔓 User **" + user.getAsTag() + "** has been unbanned.").queue();
                    });
                }, err -> event.getChannel().sendMessage("❌ User not found.").queue());
                return;
            } catch (NumberFormatException ignored) {}
        }

        if (event.getMessage().getMentions().getMembers().isEmpty() || args.length < 2) {
            event.getChannel().sendMessage("❌ **Usage**: `!ban <@user> <reason>` | `!ban unban <user_id>`").queue();
            return;
        }

        Member target = event.getMessage().getMentions().getMembers().get(0);
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        if (!ModerationService.canModerate(mod, target)) {
            event.getChannel().sendMessage("❌ Cannot ban this user (role hierarchy restriction).").queue();
            return;
        }

        ModerationService.executeBan(event.getGuild(), mod, target.getUser(), reason).thenAccept(modCase -> {
            event.getChannel().sendMessage("🔨 User **" + target.getUser().getAsTag() + "** has been banned (Case #" + modCase.id() + "). Reason: `" + reason + "`").queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Command can only be run in a server.").setEphemeral(true).queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("❌ **Access Denied**: You require `Ban Members` permission.").setEphemeral(true).queue();
            return;
        }

        String sub = event.getSubcommandName();
        event.deferReply().queue();

        if ("unban".equalsIgnoreCase(sub)) {
            OptionMapping idOpt = event.getOption("user_id");
            if (idOpt != null) {
                long targetId = idOpt.getAsLong();
                event.getJDA().retrieveUserById(targetId).queue(user -> {
                    ModerationService.executeUnban(event.getGuild(), mod, user, "Unbanned by moderator").thenAccept(ok -> {
                        event.getHook().sendMessage("🔓 User **" + user.getAsTag() + "** has been unbanned.").queue();
                    });
                }, err -> event.getHook().sendMessage("❌ User ID not found.").queue());
            }
            return;
        }

        OptionMapping userOpt = event.getOption("user");
        OptionMapping reasonOpt = event.getOption("reason");

        if (userOpt == null || reasonOpt == null) {
            event.getHook().sendMessage("❌ Please specify user and reason.").queue();
            return;
        }

        User targetUser = userOpt.getAsUser();
        Member targetMember = userOpt.getAsMember();
        String reason = reasonOpt.getAsString();

        if (targetMember != null && !ModerationService.canModerate(mod, targetMember)) {
            event.getHook().sendMessage("❌ Cannot ban this user (role hierarchy restriction).").queue();
            return;
        }

        ModerationService.executeBan(event.getGuild(), mod, targetUser, reason).thenAccept(modCase -> {
            event.getHook().sendMessage("🔨 User **" + targetUser.getAsTag() + "** has been banned (Case #" + modCase.id() + "). Reason: `" + reason + "`").queue();
        });
    }

    @Override
    public String getName() { return "ban"; }

    @Override
    public String getDescription() { return "Ban a user from the server or unban by user ID"; }

    @Override
    public String getUsage() { return "!ban <@user> <reason> or /ban user:<user> reason:<text> | /ban unban user_id:<id>"; }
}
