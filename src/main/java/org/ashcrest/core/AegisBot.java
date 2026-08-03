package org.ashcrest.core;

import org.ashcrest.commands.*;
import org.ashcrest.commands.music.*;
import org.ashcrest.db.DatabaseManager;
import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Aegis Bot core manager.
 */
public class AegisBot {
    private final JDA jda;
    private final Map<String, ICommand> commands = new HashMap<>();
    private final DatabaseManager dbManager;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public AegisBot() {
        Config.validate();
        this.dbManager = DatabaseManager.getInstance();

        this.jda = JDABuilder.createDefault(Config.DISCORD_TOKEN)
                .setAudioModuleConfig(
                        new AudioModuleConfig()
                                .withDaveSessionFactory(new JDaveSessionFactory())
                )
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .setActivity(Activity.watching("Ashcrest Operations"))
                .addEventListeners(new CommandListener(this))
                .build();

        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new StatusCommand());
        registerCommand(new AuditCommand());
        registerCommand(new HelpCommand());
        registerCommand(new WikiAddCommand(dbManager));
        registerCommand(new AshcrestCommand());
        registerCommand(new CyberHeavenCommand()); // Alias for backward compatibility
        registerCommand(new SourceCommand());
        registerCommand(new AskCommand());

        // Cyber Hack RPG Game Commands
        registerCommand(new HackCommand());
        registerCommand(new ProfileCommand());
        registerCommand(new LeaderboardCommand());
        registerCommand(new DailyCommand());

        // VC Music & Apple Music Commands
        registerCommand(new PlayCommand());
        registerCommand(new SkipCommand());
        StopCommand stopCmd = new StopCommand();
        registerCommand(stopCmd);
        commands.put("leave", stopCmd);
        registerCommand(new QueueCommand());
        registerCommand(new NowPlayingCommand());
        registerCommand(new PauseResumeCommand(true));
        registerCommand(new PauseResumeCommand(false));
    }

    private void registerCommand(ICommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public ICommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public Map<String, ICommand> getCommands() {
        return new HashMap<>(commands);
    }

    public ExecutorService getVirtualThreadExecutor() {
        return virtualThreadExecutor;
    }

    public CompletableFuture<Void> startAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                jda.awaitReady();
                registerSlashCommandsWithJDA();
                System.out.println("🛡️ Aegis Bot is online and guarding AshcrestHQ!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, virtualThreadExecutor);
    }

    private void registerSlashCommandsWithJDA() {
        jda.updateCommands().addCommands(
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("help", "Display Aegis commands categorized by security, game, wiki, and music"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("status", "Check Aegis bot system health, ping, and uptime"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("audit", "Audit server permissions, roles, and administrative risk levels"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("ashcrest", "View official links, public bot invite, and resources for AshcrestHQ"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("cyberheaven", "View official links for CyberHeaven (alias)"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("source", "View open-source GitHub repository and MIT license info for Aegis"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("aegis", "View open-source GitHub repository and MIT license info for Aegis"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("ask", "Ask Aegis AI (powered by Google Gemini 2.5 Flash)")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "prompt", "The question or prompt for Gemini AI", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("hack", "Initiate interactive cyber penetration test mini-game for credits & XP"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("profile", "View your Ashcrest hacker rank, level, XP, credits, and hack stats"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("leaderboard", "View top hacker leaderboard ranked by level and credits"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("daily", "Claim daily Ashcrest credits (250 🪙)"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("wikiadd", "Add or update an entry in the community security wiki")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "keyword", "Wiki topic keyword", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "content", "Wiki content body", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("play", "Play audio in your VC (supports Apple Music, YouTube, direct audio URLs)")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "input", "Song title, URL, or Apple Music link", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("skip", "Skip the currently playing track"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("stop", "Stop playback, clear queue, and disconnect Aegis from VC"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("queue", "Display current music queue and now playing track"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("nowplaying", "Show progress bar and details of currently playing track"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("pause", "Pause audio playback"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("resume", "Resume paused audio playback")
        ).queue();
    }

    public void shutdown() {
        if (virtualThreadExecutor != null && !virtualThreadExecutor.isShutdown()) {
            virtualThreadExecutor.shutdown();
        }
        if (dbManager != null) {
            dbManager.close();
        }
        if (jda != null) {
            jda.shutdown();
        }
        System.out.println("🔴 Aegis Bot shutdown complete");
    }
}
