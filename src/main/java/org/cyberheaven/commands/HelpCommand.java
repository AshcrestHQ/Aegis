package org.cyberheaven.commands;

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
        event.replyEmbeds(buildHelpEmbed().build()).queue();
    }

    private EmbedBuilder buildHelpEmbed() {
        return new EmbedBuilder()
                .setTitle("🛡️ Aegis Command Directory")
                .setDescription("High-performance Discord security engineering & community bot for CyberHeaven.")
                .setColor(new Color(0x00A8FC))
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
                        "`/cyberheaven` - View official CyberHeaven community links\n" +
                        "`/source` (or `/aegis`) - View open-source GitHub repository & MIT license info\n" +
                        "`/help` - Display this command directory", false)
                .setFooter("CyberHeaven Aegis | Non-blocking Java 26 Virtual Threads", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "help"; }

    @Override
    public String getDescription() { return "Display Aegis commands categorized by security, wiki, and system info"; }

    @Override
    public String getUsage() { return "!help or /help"; }
}
