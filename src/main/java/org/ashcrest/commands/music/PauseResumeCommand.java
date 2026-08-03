package org.ashcrest.commands.music;

import org.ashcrest.commands.ICommand;
import org.ashcrest.music.GuildMusicManager;
import org.ashcrest.music.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PauseResumeCommand implements ICommand {
    private final boolean isPause;

    public PauseResumeCommand(boolean isPause) {
        this.isPause = isPause;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioPlayer player = musicManager.getPlayer();

        if (player.getPlayingTrack() == null) {
            event.getChannel().sendMessage("⚠️ No track is currently playing.").queue();
            return;
        }

        player.setPaused(isPause);
        if (isPause) {
            event.getChannel().sendMessage("⏸️ Paused playback.").queue();
        } else {
            event.getChannel().sendMessage("▶️ Resumed playback.").queue();
        }
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioPlayer player = musicManager.getPlayer();

        if (player.getPlayingTrack() == null) {
            event.getHook().sendMessage("⚠️ No track is currently playing.").queue();
            return;
        }

        player.setPaused(isPause);
        if (isPause) {
            event.getHook().sendMessage("⏸️ Paused playback.").queue();
        } else {
            event.getHook().sendMessage("▶️ Resumed playback.").queue();
        }
    }

    @Override
    public String getName() { return isPause ? "pause" : "resume"; }

    @Override
    public String getDescription() { return isPause ? "Pause audio playback" : "Resume paused audio playback"; }

    @Override
    public String getUsage() { return isPause ? "!pause" : "!resume"; }
}
