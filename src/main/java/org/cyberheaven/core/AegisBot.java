package org.cyberheaven.core;

import org.cyberheaven.commands.*;
import org.cyberheaven.db.DatabaseManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS
                )
                .setActivity(Activity.watching("CyberHeaven Security"))
                .addEventListeners(new CommandListener(this))
                .build();

        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new StatusCommand());
        registerCommand(new AuditCommand());
        registerCommand(new HelpCommand());
        registerCommand(new WikiAddCommand(dbManager));
        registerCommand(new CyberHeavenCommand());
        registerCommand(new SourceCommand());
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
                System.out.println("🛡️ Aegis Bot is online and guarding CyberHeaven!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, virtualThreadExecutor);
    }

    private void registerSlashCommandsWithJDA() {
        jda.updateCommands().addCommands(
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("help", "Display Aegis commands categorized by security, wiki, and system info"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("status", "Check Aegis bot system health, ping, and uptime"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("audit", "Audit server permissions, roles, and administrative risk levels"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("cyberheaven", "View official links and resources for CyberHeaven"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("source", "View open-source GitHub repository and MIT license info for Aegis"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("aegis", "View open-source GitHub repository and MIT license info for Aegis"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("wikiadd", "Add or update an entry in the community security wiki")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "keyword", "Wiki topic keyword", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "content", "Wiki content body", true)
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
