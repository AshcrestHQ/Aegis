package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Interactive Paginated Help Command for Ashcrest Aegis.
 */
public class HelpCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildOverviewEmbed().build())
                .setComponents(ActionRow.of(buildCategorySelectMenu()))
                .queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildOverviewEmbed().build())
                .setComponents(ActionRow.of(buildCategorySelectMenu()))
                .queue();
    }

    public static EmbedBuilder buildOverviewEmbed() {
        return new EmbedBuilder()
                .setTitle("🛡️ Ashcrest Aegis — Master Command Index")
                .setDescription("""
                        Welcome to **Aegis**! A universal Discord operations bot powered by **Java 26 Virtual Threads**.
                        Use the **Select Menu** below to navigate commands across categories:
                        
                        🛡️ **Security Recon & Audit**: `/dnsscan`, `/headers`, `/ssl`, `/securitytxt`, `/hashid`, `/iplookup`, `/passwordstrength`, `/breach`, `/blacklist`, `/phishcheck`, `/cve`, `/subdomains`, `/audit`
                        🔨 **Moderation Engine**: `/warn`, `/warnings`, `/mute`, `/unmute`, `/kick`, `/ban`, `/modlogs`, `/purge`, `/slowmode`, `/lockdown`
                        🚨 **Anti-Raid & Onboarding**: `/raidmode`, `/verify`, `/automod`, `/report`, `/config`
                        🤖 **AI Assistant**: `/ask`, `/chat`, `/summarize`, `/translate`, `/explain`, `/code`
                        🎮 **Cyber RPG & Economy**: `/hack`, `/profile`, `/leaderboard`, `/daily`
                        🎉 **Fun & Utilities**: `/coinflip`, `/roll`, `/8ball`, `/joke`, `/meme`
                        📚 **Community Wiki**: `/wikiadd`
                        🎵 **VC Music**: `/play`, `/skip`, `/stop`, `/queue`, `/nowplaying`, `/pause`, `/resume`
                        ℹ️ **System & Info**: `/status`, `/ashcrest`, `/source`
                        """)
                .setColor(new Color(0x00A8FC))
                .setFooter("AshcrestHQ Aegis Operations | Java 26 Virtual Threads", null)
                .setTimestamp(Instant.now());
    }

    public static StringSelectMenu buildCategorySelectMenu() {
        return StringSelectMenu.create("help_category_select")
                .setPlaceholder("📂 Choose a Command Category...")
                .addOption("🛡️ Security Recon & Audit", "help_sec", "DNS, headers, SSL, IP lookup, entropy, security audit")
                .addOption("🔨 Moderation Engine", "help_mod", "Warn, mute, kick, ban, purge, slowmode, lockdown")
                .addOption("🚨 Anti-Raid & Onboarding", "help_raid", "Raidmode, verify gate, automod, report, config")
                .addOption("🤖 AI Assistant", "help_ai", "Gemini 3.6 Flash ask, chat, summarize, translate, explain, code")
                .addOption("🎮 Cyber RPG & Economy", "help_rpg", "Hack penetration game, profile, leaderboard, daily")
                .addOption("🎉 Fun & Utilities", "help_fun", "Coinflip, roll dice, 8ball, joke, meme")
                .addOption("🎵 VC Music Audio", "help_music", "DAVE E2EE VC audio, Apple Music, YouTube multi-client")
                .addOption("ℹ️ System & Info", "help_sys", "Bot health, status, Ashcrest links, open-source repo")
                .build();
    }

    @Override
    public String getName() { return "help"; }

    @Override
    public String getDescription() { return "Display Aegis master command directory categorized by module"; }

    @Override
    public String getUsage() { return "!help or /help"; }
}
