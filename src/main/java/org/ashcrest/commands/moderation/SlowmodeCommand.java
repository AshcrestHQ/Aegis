package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Moderation Slowmode Command.
 */
public class SlowmodeCommand implements ICommand {

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

        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!slowmode <seconds_0_to_21600>`").queue();
            return;
        }

        int seconds = 0;
        try {
            seconds = Math.min(21600, Math.max(0, Integer.parseInt(args[0])));
        } catch (NumberFormatException ignored) {}

        TextChannel channel = event.getChannel().asTextChannel();
        int finalSeconds = seconds;
        channel.getManager().setSlowmode(seconds).queue(v -> {
            event.getChannel().sendMessage(finalSeconds > 0 ? "⏱️ Channel slowmode set to **" + finalSeconds + "** seconds." : "⏱️ Slowmode disabled.").queue();
        });
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

        OptionMapping secOpt = event.getOption("seconds");
        int seconds = secOpt != null ? Math.min(21600, Math.max(0, secOpt.getAsInt())) : 0;

        event.deferReply().queue();

        TextChannel channel = event.getChannel().asTextChannel();
        channel.getManager().setSlowmode(seconds).queue(v -> {
            event.getHook().sendMessage(seconds > 0 ? "⏱️ Channel slowmode set to **" + seconds + "** seconds." : "⏱️ Slowmode disabled.").queue();
        });
    }

    @Override
    public String getName() { return "slowmode"; }

    @Override
    public String getDescription() { return "Set channel message slowmode delay in seconds (0 to disable)"; }

    @Override
    public String getUsage() { return "!slowmode <seconds> or /slowmode seconds:<int>"; }
}
