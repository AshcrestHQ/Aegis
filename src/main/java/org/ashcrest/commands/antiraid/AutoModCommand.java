package org.ashcrest.commands.antiraid;

import org.ashcrest.commands.ICommand;
import org.ashcrest.db.GuildSettingsManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * AutoMod Configuration Command.
 */
public class AutoModCommand implements ICommand {

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

        long guildId = event.getGuild().getIdLong();

        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!automod banned-words add <word>` | `!automod spam-threshold <int>`").queue();
            return;
        }

        if ("banned-words".equalsIgnoreCase(args[0]) && args.length >= 3) {
            String word = args[2].toLowerCase();
            GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
                String existing = settings.automodBannedWords() != null ? settings.automodBannedWords() : "";
                String updated = existing.isEmpty() ? word : existing + "," + word;
                GuildSettingsManager.getInstance().updateAutoModBannedWords(guildId, updated).thenAccept(ok -> {
                    event.getChannel().sendMessage(ok ? "✅ Banned word `" + word + "` added to AutoMod filter." : "❌ Failed to update AutoMod.").queue();
                });
            });
            return;
        }

        if ("spam-threshold".equalsIgnoreCase(args[0]) && args.length >= 2) {
            try {
                int threshold = Integer.parseInt(args[1]);
                GuildSettingsManager.getInstance().updateAutoModSpamThreshold(guildId, threshold).thenAccept(ok -> {
                    event.getChannel().sendMessage(ok ? "✅ AutoMod spam threshold set to **" + threshold + "** messages/2sec." : "❌ Failed to update threshold.").queue();
                });
                return;
            } catch (NumberFormatException ignored) {}
        }

        event.getChannel().sendMessage("❌ Usage: `!automod banned-words add <word>` | `!automod spam-threshold <int>`").queue();
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

        long guildId = event.getGuild().getIdLong();
        String sub = event.getSubcommandName();

        event.deferReply().queue();

        if ("banned-words".equalsIgnoreCase(sub)) {
            OptionMapping wordOpt = event.getOption("word");
            if (wordOpt != null) {
                String word = wordOpt.getAsString().toLowerCase();
                GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
                    String existing = settings.automodBannedWords() != null ? settings.automodBannedWords() : "";
                    String updated = existing.isEmpty() ? word : existing + "," + word;
                    GuildSettingsManager.getInstance().updateAutoModBannedWords(guildId, updated).thenAccept(ok -> {
                        event.getHook().sendMessage(ok ? "✅ Banned word `" + word + "` added to AutoMod filter." : "❌ Failed to update AutoMod.").queue();
                    });
                });
            }
        } else if ("spam-threshold".equalsIgnoreCase(sub)) {
            OptionMapping countOpt = event.getOption("count");
            if (countOpt != null) {
                int threshold = countOpt.getAsInt();
                GuildSettingsManager.getInstance().updateAutoModSpamThreshold(guildId, threshold).thenAccept(ok -> {
                    event.getHook().sendMessage(ok ? "✅ AutoMod spam threshold set to **" + threshold + "** messages/2sec." : "❌ Failed to update threshold.").queue();
                });
            }
        }
    }

    @Override
    public String getName() { return "automod"; }

    @Override
    public String getDescription() { return "Configure AutoMod banned words filter and spam rate limits"; }

    @Override
    public String getUsage() { return "!automod banned-words add <word> | !automod spam-threshold <int>"; }
}
