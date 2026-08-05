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
 * Moderation Case History Lookup Command.
 */
public class ModLogsCommand implements ICommand {

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

        if (event.getMessage().getMentions().getMembers().isEmpty()) {
            event.getChannel().sendMessage("❌ **Usage**: `!modlogs <@user>`").queue();
            return;
        }

        Member target = event.getMessage().getMentions().getMembers().get(0);
        ModerationManager.getInstance().getCaseHistory(event.getGuild().getIdLong(), target.getIdLong()).thenAccept(cases -> {
            event.getChannel().sendMessageEmbeds(buildModLogsEmbed(target, cases).build()).queue();
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

        OptionMapping userOpt = event.getOption("user");
        if (userOpt == null) {
            event.reply("❌ Please specify a user.").setEphemeral(true).queue();
            return;
        }

        Member target = userOpt.getAsMember();
        if (target == null) {
            event.reply("❌ Member not found.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        ModerationManager.getInstance().getCaseHistory(event.getGuild().getIdLong(), target.getIdLong()).thenAccept(cases -> {
            event.getHook().sendMessageEmbeds(buildModLogsEmbed(target, cases).build()).queue();
        });
    }

    private EmbedBuilder buildModLogsEmbed(Member target, List<ModCase> cases) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📋 Full Moderation History — " + target.getUser().getAsTag())
                .setColor(new Color(0x00A8FC))
                .setFooter("AshcrestHQ Aegis Moderation Engine", null)
                .setTimestamp(Instant.now());

        if (cases.isEmpty()) {
            embed.addField("Status", "Clean record! No moderation cases found for this user.", false);
        } else {
            StringBuilder sb = new StringBuilder();
            for (ModCase c : cases) {
                String activeTag = c.active() ? "🟢" : "⚪";
                sb.append(activeTag).append(" **Case #").append(c.id()).append("** [`").append(c.action()).append("`] — ")
                        .append(c.reason()).append(" (Mod: <@").append(c.moderatorId()).append(">)\n");
            }
            embed.addField("Case Log History (" + cases.size() + ")", sb.toString(), false);
        }

        return embed;
    }

    @Override
    public String getName() { return "modlogs"; }

    @Override
    public String getDescription() { return "View complete moderation case log history for a server member"; }

    @Override
    public String getUsage() { return "!modlogs <@user> or /modlogs user:<user>"; }
}
