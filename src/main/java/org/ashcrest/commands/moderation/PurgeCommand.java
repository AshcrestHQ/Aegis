package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

/**
 * Moderation Purge (Bulk Delete) Command.
 */
public class PurgeCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Command can only be run in a server.").queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `Manage Messages` permission.").queue();
            return;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!purge <count_1_to_100>`").queue();
            return;
        }

        int count = 10;
        try {
            count = Math.min(100, Math.max(1, Integer.parseInt(args[0])));
        } catch (NumberFormatException ignored) {}

        TextChannel channel = event.getChannel().asTextChannel();
        int finalCount = count;

        channel.getIterableHistory().takeAsync(finalCount + 1).thenAccept(messages -> {
            List<Message> toDelete = messages.stream().filter(m -> !m.isPinned()).toList();
            channel.purgeMessages(toDelete);
            channel.sendMessage("🧹 Successfully purged **" + (toDelete.size() - 1) + "** messages.").queue(msg -> {
                msg.delete().queueAfter(3, java.util.concurrent.TimeUnit.SECONDS);
            });
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Command can only be run in a server.").setEphemeral(true).queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("❌ **Access Denied**: You require `Manage Messages` permission.").setEphemeral(true).queue();
            return;
        }

        OptionMapping countOpt = event.getOption("count");
        int count = countOpt != null ? Math.min(100, Math.max(1, countOpt.getAsInt())) : 10;

        event.deferReply().setEphemeral(true).queue();

        TextChannel channel = event.getChannel().asTextChannel();
        channel.getIterableHistory().takeAsync(count).thenAccept(messages -> {
            List<Message> toDelete = messages.stream().filter(m -> !m.isPinned()).toList();
            channel.purgeMessages(toDelete);
            event.getHook().sendMessage("🧹 Successfully purged **" + toDelete.size() + "** messages.").queue();
        });
    }

    @Override
    public String getName() { return "purge"; }

    @Override
    public String getDescription() { return "Bulk delete recent messages in the current channel (max 100 per batch)"; }

    @Override
    public String getUsage() { return "!purge <count> or /purge count:<int>"; }
}
