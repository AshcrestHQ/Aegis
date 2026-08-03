package org.ashcrest.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Interface contract for all Aegis commands.
 */
public interface ICommand {
    void execute(MessageReceivedEvent event, String[] args);
    void execute(SlashCommandInteractionEvent event);
    String getName();
    String getDescription();
    String getUsage();
    default boolean supportsSlash() { return true; }
}
