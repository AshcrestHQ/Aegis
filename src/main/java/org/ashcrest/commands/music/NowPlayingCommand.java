package org.ashcrest.commands.music;

import org.ashcrest.commands.ICommand;
import org.ashcrest.music.GuildMusicManager;
import org.ashcrest.music.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

public class NowPlayingCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildNowPlayingEmbed(event.getGuild()).build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        event.getHook().sendMessageEmbeds(buildNowPlayingEmbed(event.getGuild()).build()).queue();
    }

    private EmbedBuilder buildNowPlayingEmbed(net.dv8tion.jda.api.entities.Guild guild) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(guild);
        AudioTrack playing = musicManager.getPlayer().getPlayingTrack();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▶️ Now Playing")
                .setColor(new Color(138, 43, 226));

        if (playing == null) {
            embed.setDescription("Nothing is currently playing.");
        } else {
            long position = playing.getPosition();
            long duration = playing.getDuration();
            String progressBar = buildProgressBar(position, duration);

            embed.setDescription("[" + playing.getInfo().title + "](" + playing.getInfo().uri + ")\n\n" +
                    "`" + formatDuration(position) + "` " + progressBar + " `" + formatDuration(duration) + "`")
                    .addField("Artist/Author", playing.getInfo().author, true);
        }

        return embed;
    }

    private String buildProgressBar(long current, long total) {
        if (total <= 0) return "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
        int totalBlocks = 20;
        int progressBlocks = (int) (((double) current / total) * totalBlocks);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalBlocks; i++) {
            if (i == progressBlocks) {
                sb.append("🔘");
            } else {
                sb.append("▬");
            }
        }
        return sb.toString();
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
    public String getName() { return "nowplaying"; }

    @Override
    public String getDescription() { return "Display details and progress bar of currently playing audio track"; }

    @Override
    public String getUsage() { return "!nowplaying"; }
}
