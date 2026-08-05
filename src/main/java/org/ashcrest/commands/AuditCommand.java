package org.ashcrest.commands;

import org.ashcrest.security.SecurityAuditService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

/**
 * Audit command for checking server permission security and elevated privileges.
 */
public class AuditCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Audit command can only be run within a server.").queue();
            return;
        }
        var audit = SecurityAuditService.auditGuild(event.getGuild());
        event.getChannel().sendMessageEmbeds(buildAuditEmbed(audit).build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Audit command can only be run within a server.").setEphemeral(true).queue();
            return;
        }
        var audit = SecurityAuditService.auditGuild(event.getGuild());
        event.replyEmbeds(buildAuditEmbed(audit).build()).queue();
    }

    private EmbedBuilder buildAuditEmbed(SecurityAuditService.AuditResult result) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛡️ Aegis Security & Permission Audit")
                .setColor(result.warningFlags().isEmpty() ? Color.GREEN : Color.ORANGE)
                .addField("👥 Total Members", String.valueOf(result.totalMembers()), true)
                .addField("⚡ Elevated Privilege Users", String.valueOf(result.dangerousRoleMembers()), true);

        if (result.warningFlags().isEmpty()) {
            embed.addField("✅ Status", "No critical permission misconfigurations detected.", false);
        } else {
            embed.addField("🚨 Warnings", String.join("\n", result.warningFlags()), false);
        }

        embed.setFooter("AshcrestHQ Aegis | Security Engineering")
             .setTimestamp(java.time.Instant.now());

        return embed;
    }

    @Override
    public String getName() { return "audit"; }

    @Override
    public String getDescription() { return "Audit server permissions, roles, and administrative risk levels"; }

    @Override
    public String getUsage() { return "!audit"; }
}
