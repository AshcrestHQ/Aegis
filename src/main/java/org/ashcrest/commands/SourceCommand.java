package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.ashcrest.core.AegisBot;
import org.ashcrest.core.Config;

import java.awt.Color;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;

/**
 * Open-source repository & dynamic metrics command for Aegis.
 */
public class SourceCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessageEmbeds(buildSourceEmbed(event.getJDA()).build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(buildSourceEmbed(event.getJDA()).build()).queue();
    }

    private EmbedBuilder buildSourceEmbed(JDA jda) {
        long serverCount = jda != null ? jda.getGuilds().size() : 0;
        long totalMembers = jda != null ? jda.getGuilds().stream().mapToLong(net.dv8tion.jda.api.entities.Guild::getMemberCount).sum() : 0;
        long commandCount = (AegisBot.getInstance() != null && AegisBot.getInstance().getCommands() != null)
                ? AegisBot.getInstance().getCommands().values().stream().map(ICommand::getName).distinct().count()
                : 41;

        NumberFormat nf = NumberFormat.getInstance(Locale.US);

        return new EmbedBuilder()
                .setTitle("🛡️ Aegis Security Bot — System Metrics & Source")
                .setDescription("Aegis is an open-source Discord operations & security bot engineered with **Java 26** and **Virtual Threads**.")
                .setColor(new Color(0x00A8FC))
                .addField("📊 Dynamic Bot Metrics", String.format("""
                        • **Total Commands**: `%d` commands
                        • **Members Using Me**: `%s` members
                        • **Servers I am in**: `%d` servers
                        """, commandCount, nf.format(totalMembers), serverCount), false)
                .addField("💻 GitHub Repository", "[AshcrestHQ/Aegis](https://github.com/AshcrestHQ/Aegis)", false)
                .addField("🤖 Public Bot Invite", "[Invite Aegis to Your Server](" + Config.PUBLIC_INVITE_URL + ")", false)
                .addField("📜 Open Source License", "Released under the **MIT License** — free to audit, contribute, and modify.", false)
                .setFooter("AshcrestHQ Aegis | Universal Discord Security Operations", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "source"; }

    @Override
    public String getDescription() { return "View dynamic bot metrics, total commands, server count, GitHub repo, and license info for Aegis"; }

    @Override
    public String getUsage() { return "!source or /source (alias: /aegis)"; }
}
