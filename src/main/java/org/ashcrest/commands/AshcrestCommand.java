package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Official AshcrestHQ community resource & bot invite command.
 */
public class AshcrestCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildAshcrestEmbed().build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildAshcrestEmbed().build()).queue();
    }

    private EmbedBuilder buildAshcrestEmbed() {
        return new EmbedBuilder()
                .setTitle("🛡️ AshcrestHQ Community & Operations Suite")
                .setDescription("Official hubs, documentation, public bot invite, and open-source repository for Ashcrest Aegis.")
                .setColor(new Color(0x00A8FC))
                .addField("💻 GitHub Repository", "[AshcrestHQ/Aegis](https://github.com/AshcrestHQ/Aegis)", false)
                .addField("🤖 Public Bot Invite", "[Invite Aegis to Your Server](" + org.ashcrest.core.Config.PUBLIC_INVITE_URL + ")", false)
                .addField("🌐 Ashcrest Operations Hub", "[ashcrest.org](https://ashcrest.org)", false)
                .addField("🎮 Cyber Operations RPG", "Use `/hack` to play the interactive penetration testing mini-game, `/profile` for stats, and `/leaderboard` for rankings!", false)
                .setFooter("AshcrestHQ | Universal Discord Security, Operations & AI", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "ashcrest"; }

    @Override
    public String getDescription() { return "View official links, public bot invite, and resources for AshcrestHQ Aegis"; }

    @Override
    public String getUsage() { return "!ashcrest or /ashcrest"; }
}
