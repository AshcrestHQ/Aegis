package org.cyberheaven.commands.music;

import org.cyberheaven.commands.ICommand;
import org.cyberheaven.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PlayCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("⚠️ Usage: `!play <song name | url | apple music link>`").queue();
            return;
        }

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage("⚠️ You must be connected to a Voice Channel to play music.").queue();
            return;
        }

        AudioChannel voiceChannel = member.getVoiceState().getChannel();
        String query = String.join(" ", args);
        PlayerManager.getInstance().loadAndPlay(event.getGuild(), event.getChannel().asTextChannel(), voiceChannel, query, null);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption("input");
        if (option == null) {
            event.reply("⚠️ Please provide a song name, URL, or Apple Music link.").setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            event.reply("⚠️ You must be connected to a Voice Channel to play music.").setEphemeral(true).queue();
            return;
        }

        AudioChannel voiceChannel = member.getVoiceState().getChannel();
        String query = option.getAsString();
        PlayerManager.getInstance().loadAndPlay(event.getGuild(), event.getChannel().asTextChannel(), voiceChannel, query, event);
    }

    @Override
    public String getName() { return "play"; }

    @Override
    public String getDescription() { return "Play audio in your Voice Channel from YouTube, direct links, or Apple Music"; }

    @Override
    public String getUsage() { return "!play <song | url | apple_music_link>"; }
}
