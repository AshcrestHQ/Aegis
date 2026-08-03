package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Help command displaying categorized Aegis commands.
 */
public class HelpCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildHelpEmbed().build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        event.getHook().sendMessageEmbeds(buildHelpEmbed().build()).queue();
    }

    private EmbedBuilder buildHelpEmbed() {
        return new EmbedBuilder()
                .setTitle("🛡️ Ashcrest Aegis Command Directory")
                .setDescription("Universal Discord security, community operations, music, AI, and cyber RPG suite for AshcrestHQ.")
                .setColor(new Color(0x00A8FC))
                .addField("🤖 AI Assistant (Google Gemini)",
                        "`/ask <prompt>` (or `!ask <prompt>`) - Ask Aegis AI questions powered by Google Gemini 2.5 Flash\n", false)
                .addField("🎮 Cyber Operations RPG & Economy",
                        "`/hack` - Initiate interactive penetration test mini-game against target mainframes for credits & XP\n" +
                        "`/profile` - View your hacker rank, level, XP progress bar, credits balance, and win stats\n" +
                        "`/leaderboard` - View top 10 global/server hacker leaderboard\n" +
                        "`/daily` - Claim your daily 250 🪙 Ashcrest credits\n", false)
                .addField("🛡️ Security & Engineering",
                        "`/audit` - Audit server roles, elevated privileges, and security risks\n", false)
                .addField("📚 Community Wiki",
                        "`/wiki get <keyword>` - Query the security knowledgebase\n" +
                        "`/wiki add <keyword> <content>` - Add/update a wiki entry (*Staff/Admin restricted*)", false)
                .addField("🎵 VC Music & Apple Music",
                        "`/play <input>` - Play audio in VC (supports Apple Music, YouTube, Direct URLs)\n" +
                        "`/skip` - Skip current track\n" +
                        "`/stop` (or `!leave`) - Stop music, clear queue, & disconnect from VC\n" +
                        "`/queue` - View queued tracks and now playing song\n" +
                        "`/nowplaying` - Show progress bar & current track info\n" +
                        "`/pause` / `/resume` - Pause or resume music playback", false)
                .addField("⚙️ System & Community Info",
                        "`/status` - Check bot gateway latency, uptime, and Java 26 status\n" +
                        "`/ashcrest` - View official AshcrestHQ community links and public bot invite\n" +
                        "`/source` (or `/aegis`) - View open-source GitHub repository & MIT license info\n" +
                        "`/help` - Display this command directory\n" +
                        "📖 **Deployment Guide**: [SETUP.md](https://github.com/AshcrestHQ/Aegis/blob/main/SETUP.md)", false)
                .setFooter("Ashcrest Aegis | Universal Discord Bot Engine", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "help"; }

    @Override
    public String getDescription() { return "Display Aegis commands categorized by security, wiki, and system info"; }

    @Override
    public String getUsage() { return "!help or /help"; }
}
