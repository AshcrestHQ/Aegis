package org.cyberheaven.commands.music;

import org.cyberheaven.commands.ICommand;
import org.cyberheaven.music.GuildMusicManager;
import org.cyberheaven.music.PlayerManager;
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
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack playing = musicManager.getPlayer().getPlayingTrack();

        if (playing == null) {
            event.reply("⚠️ No track is currently playing.").setEphemeral(true).queue();
            return;
        }

        boolean skipped = musicManager.getScheduler().nextTrack();
        if (skipped) {
            event.reply("⏭️ Skipped `" + playing.getInfo().title + "`").queue();
        } else {
            event.reply("⏭️ Skipped `" + playing.getInfo().title + "`. Queue is now empty.").queue();
        }
    }

    @Override
    public String getName() { return "skip"; }

    @Override
    public String getDescription() { return "Skip the currently playing track"; }

    @Override
    public String getUsage() { return "!skip"; }
}
