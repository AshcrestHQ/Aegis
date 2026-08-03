package org.ashcrest.commands.music;

import org.ashcrest.commands.ICommand;
import org.ashcrest.music.GuildMusicManager;
import org.ashcrest.music.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        musicManager.getScheduler().clearQueue();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.getChannel().sendMessage("⏹️ Stopped playback, cleared queue, and disconnected from Voice Channel.").queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        musicManager.getScheduler().clearQueue();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.getHook().sendMessage("⏹️ Stopped playback, cleared queue, and disconnected from Voice Channel.").queue();
    }

    @Override
    public String getName() { return "stop"; }

    @Override
    public String getDescription() { return "Stop music playback, clear the queue, and disconnect Aegis from VC"; }

    @Override
    public String getUsage() { return "!stop or !leave"; }
}
