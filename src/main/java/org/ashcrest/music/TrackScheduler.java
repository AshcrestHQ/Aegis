package org.ashcrest.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages the track queue and playback events for a single guild.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add track to queue or play immediately if nothing is currently playing.
     */
    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        net.dv8tion.jda.api.JDA jda = org.ashcrest.core.AegisBot.getJda();
        if (jda != null && track != null && track.getInfo() != null) {
            String title = track.getInfo().title;
            String author = track.getInfo().author;
            String label = title + (author != null && !author.equalsIgnoreCase("Unknown author") ? " - " + author : "");
            jda.getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.listening(label));
        }
    }

    /**
     * Skip current track and play next in queue.
     */
    public boolean nextTrack() {
        AudioTrack next = queue.poll();
        if (next != null) {
            player.startTrack(next, false);
            return true;
        } else {
            player.stopTrack();
            resetPresence();
            return false;
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue);
    }

    public void clearQueue() {
        queue.clear();
        player.stopTrack();
        resetPresence();
    }

    private void resetPresence() {
        net.dv8tion.jda.api.JDA jda = org.ashcrest.core.AegisBot.getJda();
        if (jda != null) {
            jda.getPresence().setActivity(net.dv8tion.jda.api.entities.Activity.watching("Ashcrest Operations"));
        }
    }

    public AudioPlayer getPlayer() {
        return player;
    }
}
