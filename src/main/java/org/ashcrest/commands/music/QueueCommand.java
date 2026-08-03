package org.ashcrest.commands.music;

import org.ashcrest.commands.ICommand;
import org.ashcrest.music.GuildMusicManager;
import org.ashcrest.music.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.List;

public class QueueCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildQueueEmbed(event.getGuild()).build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        event.getHook().sendMessageEmbeds(buildQueueEmbed(event.getGuild()).build()).queue();
    }

    private EmbedBuilder buildQueueEmbed(net.dv8tion.jda.api.entities.Guild guild) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(guild);
        AudioTrack playing = musicManager.getPlayer().getPlayingTrack();
        List<AudioTrack> queue = musicManager.getScheduler().getQueue();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎶 Aegis Music Queue")
                .setColor(new Color(138, 43, 226));

        if (playing != null) {
            embed.addField("▶️ Now Playing", "[" + playing.getInfo().title + "](" + playing.getInfo().uri + ") (" + formatDuration(playing.getDuration()) + ")", false);
        } else {
            embed.addField("▶️ Now Playing", "Nothing currently playing", false);
        }

        if (queue.isEmpty()) {
            embed.addField("📋 Upcoming Queue", "The queue is currently empty.", false);
        } else {
            StringBuilder sb = new StringBuilder();
            int count = Math.min(queue.size(), 10);
            for (int i = 0; i < count; i++) {
                AudioTrack track = queue.get(i);
                sb.append("`").append(i + 1).append(".` [").append(track.getInfo().title).append("](").append(track.getInfo().uri).append(") (").append(formatDuration(track.getDuration())).append(")\n");
            }
            if (queue.size() > 10) {
                sb.append("\n*...and ").append(queue.size() - 10).append(" more track(s)*");
            }
            embed.addField("📋 Upcoming Queue (" + queue.size() + ")", sb.toString(), false);
        }

        return embed;
    }

    private String formatDuration(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String getName() { return "queue"; }

    @Override
    public String getDescription() { return "View the current music queue and now playing track"; }

    @Override
    public String getUsage() { return "!queue"; }
}
