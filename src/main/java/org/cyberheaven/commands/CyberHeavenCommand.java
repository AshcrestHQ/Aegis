package org.cyberheaven.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Official CyberHeaven community resource command.
 */
public class CyberHeavenCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildCyberHeavenEmbed().build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildCyberHeavenEmbed().build()).queue();
    }

    private EmbedBuilder buildCyberHeavenEmbed() {
        return new EmbedBuilder()
                .setTitle("🌐 CyberHeaven Community Resources")
                .setDescription("Official links and hubs for the CyberHeaven security-education community.")
                .setColor(new Color(0x7289DA))
                .addField("🌐 Official Website", "[cyberheaven.eu](https://cyberheaven.eu)", false)
                .addField("💼 LinkedIn", "[CyberHeaven on LinkedIn](https://linkedin.com/company/cyberheaven/)", false)
                .addField("💻 GitHub Organization", "[CyberHeaven GitHub](https://github.com/Cyberheaven)", false)
                .setFooter("CyberHeaven | Empowering Security Engineers & Education", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "cyberheaven"; }

    @Override
    public String getDescription() { return "View official links and resources for the CyberHeaven community"; }

    @Override
    public String getUsage() { return "!cyberheaven or /cyberheaven"; }
}
