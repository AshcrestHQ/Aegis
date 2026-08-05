package org.ashcrest.commands;

import org.ashcrest.db.GuildSettingsManager;
import org.ashcrest.db.GuildSettingsManager.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;

/**
 * Server Configuration Command for managing per-guild settings.
 */
public class ConfigCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ This command can only be run within a server.").queue();
            return;
        }

        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.MANAGE_SERVER)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `Manage Server` permission to configure Aegis.").queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();

        if (args.length == 0 || "view".equalsIgnoreCase(args[0])) {
            GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
                event.getChannel().sendMessageEmbeds(buildConfigEmbed(event.getGuild().getName(), settings).build()).queue();
            });
            return;
        }

        String sub = args[0].toLowerCase();

        if ("modlog".equalsIgnoreCase(sub) && args.length >= 2) {
            if (!event.getMessage().getMentions().getChannels().isEmpty() && event.getMessage().getMentions().getChannels().get(0) instanceof TextChannel tc) {
                GuildSettingsManager.getInstance().updateModLogChannel(guildId, tc.getIdLong()).thenAccept(ok -> {
                    event.getChannel().sendMessage(ok ? "✅ Moderation log channel set to " + tc.getAsMention() : "❌ Failed to save configuration.").queue();
                });
                return;
            }
        }

        if ("welcome-channel".equalsIgnoreCase(sub) && args.length >= 2) {
            if (!event.getMessage().getMentions().getChannels().isEmpty() && event.getMessage().getMentions().getChannels().get(0) instanceof TextChannel tc) {
                GuildSettingsManager.getInstance().updateWelcomeChannel(guildId, tc.getIdLong()).thenAccept(ok -> {
                    event.getChannel().sendMessage(ok ? "✅ Welcome channel set to " + tc.getAsMention() : "❌ Failed to save configuration.").queue();
                });
                return;
            }
        }

        if ("welcome-message".equalsIgnoreCase(sub) && args.length >= 2) {
            String msg = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            GuildSettingsManager.getInstance().updateWelcomeMessage(guildId, msg).thenAccept(ok -> {
                event.getChannel().sendMessage(ok ? "✅ Welcome message set to: `" + msg + "`" : "❌ Failed to save configuration.").queue();
            });
            return;
        }

        if ("autorole".equalsIgnoreCase(sub) && args.length >= 2) {
            if (!event.getMessage().getMentions().getRoles().isEmpty()) {
                Role role = event.getMessage().getMentions().getRoles().get(0);
                GuildSettingsManager.getInstance().updateAutorole(guildId, role.getIdLong()).thenAccept(ok -> {
                    event.getChannel().sendMessage(ok ? "✅ Autorole set to **" + role.getName() + "**" : "❌ Failed to save configuration.").queue();
                });
                return;
            }
        }

        if ("automod".equalsIgnoreCase(sub) && args.length >= 2) {
            boolean enable = "on".equalsIgnoreCase(args[1]) || "true".equalsIgnoreCase(args[1]);
            GuildSettingsManager.getInstance().updateAutoMod(guildId, enable).thenAccept(ok -> {
                event.getChannel().sendMessage(ok ? "✅ AutoMod is now " + (enable ? "**ENABLED**" : "**DISABLED**") : "❌ Failed to save configuration.").queue();
            });
            return;
        }

        event.getChannel().sendMessage("❌ Usage: `!config view` | `!config modlog #channel` | `!config welcome-channel #channel` | `!config welcome-message <text>` | `!config autorole @role` | `!config automod on|off`").queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ This command can only be run within a server.").setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        if (member == null || !member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("❌ **Access Denied**: You require `Manage Server` permission to configure Aegis.").setEphemeral(true).queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        String subcommand = event.getSubcommandName();

        event.deferReply().queue();

        if (subcommand == null || "view".equalsIgnoreCase(subcommand)) {
            GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
                event.getHook().sendMessageEmbeds(buildConfigEmbed(event.getGuild().getName(), settings).build()).queue();
            });
            return;
        }

        switch (subcommand) {
            case "modlog" -> {
                OptionMapping opt = event.getOption("channel");
                if (opt != null && opt.getAsChannel() instanceof TextChannel channel) {
                    GuildSettingsManager.getInstance().updateModLogChannel(guildId, channel.getIdLong()).thenAccept(ok -> {
                        event.getHook().sendMessage(ok ? "✅ Moderation log channel set to " + channel.getAsMention() : "❌ Failed to save configuration.").queue();
                    });
                }
            }
            case "welcome-channel" -> {
                OptionMapping opt = event.getOption("channel");
                if (opt != null && opt.getAsChannel() instanceof TextChannel channel) {
                    GuildSettingsManager.getInstance().updateWelcomeChannel(guildId, channel.getIdLong()).thenAccept(ok -> {
                        event.getHook().sendMessage(ok ? "✅ Welcome channel set to " + channel.getAsMention() : "❌ Failed to save configuration.").queue();
                    });
                }
            }
            case "welcome-message" -> {
                OptionMapping opt = event.getOption("message");
                if (opt != null) {
                    String msg = opt.getAsString();
                    GuildSettingsManager.getInstance().updateWelcomeMessage(guildId, msg).thenAccept(ok -> {
                        event.getHook().sendMessage(ok ? "✅ Welcome message set to: `" + msg + "`" : "❌ Failed to save configuration.").queue();
                    });
                }
            }
            case "autorole" -> {
                OptionMapping opt = event.getOption("role");
                if (opt != null) {
                    Role role = opt.getAsRole();
                    GuildSettingsManager.getInstance().updateAutorole(guildId, role.getIdLong()).thenAccept(ok -> {
                        event.getHook().sendMessage(ok ? "✅ Autorole set to **" + role.getName() + "**" : "❌ Failed to save configuration.").queue();
                    });
                }
            }
            case "automod" -> {
                OptionMapping opt = event.getOption("enabled");
                if (opt != null) {
                    boolean enabled = opt.getAsBoolean();
                    GuildSettingsManager.getInstance().updateAutoMod(guildId, enabled).thenAccept(ok -> {
                        event.getHook().sendMessage(ok ? "✅ AutoMod is now " + (enabled ? "**ENABLED**" : "**DISABLED**") : "❌ Failed to save configuration.").queue();
                    });
                }
            }
            default -> event.getHook().sendMessage("❌ Unknown subcommand.").queue();
        }
    }

    private EmbedBuilder buildConfigEmbed(String serverName, GuildSettings s) {
        String modLog = s.modLogChannelId() > 0 ? "<#" + s.modLogChannelId() + ">" : "*Not Configured*";
        String welcomeChan = s.welcomeChannelId() > 0 ? "<#" + s.welcomeChannelId() + ">" : "*Not Configured*";
        String autorole = s.autoroleId() > 0 ? "<@&" + s.autoroleId() + ">" : "*Not Configured*";
        String automod = s.automodEnabled() ? "🟢 Enabled" : "🔴 Disabled";

        return new EmbedBuilder()
                .setTitle("⚙️ Aegis Guild Configuration — " + serverName)
                .setColor(new Color(0x00A8FC))
                .addField("📜 Moderation Log Channel", modLog, true)
                .addField("👋 Welcome Channel", welcomeChan, true)
                .addField("🏷️ Auto-Assigned Role", autorole, true)
                .addField("🛡️ AutoMod Status", automod, true)
                .addField("⚡ AutoMod Spam Threshold", s.automodSpamThreshold() + " msg/sec", true)
                .addField("💬 Effective Prefix", "`" + s.getEffectivePrefix() + "`", true)
                .addField("✉️ Welcome Message", "`" + s.welcomeMessage() + "`", false)
                .setFooter("AshcrestHQ Aegis Configuration Engine", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "config"; }

    @Override
    public String getDescription() { return "View and modify per-guild Aegis settings (Manage Server required)"; }

    @Override
    public String getUsage() { return "!config view | !config modlog #channel | !config welcome-channel #channel | !config welcome-message <text> | !config autorole @role | !config automod on|off"; }
}
