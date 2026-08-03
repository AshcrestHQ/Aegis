package org.ashcrest.commands.music;

import org.ashcrest.commands.ICommand;
import org.ashcrest.music.GuildMusicManager;
import org.ashcrest.music.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SkipCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack playing = musicManager.getPlayer().getPlayingTrack();

        if (playing == null) {
            event.getChannel().sendMessage("⚠️ No track is currently playing.").queue();
            return;
        }

        boolean skipped = musicManager.getScheduler().nextTrack();
        if (skipped) {
            event.getChannel().sendMessage("⏭️ Skipped `" + playing.getInfo().title + "`").queue();
        } else {
            event.getChannel().sendMessage("⏭️ Skipped `" + playing.getInfo().title + "`. Queue is now empty.").queue();
        }
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack playing = musicManager.getPlayer().getPlayingTrack();

        if (playing == null) {
            event.getHook().sendMessage("⚠️ No track is currently playing.").queue();
            return;
        }

        boolean skipped = musicManager.getScheduler().nextTrack();
        if (skipped) {
            event.getHook().sendMessage("⏭️ Skipped `" + playing.getInfo().title + "`").queue();
        } else {
            event.getHook().sendMessage("⏭️ Skipped `" + playing.getInfo().title + "`. Queue is now empty.").queue();
        }
    }

    @Override
    public String getName() { return "skip"; }

    @Override
    public String getDescription() { return "Skip the currently playing track"; }

    @Override
    public String getUsage() { return "!skip"; }
}
