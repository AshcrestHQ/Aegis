package org.cyberheaven.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Singleton manager for audio players and track loading across all guilds.
 */
public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager playerManager;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        // Register Lavalink YouTube Source Manager plugin for updated YouTube clients
        dev.lavalink.youtube.YoutubeAudioSourceManager youtube = new dev.lavalink.youtube.YoutubeAudioSourceManager();
        this.playerManager.registerSourceManager(youtube);

        AudioSourceManagers.registerRemoteSources(this.playerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        AudioSourceManagers.registerLocalSource(this.playerManager);
    }

    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    /**
     * Load and play audio track or Apple Music link for a given guild and audio channel.
     */
    public void loadAndPlay(Guild guild, TextChannel textChannel, AudioChannel userVoiceChannel, String input, IReplyCallback replyCallback) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);
        AudioManager audioManager = guild.getAudioManager();

        // Connect to voice channel if not connected
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(userVoiceChannel);
        }

        // Check if input is Apple Music link
        if (AppleMusicResolver.isAppleMusicUrl(input)) {
            Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
                List<AppleMusicResolver.AppleMusicTrack> appleTracks = AppleMusicResolver.resolve(input);
                if (appleTracks.isEmpty()) {
                    sendReply(replyCallback, textChannel, "❌ Could not resolve metadata for Apple Music link.");
                    return;
                }

                sendReply(replyCallback, textChannel, "🍎 Resolving **" + appleTracks.size() + "** track(s) from Apple Music...");

                for (AppleMusicResolver.AppleMusicTrack amTrack : appleTracks) {
                    String searchQuery = amTrack.toSearchQuery();
                    playerManager.loadItemOrdered(musicManager, searchQuery, new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            musicManager.getScheduler().queue(track);
                            sendAppleMusicEmbed(textChannel, amTrack, track);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            if (!playlist.getTracks().isEmpty()) {
                                AudioTrack track = playlist.getTracks().get(0);
                                musicManager.getScheduler().queue(track);
                                sendAppleMusicEmbed(textChannel, amTrack, track);
                            }
                        }

                        @Override
                        public void noMatches() {
                            sendReply(null, textChannel, "⚠️ No matches found on audio streams for: `" + amTrack.title() + "`");
                        }

                        @Override
                        public void loadFailed(FriendlyException exception) {
                            sendReply(null, textChannel, "❌ Failed to load audio stream for: `" + amTrack.title() + "`");
                        }
                    });
                }
            });
            return;
        }

        // Format search query if raw text
        String searchQuery = input;
        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            searchQuery = "ytsearch:" + input;
        }

        playerManager.loadItemOrdered(musicManager, searchQuery, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.getScheduler().queue(track);
                sendTrackQueuedEmbed(replyCallback, textChannel, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack track = playlist.getTracks().get(0);
                    musicManager.getScheduler().queue(track);
                    sendTrackQueuedEmbed(replyCallback, textChannel, track);
                } else {
                    for (AudioTrack track : playlist.getTracks()) {
                        musicManager.getScheduler().queue(track);
                    }
                    sendReply(replyCallback, textChannel, "🎶 Queued **" + playlist.getTracks().size() + "** tracks from playlist: `" + playlist.getName() + "`");
                }
            }

            @Override
            public void noMatches() {
                sendReply(replyCallback, textChannel, "❌ No audio sources found matching: `" + input + "`");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sendReply(replyCallback, textChannel, "❌ Failed to load track: " + exception.getMessage());
            }
        });
    }

    private void sendTrackQueuedEmbed(IReplyCallback callback, TextChannel channel, AudioTrack track) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎶 Added to Music Queue")
                .setDescription("[" + track.getInfo().title + "](" + track.getInfo().uri + ")")
                .addField("Artist/Author", track.getInfo().author, true)
                .addField("Duration", formatDuration(track.getDuration()), true)
                .setColor(new Color(138, 43, 226));

        if (callback != null) {
            if (callback.isAcknowledged()) {
                callback.getHook().sendMessageEmbeds(embed.build()).queue();
            } else {
                callback.replyEmbeds(embed.build()).queue();
            }
        } else if (channel != null) {
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    private void sendAppleMusicEmbed(TextChannel channel, AppleMusicResolver.AppleMusicTrack amTrack, AudioTrack track) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🍎 Apple Music Track Queued")
                .setDescription("**" + amTrack.title() + "**")
                .addField("Artist", amTrack.artist(), true)
                .addField("Album", amTrack.album(), true)
                .addField("Duration", formatDuration(track.getDuration()), true)
                .setColor(new Color(250, 36, 60)); // Apple Music Red

        if (amTrack.artworkUrl() != null && !amTrack.artworkUrl().isEmpty()) {
            embed.setThumbnail(amTrack.artworkUrl());
        }

        if (channel != null) {
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    private void sendReply(IReplyCallback callback, TextChannel channel, String msg) {
        if (callback != null) {
            if (callback.isAcknowledged()) {
                callback.getHook().sendMessage(msg).queue();
            } else {
                callback.reply(msg).queue();
            }
        } else if (channel != null) {
            channel.sendMessage(msg).queue();
        }
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
}
