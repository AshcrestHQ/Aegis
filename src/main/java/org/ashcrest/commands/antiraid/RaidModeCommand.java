package org.ashcrest.commands.antiraid;

import org.ashcrest.commands.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Anti-Raid Mode Toggle Command.
 */
public class RaidModeCommand implements ICommand {

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

        boolean enable = args.length > 0 && ("on".equalsIgnoreCase(args[0]) || "true".equalsIgnoreCase(args[0]));
        VerificationLevel level = enable ? VerificationLevel.VERY_HIGH : VerificationLevel.MEDIUM;

        event.getGuild().getManager().setVerificationLevel(level).queue(v -> {
            event.getChannel().sendMessage(enable
                    ? "🚨 **RAIDMODE ENABLED**: Server verification level raised to Highest (Phone Verification Required)."
                    : "🟢 **RAIDMODE DISABLED**: Server verification level restored to Normal.").queue();
        });
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

        OptionMapping enabledOpt = event.getOption("enabled");
        boolean enable = enabledOpt != null && enabledOpt.getAsBoolean();

        event.deferReply().queue();

        VerificationLevel level = enable ? VerificationLevel.VERY_HIGH : VerificationLevel.MEDIUM;
        event.getGuild().getManager().setVerificationLevel(level).queue(v -> {
            event.getHook().sendMessage(enable
                    ? "🚨 **RAIDMODE ENABLED**: Server verification level raised to Highest (Phone Verification Required)."
                    : "🟢 **RAIDMODE DISABLED**: Server verification level restored to Normal.").queue();
        });
    }

    @Override
    public String getName() { return "raidmode"; }

    @Override
    public String getDescription() { return "Temporarily lock server invite verification level during raid attacks"; }

    @Override
    public String getUsage() { return "!raidmode on|off or /raidmode enabled:<boolean>"; }
}
