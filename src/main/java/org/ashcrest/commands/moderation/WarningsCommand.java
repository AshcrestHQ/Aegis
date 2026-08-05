package org.ashcrest.commands.moderation;

import org.ashcrest.commands.ICommand;
import org.ashcrest.db.ModerationManager;
import org.ashcrest.db.ModerationManager.ModCase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * Moderation Warnings & Delwarn Command.
 */
public class WarningsCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Command can only be run in a server.").queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `Moderate Members` permission.").queue();
            return;
        }

        if (args.length > 0 && "del".equalsIgnoreCase(args[0]) && args.length >= 2) {
            try {
                int caseId = Integer.parseInt(args[1]);
                ModerationManager.getInstance().revokeCase(event.getGuild().getIdLong(), caseId).thenAccept(ok -> {
                    event.getChannel().sendMessage(ok ? "✅ Warning Case #" + caseId + " revoked." : "❌ Case not found or error revoking.").queue();
                });
                return;
            } catch (NumberFormatException ignored) {}
        }

        Member target = event.getMessage().getMentions().getMembers().isEmpty() ? mod : event.getMessage().getMentions().getMembers().get(0);
        ModerationManager.getInstance().getCaseHistory(event.getGuild().getIdLong(), target.getIdLong()).thenAccept(cases -> {
            event.getChannel().sendMessageEmbeds(buildWarningsEmbed(target, cases).build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Command can only be run in a server.").setEphemeral(true).queue();
            return;
        }

        Member mod = event.getMember();
        if (mod == null || !mod.hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply("❌ **Access Denied**: You require `Moderate Members` permission.").setEphemeral(true).queue();
            return;
        }

        OptionMapping delOpt = event.getOption("case_id");
        if (delOpt != null) {
            int caseId = delOpt.getAsInt();
            event.deferReply().queue();
            ModerationManager.getInstance().revokeCase(event.getGuild().getIdLong(), caseId).thenAccept(ok -> {
                event.getHook().sendMessage(ok ? "✅ Warning Case #" + caseId + " revoked." : "❌ Case not found or error revoking.").queue();
            });
            return;
        }

        OptionMapping userOpt = event.getOption("user");
        Member target = userOpt != null ? userOpt.getAsMember() : mod;

        event.deferReply().queue();

        if (target != null) {
            ModerationManager.getInstance().getCaseHistory(event.getGuild().getIdLong(), target.getIdLong()).thenAccept(cases -> {
                event.getHook().sendMessageEmbeds(buildWarningsEmbed(target, cases).build()).queue();
            });
        }
    }

    private EmbedBuilder buildWarningsEmbed(Member target, List<ModCase> cases) {
        List<ModCase> warns = cases.stream().filter(c -> "WARN".equalsIgnoreCase(c.action()) && c.active()).toList();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚠️ Warning Log — " + target.getUser().getAsTag())
                .setColor(warns.isEmpty() ? Color.GREEN : Color.ORANGE)
                .addField("📊 Active Warnings", "**" + warns.size() + "** active warning(s)", false)
                .setFooter("AshcrestHQ Aegis Moderation Engine", null)
                .setTimestamp(Instant.now());

        if (warns.isEmpty()) {
            embed.addField("Status", "No active warnings on record for this user.", false);
        } else {
            StringBuilder sb = new StringBuilder();
            for (ModCase c : warns) {
                sb.append("• **Case #").append(c.id()).append("** — `").append(c.reason()).append("` (<@").append(c.moderatorId()).append(">)\n");
            }
            embed.addField("Active Cases", sb.toString(), false);
        }

        return embed;
    }

    @Override
    public String getName() { return "warnings"; }

    @Override
    public String getDescription() { return "View warning cases for a member or delete a warning by case ID"; }

    @Override
    public String getUsage() { return "!warnings <@user> or /warnings user:<user> | /delwarn case_id:<id>"; }
}
