package org.ashcrest.core;

import org.ashcrest.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.CompletableFuture;

/**
 * Event listener handling commands on Java 26 Virtual Threads.
 */
public class CommandListener extends ListenerAdapter {
    private final AegisBot bot;

    public CommandListener(AegisBot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

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
    public void onGuildVoiceUpdate(net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent event) {
        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            if (event.getChannelJoined() == null) {
                // Bot disconnected from VC, stop reconnect loop & clear state
                org.ashcrest.music.GuildMusicManager musicManager = org.ashcrest.music.PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
                musicManager.getScheduler().clearQueue();
                event.getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

    @Override
    public void onButtonInteraction(net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
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
                    default -> { // hack_stealth
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
                        .setComponents() // Remove buttons after turn completes
                        .queue();
            }, bot.getVirtualThreadExecutor());
        }
    }
}
