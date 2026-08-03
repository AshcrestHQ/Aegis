package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.actionrow.ActionRow;

import java.awt.Color;
import java.time.Instant;
import java.util.Random;

/**
 * Interactive Cyber Penetration Test & Breach Mini-Game Command.
 */
public class HackCommand implements ICommand {
    private static final Random RANDOM = new Random();

    public static final String[] TARGETS = {
            "🏦 Quantum Financial Mainframe",
            "🛰️ Deep Space Satellite Array",
            "🤖 AI Neural Core Facility",
            "🛡️ Global Defense Substation",
            "⚡ Crypto Vault Relays"
    };

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        long userId = event.getAuthor().getIdLong();
        EmbedBuilder embed = buildHackEmbed(event.getAuthor().getEffectiveName(), userId);
        event.getChannel().sendMessageEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.primary("hack_brute:" + userId, "🔨 Brute Force SSH"),
                        Button.success("hack_zeroday:" + userId, "⚡ Zero-Day Exploit"),
                        Button.secondary("hack_phish:" + userId, "🎣 Social Engineering"),
                        Button.danger("hack_stealth:" + userId, "🕵️ Stealth Probe")
                )).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long userId = event.getUser().getIdLong();
        EmbedBuilder embed = buildHackEmbed(event.getUser().getEffectiveName(), userId);
        event.getHook().sendMessageEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.primary("hack_brute:" + userId, "🔨 Brute Force SSH"),
                        Button.success("hack_zeroday:" + userId, "⚡ Zero-Day Exploit"),
                        Button.secondary("hack_phish:" + userId, "🎣 Social Engineering"),
                        Button.danger("hack_stealth:" + userId, "🕵️ Stealth Probe")
                )).queue();
    }

    private EmbedBuilder buildHackEmbed(String username, long userId) {
        String target = TARGETS[RANDOM.nextInt(TARGETS.length)];
        int difficulty = 1 + RANDOM.nextInt(5);
        long reward = difficulty * 75L + RANDOM.nextInt(50);

        return new EmbedBuilder()
                .setTitle("💻 Ashcrest Cyber Operations — Penetration Test")
                .setDescription("Agent **" + username + "**, target node identified: **" + target + "**!\n" +
                        "Select your intrusion vector below before firewall countermeasures deploy.")
                .setColor(new Color(0xFF9900))
                .addField("🎯 Target System", target, true)
                .addField("🔒 Firewall Security Tier", "Level " + difficulty + " Security Matrix", true)
                .addField("🪙 Estimated Reward", "Up to **" + reward + " 🪙 Ashcrest Credits** + XP", true)
                .setFooter("Ashcrest Aegis Cyber Operations • Select a tactic below", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "hack"; }

    @Override
    public String getDescription() { return "Attempt an interactive cyber security breach against target mainframes for credits & XP!"; }

    @Override
    public String getUsage() { return "!hack or /hack"; }
}
