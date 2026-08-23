package org.ashcrest.commands.antiraid;

import org.ashcrest.db.GuildSettingsManager;
import org.ashcrest.moderation.ModerationService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service evaluating incoming chat messages against AutoMod rules.
 */
public class AutoModService {
    private static final Map<String, Long> userLastMessageTimes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> userMessageCounts = new ConcurrentHashMap<>();

    public static void processMessage(MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot() || event.getMember() == null) return;

        long guildId = event.getGuild().getIdLong();
        GuildSettingsManager.getInstance().getSettings(guildId).thenAccept(settings -> {
            if (!settings.automodEnabled()) return;

            String content = event.getMessage().getContentRaw();
            String lowerContent = content.toLowerCase();

            // 1. Check Banned Words
            if (settings.automodBannedWords() != null && !settings.automodBannedWords().trim().isEmpty()) {
                String[] bannedWords = settings.automodBannedWords().split(",");
                for (String word : bannedWords) {
                    String trimmed = word.trim().toLowerCase();
                    if (!trimmed.isEmpty() && lowerContent.contains(trimmed)) {
                        event.getMessage().delete().queue(null, throwable -> {});
                        event.getChannel().sendMessage("⚠️ " + event.getAuthor().getAsMention() + " Message deleted by AutoMod (Banned word detected).").queue();
                        ModerationService.executeWarn(event.getGuild(), event.getGuild().getSelfMember(), event.getMember(), "AutoMod: Used banned word `" + trimmed + "`");
                        return;
                    }
                }
            }

            // 2. Check Spam Rate Flood
            long now = System.currentTimeMillis();
            String userKey = guildId + ":" + event.getAuthor().getIdLong();
            long lastTime = userLastMessageTimes.getOrDefault(userKey, 0L);

            if (now - lastTime < 2000) {
                int count = userMessageCounts.getOrDefault(userKey, 0) + 1;
                userMessageCounts.put(userKey, count);

                if (count >= settings.automodSpamThreshold()) {
                    userMessageCounts.remove(userKey);
                    event.getMessage().delete().queue(null, throwable -> {});
                    event.getChannel().sendMessage("🚨 " + event.getAuthor().getAsMention() + " AutoMod detected chat flood/spam! Applying warning...").queue();
                    ModerationService.executeWarn(event.getGuild(), event.getGuild().getSelfMember(), event.getMember(), "AutoMod: Chat flood/spam flood detected");
                }
            } else {
                userLastMessageTimes.put(userKey, now);
                userMessageCounts.put(userKey, 1);
            }
        });
    }
}
