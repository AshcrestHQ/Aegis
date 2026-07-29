package org.cyberheaven.core;

import org.cyberheaven.commands.ICommand;
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
        String commandName = event.getName();
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
}
