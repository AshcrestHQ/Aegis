package org.ashcrest.commands.antiraid;

import org.ashcrest.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.time.Instant;

/**
 * Verification Gate Setup Command.
 */
public class VerifyCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Command can only be run in a server.").queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MANAGE_SERVER)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `Manage Server` permission.").queue();
            return;
        }

        event.getChannel().sendMessageEmbeds(buildVerifyEmbed(event.getGuild().getName()).build())
                .setComponents(ActionRow.of(
                        Button.success("aegis_verify", "✅ Complete Verification")
                )).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Command can only be run in a server.").setEphemeral(true).queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("❌ **Access Denied**: You require `Manage Server` permission.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();
        event.getHook().sendMessageEmbeds(buildVerifyEmbed(event.getGuild().getName()).build())
                .setComponents(ActionRow.of(
                        Button.success("aegis_verify", "✅ Complete Verification")
                )).queue();
    }

    private EmbedBuilder buildVerifyEmbed(String serverName) {
        return new EmbedBuilder()
                .setTitle("🛡️ Member Verification — " + serverName)
                .setDescription("Welcome to **" + serverName + "**!\nClick the button below to complete human verification and access full server channels.")
                .setColor(new Color(0x00FFC8))
                .setFooter("AshcrestHQ Aegis Security Gate", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "verify"; }

    @Override
    public String getDescription() { return "Post a button-based human verification gate for new server members"; }

    @Override
    public String getUsage() { return "!verify or /verify"; }
}
