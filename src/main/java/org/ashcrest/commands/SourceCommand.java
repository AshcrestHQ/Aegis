package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Open-source repository command for Aegis.
 */
public class SourceCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildSourceEmbed().build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildSourceEmbed().build()).queue();
    }

    private EmbedBuilder buildSourceEmbed() {
        return new EmbedBuilder()
                .setTitle("🛡️ Aegis Source Code & Repository")
                .setDescription("Aegis is an open-source Discord security engineering bot engineered with **Java 26** and **Virtual Threads**.")
                .setColor(new Color(0x23272A))
                .addField("📦 GitHub Repository", "[AshcrestHQ/Aegis](https://github.com/AshcrestHQ/Aegis)", false)
                .addField("📜 Open Source License", "Released under the **MIT License** — free to audit, contribute, and modify.", false)
                .addField("🦅 Heritage", "Built from the architectural foundation born in Apex.", false)
                .setFooter("AshcrestHQ Aegis | Open Source Security Engineering", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "source"; }

    @Override
    public String getDescription() { return "View the open-source GitHub repository and MIT license info for Aegis"; }

    @Override
    public String getUsage() { return "!source or /source"; }
}
