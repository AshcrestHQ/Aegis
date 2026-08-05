package org.ashcrest.core;

import org.ashcrest.commands.ICommand;
import org.ashcrest.commands.antiraid.AutoModService;
import org.ashcrest.db.GuildSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Event listener handling commands, automod, onboarding events, and audit logs on Java 26 Virtual Threads.
 */
public class CommandListener extends ListenerAdapter {
    private final AegisBot bot;

    public CommandListener(AegisBot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // AutoMod Processing
        AutoModService.processMessage(event);

        String content = event.getMessage().getContentRaw();
        String prefix = Config.BOT_PREFIX;

        if (!content.startsWith(prefix)) return;

        String[] parts = content.substring(prefix.length()).split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

        ICommand command = bot.getCommand(commandName);
        if (command != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    command.execute(event, args);
                } catch (Exception e) {
                    System.err.println("Error executing command [" + commandName + "]: " + e.getMessage());
                }
            }, bot.getVirtualThreadExecutor());
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String rawName = event.getName();
        final String commandName = "aegis".equalsIgnoreCase(rawName) ? "source" : rawName;
        ICommand command = bot.getCommand(commandName);

        if (command != null && command.supportsSlash()) {
            CompletableFuture.runAsync(() -> {
                try {
                    command.execute(event);
                } catch (Exception e) {
                    System.err.println("Error executing slash command [" + commandName + "]: " + e.getMessage());
                }
            }, bot.getVirtualThreadExecutor());
        } else {
            event.reply("❌ Command not found.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
            // 1. Autorole
            if (settings.autoroleId() > 0) {
                Role role = event.getGuild().getRoleById(settings.autoroleId());
                if (role != null) {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue(null, throwable -> {});
                }
            }

            // 2. Welcome Message
            if (settings.welcomeChannelId() > 0) {
                TextChannel welcomeChan = event.getGuild().getTextChannelById(settings.welcomeChannelId());
                if (welcomeChan != null && welcomeChan.canTalk()) {
                    String msg = settings.welcomeMessage()
                            .replace("{user}", event.getMember().getAsMention())
                            .replace("{server}", event.getGuild().getName());

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("👋 Welcome to " + event.getGuild().getName() + "!")
                            .setDescription(msg)
                            .setColor(new Color(0x00FFC8))
                            .setThumbnail(event.getUser().getEffectiveAvatarUrl())
                            .setFooter("AshcrestHQ Onboarding Gate", null)
                            .setTimestamp(Instant.now());

                    welcomeChan.sendMessageEmbeds(embed.build()).queue();
                }
            }

            // 3. Audit Log
            postAuditLog(event.getGuild(), settings.modLogChannelId(), new EmbedBuilder()
                    .setTitle("📥 Member Joined")
                    .setColor(Color.GREEN)
                    .addField("User", event.getUser().getAsTag() + " (`" + event.getUser().getId() + "`)", false)
                    .setTimestamp(Instant.now()));
        });
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        long guildId = event.getGuild().getIdLong();
        GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
            postAuditLog(event.getGuild(), settings.modLogChannelId(), new EmbedBuilder()
                    .setTitle("📤 Member Left")
                    .setColor(Color.RED)
                    .addField("User", event.getUser().getAsTag() + " (`" + event.getUser().getId() + "`)", false)
                    .setTimestamp(Instant.now()));
        });
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) return;
        long guildId = event.getGuild().getIdLong();
        GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
            postAuditLog(event.getGuild(), settings.modLogChannelId(), new EmbedBuilder()
                    .setTitle("✏️ Message Edited")
                    .setColor(Color.ORANGE)
                    .addField("Author", event.getAuthor().getAsTag(), true)
                    .addField("Channel", event.getChannel().getAsMention(), true)
                    .addField("Content", event.getMessage().getContentRaw(), false)
                    .setTimestamp(Instant.now()));
        });
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (!event.isFromGuild()) return;
        long guildId = event.getGuild().getIdLong();
        GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
            postAuditLog(event.getGuild(), settings.modLogChannelId(), new EmbedBuilder()
                    .setTitle("🗑️ Message Deleted")
                    .setColor(Color.RED)
                    .addField("Channel", event.getChannel().getAsMention(), true)
                    .addField("Message ID", event.getMessageId(), true)
                    .setTimestamp(Instant.now()));
        });
    }

    private void postAuditLog(net.dv8tion.jda.api.entities.Guild guild, long channelId, EmbedBuilder embed) {
        if (channelId <= 0) return;
        TextChannel logChannel = guild.getTextChannelById(channelId);
        if (logChannel != null && logChannel.canTalk()) {
            embed.setFooter("AshcrestHQ Audit Log", null);
            logChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    @Override
    public void onGuildVoiceUpdate(net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent event) {
        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            if (event.getChannelJoined() == null) {
                org.ashcrest.music.GuildMusicManager musicManager = org.ashcrest.music.PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
                musicManager.getScheduler().clearQueue();
                event.getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if ("aegis_verify".equalsIgnoreCase(componentId)) {
            if (!event.isFromGuild() || event.getMember() == null) return;
            long guildId = event.getGuild().getIdLong();
            GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
                if (settings.autoroleId() > 0) {
                    Role role = event.getGuild().getRoleById(settings.autoroleId());
                    if (role != null) {
                        event.getGuild().addRoleToMember(event.getMember(), role).queue(
                                v -> event.reply("✅ Human verification complete! Verified role assigned.").setEphemeral(true).queue(),
                                err -> event.reply("❌ Error assigning verified role.").setEphemeral(true).queue()
                        );
                        return;
                    }
                }
                event.reply("✅ Human verification complete!").setEphemeral(true).queue();
            });
            return;
        }

        if (componentId.startsWith("hack_")) {
            String[] parts = componentId.split(":");
            if (parts.length < 2) return;

            String action = parts[0];
            long targetUserId = Long.parseLong(parts[1]);

            if (event.getUser().getIdLong() != targetUserId) {
                event.reply("❌ Only the agent who initiated this cyber breach mission can select a tactic.").setEphemeral(true).queue();
                return;
            }

            event.deferEdit().queue();

            CompletableFuture.runAsync(() -> {
                java.util.Random rand = new java.util.Random();
                boolean success;
                long creditDelta;
                long xpDelta;
                String tacticName;

                switch (action) {
                    case "hack_brute" -> {
                        tacticName = "🔨 Brute Force SSH";
                        success = rand.nextInt(100) < 60;
                        creditDelta = success ? 100 : -30;
                        xpDelta = success ? 50 : 10;
                    }
                    case "hack_zeroday" -> {
                        tacticName = "⚡ Zero-Day Exploit";
                        success = rand.nextInt(100) < 45;
                        creditDelta = success ? 250 : -50;
                        xpDelta = success ? 120 : 15;
                    }
                    case "hack_phish" -> {
                        tacticName = "🎣 Social Engineering";
                        success = rand.nextInt(100) < 75;
                        creditDelta = success ? 60 : -10;
                        xpDelta = success ? 30 : 5;
                    }
                    default -> {
                        tacticName = "🕵️ Stealth Probe";
                        success = rand.nextInt(100) < 50;
                        creditDelta = success ? 180 : -40;
                        xpDelta = success ? 80 : 10;
                    }
                }

                org.ashcrest.db.EconomyManager.UserProfile updated = org.ashcrest.db.EconomyManager.getInstance()
                        .recordHackOutcome(targetUserId, event.getUser().getEffectiveName(), success, creditDelta, xpDelta).join();

                net.dv8tion.jda.api.EmbedBuilder embed = new net.dv8tion.jda.api.EmbedBuilder()
                        .setTitle(success ? "🟢 PENETRATION SUCCESSFUL" : "🔴 BREACH FAILED — TRACE DETECTED")
                        .setColor(success ? new java.awt.Color(0x57F287) : new java.awt.Color(0xED4245))
                        .setDescription(success
                                ? "Agent **" + event.getUser().getEffectiveName() + "** executed **" + tacticName + "** and breached the mainframe root core!"
                                : "Agent **" + event.getUser().getEffectiveName() + "** used **" + tacticName + "** but automated countermeasures trace-busted your connection!")
                        .addField("📊 Mission Outcome", success ? "✅ SYSTEM OVERRIDDEN" : "🚨 ALERT LEVEL MAX", true)
                        .addField("🪙 Credits Gained", (creditDelta >= 0 ? "+" : "") + creditDelta + " 🪙", true)
                        .addField("⚡ XP Gained", "+" + xpDelta + " XP", true)
                        .addField("👤 Updated Vault Balance", "**" + updated.credits() + " 🪙** Credits (Level " + updated.level() + ")", false)
                        .setFooter("Ashcrest Aegis Cyber Operations", null)
                        .setTimestamp(java.time.Instant.now());

                event.getHook().editOriginalEmbeds(embed.build())
                        .setComponents()
                        .queue();
            }, bot.getVirtualThreadExecutor());
        }
    }

    @Override
    public void onStringSelectInteraction(net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent event) {
        if ("help_category_select".equalsIgnoreCase(event.getComponentId())) {
            String selected = event.getValues().get(0);
            EmbedBuilder embed = new EmbedBuilder().setTimestamp(Instant.now());

            switch (selected) {
                case "help_sec" -> embed.setTitle("🛡️ Security Recon & Audit Commands")
                        .setColor(new Color(0x00A8FC))
                        .addField("`/dnsscan <domain>`", "Audit DNS A, AAAA, MX, NS, TXT, SPF (`v=spf1`), DMARC (`_dmarc`), and DNSSEC (`DNSKEY`)", false)
                        .addField("`/headers <url>`", "Audit HTTP security response headers (CSP, HSTS, XFO, XCTO, Referrer-Policy)", false)
                        .addField("`/ssl <domain>`", "Inspect TLS/SSL certificate CN, issuer, valid dates, and days remaining", false)
                        .addField("`/securitytxt <domain>`", "Check RFC 9116 security vulnerability policy (`/.well-known/security.txt`)", false)
                        .addField("`/hashid <hash>`", "Identify probable hash algorithm (MD5, SHA-1, SHA-256, bcrypt, etc.)", false)
                        .addField("`/iplookup <ip>`", "Passive IP geolocation and ASN provider registry lookup", false)
                        .addField("`/passwordstrength`", "Ephemerally test password bit entropy and brute-force resistance (never logged)", false)
                        .addField("`/breach <email>`", "Check if an email address has been compromised in data breaches (HIBP API)", false)
                        .addField("`/blacklist <ip|domain>`", "Perform passive DNSBL blacklist lookup against Spamhaus & RBLs", false)
                        .addField("`/phishcheck <url>`", "Check URL against Google Safe Browsing threat list", false)
                        .addField("`/cve <id>`", "Lookup CVE vulnerability details, CVSS scores, and NVD report", false)
                        .addField("`/subdomains <domain>`", "Enumerate subdomains passively via Certificate Transparency logs (crt.sh)", false)
                        .addField("`/audit`", "Audit server roles, administrator privileges, and risk misconfigurations", false);

                case "help_mod" -> embed.setTitle("🔨 Moderation Engine Commands")
                        .setColor(new Color(0xED4245))
                        .addField("`/warn <@user> <reason>`", "Issue a formal warning (3 active warnings = auto 1-hr timeout)", false)
                        .addField("`/warnings <@user>`", "View active warning logs or delete warning by case ID (`/delwarn`)", false)
                        .addField("`/mute <@user> <minutes> <reason>`", "Timeout member natively in Discord up to 28 days", false)
                        .addField("`/unmute <@user>`", "Remove member timeout", false)
                        .addField("`/kick <@user> <reason>`", "Kick member from server with DM notice", false)
                        .addField("`/ban <@user> <reason>`", "Ban user from server or unban by user ID (`/unban`)", false)
                        .addField("`/modlogs <@user>`", "View complete case history log for a member", false)
                        .addField("`/purge <count>`", "Bulk delete up to 100 recent channel messages", false)
                        .addField("`/slowmode <seconds>`", "Set channel message slowmode delay in seconds", false)
                        .addField("`/lockdown` / `/unlock`", "Restrict or restore message sending permissions for @everyone", false);

                case "help_raid" -> embed.setTitle("🚨 Anti-Raid & Onboarding Commands")
                        .setColor(new Color(0xFF6B00))
                        .addField("`/raidmode <on|off>`", "Lock verification level to Highest during active raid attacks", false)
                        .addField("`/verify`", "Post button-based human verification gate for new joins", false)
                        .addField("`/automod`", "Manage banned words filter and chat spam rate limits", false)
                        .addField("`/report <@user> <reason>`", "Report user to moderators via moderation log channel", false)
                        .addField("`/config`", "Configure modlog, welcome channel/message, autorole, and automod settings", false);

                case "help_ai" -> embed.setTitle("🤖 AI Assistant Suite (Google Gemini 3.6 Flash)")
                        .setColor(new Color(0x4285F4))
                        .addField("`/ask <prompt>`", "Stateless single-shot Q&A assistant", false)
                        .addField("`/chat <message>`", "Stateful multi-turn conversation maintaining channel context", false)
                        .addField("`/summarize <text>`", "Summarize articles and long text into bulleted key takeaways", false)
                        .addField("`/translate <lang> <text>`", "Translate text into any target language", false)
                        .addField("`/explain <topic>`", "Explain complex technical concepts, errors, or code snippets", false)
                        .addField("`/code <question>`", "Software engineering and programming Q&A assistant", false)
                        .setFooter("Powered by Google Gemini 3.6 Flash", Config.GEMINI_LOGO_URL);

                case "help_rpg" -> embed.setTitle("🎮 Cyber RPG & Vault Economy")
                        .setColor(new Color(0x57F287))
                        .addField("`/hack`", "Interactive terminal penetration test mini-game against target mainframes", false)
                        .addField("`/profile`", "View hacker rank, level, XP progress bar, credits balance, and win rate", false)
                        .addField("`/leaderboard`", "Top 10 global/server hacker leaderboard with leader badges", false)
                        .addField("`/daily`", "Claim daily 250 🪙 Ashcrest credits reward", false);

                case "help_fun" -> embed.setTitle("🎉 Fun & Utility Commands")
                        .setColor(new Color(0x9B59B6))
                        .addField("`/coinflip`", "Flip a coin (Heads/Tails)", false)
                        .addField("`/roll <sides>`", "Roll a die with optional custom side count", false)
                        .addField("`/8ball <question>`", "Ask the Magic 8-Ball a question", false)
                        .addField("`/joke`", "Get a programming joke", false)
                        .addField("`/meme`", "Get a cyber meme", false);

                case "help_music" -> embed.setTitle("🎵 VC Music Audio Commands")
                        .setColor(new Color(0x1DB954))
                        .addField("`/play <input>`", "Play audio (supports YouTube multi-client fallback, Apple Music metadata, direct URLs)", false)
                        .addField("`/skip`", "Skip current playing track", false)
                        .addField("`/stop`", "Stop playback, clear queue, and disconnect bot from VC", false)
                        .addField("`/queue`", "View current queue list and now playing track", false)
                        .addField("`/nowplaying`", "Display progress bar and details of current track", false)
                        .addField("`/pause` / `/resume`", "Pause or resume audio playback", false);

                default -> embed.setTitle("ℹ️ System & Info Commands")
                        .setColor(new Color(0x00A8FC))
                        .addField("`/status`", "View gateway ping, uptime, Java 26 status, and system health", false)
                        .addField("`/ashcrest`", "View official AshcrestHQ community links, bot invite, and repo info", false)
                        .addField("`/source`", "View open-source GitHub repository details (`AshcrestHQ/Aegis`)", false)
                        .addField("`/cyberheaven`", "Legacy community alias (use `/ashcrest` or `/source`)", false);
            }

            embed.setFooter("AshcrestHQ Aegis Master Command Directory", null);

            event.editMessageEmbeds(embed.build())
                    .setComponents(ActionRow.of(org.ashcrest.commands.HelpCommand.buildCategorySelectMenu()))
                    .queue();
        }
    }
}
