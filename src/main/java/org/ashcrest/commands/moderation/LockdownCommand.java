package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Moderation Channel Lockdown & Unlock Command.
 */
public class LockdownCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Command can only be run in a server.").queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MANAGE_CHANNEL)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `Manage Channels` permission.").queue();
            return;
        }

        boolean unlock = args.length > 0 && "unlock".equalsIgnoreCase(args[0]);
        TextChannel channel = event.getChannel().asTextChannel();
        Role everyoneRole = event.getGuild().getPublicRole();

        if (unlock) {
            channel.getManager().putPermissionOverride(everyoneRole, null, java.util.List.of(Permission.MESSAGE_SEND)).queue(v -> {
                event.getChannel().sendMessage("🔓 **Channel Unlocked**: `@everyone` can now send messages.").queue();
            });
        } else {
            channel.getManager().putPermissionOverride(everyoneRole, null, java.util.List.of(Permission.MESSAGE_SEND)).queue(v -> {
                event.getChannel().sendMessage("🔒 **Channel Locked**: Message sending restricted for `@everyone`.").queue();
            });
        }
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Command can only be run in a server.").setEphemeral(true).queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MANAGE_CHANNEL)) {
            event.reply("❌ **Access Denied**: You require `Manage Channels` permission.").setEphemeral(true).queue();
            return;
        }

        String name = event.getName();
        boolean unlock = "unlock".equalsIgnoreCase(name);

        event.deferReply().queue();

        TextChannel channel = event.getChannel().asTextChannel();
        Role everyoneRole = event.getGuild().getPublicRole();

        if (unlock) {
            channel.getManager().putPermissionOverride(everyoneRole, null, java.util.List.of(Permission.MESSAGE_SEND)).queue(v -> {
                event.getHook().sendMessage("🔓 **Channel Unlocked**: `@everyone` can now send messages.").queue();
            });
        } else {
            channel.getManager().putPermissionOverride(everyoneRole, null, java.util.List.of(Permission.MESSAGE_SEND)).queue(v -> {
                event.getHook().sendMessage("🔒 **Channel Locked**: Message sending restricted for `@everyone`.").queue();
            });
        }
    }

    @Override
    public String getName() { return "lockdown"; }

    @Override
    public String getDescription() { return "Lockdown or unlock the current channel for @everyone"; }

    @Override
    public String getUsage() { return "!lockdown | !lockdown unlock or /lockdown | /unlock"; }
}
