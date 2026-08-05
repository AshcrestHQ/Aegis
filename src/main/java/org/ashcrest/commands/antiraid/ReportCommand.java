package org.ashcrest.commands.antiraid;

import org.ashcrest.commands.ICommand;
import org.ashcrest.db.GuildSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;

/**
 * User Report Command.
 */
public class ReportCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ Command can only be run in a server.").queue();
            return;
        }

        if (event.getMessage().getMentions().getMembers().isEmpty() || args.length < 2) {
            event.getChannel().sendMessage("❌ **Usage**: `!report <@user> <reason>`").queue();
            return;
        }

        Member target = event.getMessage().getMentions().getMembers().get(0);
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        sendReport(event.getGuild(), event.getMember(), target, reason);
        event.getChannel().sendMessage("✅ Report submitted to server moderators.").queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ Command can only be run in a server.").setEphemeral(true).queue();
            return;
        }

        OptionMapping userOpt = event.getOption("user");
        OptionMapping reasonOpt = event.getOption("reason");

        if (userOpt == null || reasonOpt == null) {
            event.reply("❌ Please provide both user and reason.").setEphemeral(true).queue();
            return;
        }

        Member target = userOpt.getAsMember();
        String reason = reasonOpt.getAsString();

        if (target == null) {
            event.reply("❌ Member not found.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().setEphemeral(true).queue();

        sendReport(event.getGuild(), event.getMember(), target, reason);
        event.getHook().sendMessage("✅ Report submitted to server moderators.").queue();
    }

    private void sendReport(net.dv8tion.jda.api.entities.Guild guild, Member reporter, Member target, String reason) {
        GuildSettingsManager.getInstance().getSettings(guild.getIdLong()).thenAccept(settings -> {
            if (settings.modLogChannelId() <= 0) return;

            TextChannel logChannel = guild.getTextChannelById(settings.modLogChannelId());
            if (logChannel == null || !logChannel.canTalk()) return;

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🚨 Member Report Received")
                    .setColor(Color.ORANGE)
                    .addField("👤 Reported User", target.getAsMention() + " (`" + target.getId() + "`)", true)
                    .addField("🚩 Reported By", reporter.getAsMention() + " (`" + reporter.getId() + "`)", true)
                    .addField("📄 Reason", reason, false)
                    .setFooter("AshcrestHQ Aegis Security Desk", null)
                    .setTimestamp(Instant.now());

            logChannel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public String getName() { return "report"; }

    @Override
    public String getDescription() { return "Report a user to server moderators"; }

    @Override
    public String getUsage() { return "!report <@user> <reason> or /report user:<user> reason:<text>"; }
}
