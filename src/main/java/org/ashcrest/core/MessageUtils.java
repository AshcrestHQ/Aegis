package org.ashcrest.core;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    public static void sendLongMessage(MessageChannel channel, String prefix, String content) {
        List<String> chunks = splitMessage(prefix + content);
        for (String chunk : chunks) {
            channel.sendMessage(chunk).queue();
        }
    }

    public static void sendLongMessageSlash(IReplyCallback event, String prefix, String content) {
        List<String> chunks = splitMessage(prefix + content);
        if (chunks.isEmpty()) return;

        event.getHook().sendMessage(chunks.get(0)).queue();
        
        for (int i = 1; i < chunks.size(); i++) {
            if (event.getChannel() instanceof net.dv8tion.jda.api.entities.channel.middleman.MessageChannel mc) {
                mc.sendMessage(chunks.get(i)).queue();
            }
        }
    }

    private static List<String> splitMessage(String text) {
        List<String> chunks = new ArrayList<>();
        int maxLength = 1950; // Leave some buffer for Discord's 2000 char limit

        while (text.length() > maxLength) {
            int splitIndex = text.lastIndexOf('\n', maxLength);
            if (splitIndex == -1 || splitIndex < maxLength - 500) {
                splitIndex = text.lastIndexOf(' ', maxLength);
            }
            if (splitIndex == -1) {
                splitIndex = maxLength;
            }

            chunks.add(text.substring(0, splitIndex).trim());
            text = text.substring(splitIndex).trim();
        }
        
        if (!text.isEmpty()) {
            chunks.add(text);
        }
        
        return chunks;
    }
}
