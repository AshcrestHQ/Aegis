package org.ashcrest.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ashcrest.core.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.*;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Multilingual Translation Command powered by Gemini 3.6 Flash.
 */
public class TranslateCommand implements ICommand {
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length < 2) {
            event.getChannel().sendMessage("⚠️ Usage: `!translate <target_language> <text>`").queue();
            return;
        }

        String targetLang = args[0];
        String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String translated = translate(targetLang, text);
            sendEmbed(event.getChannel().asTextChannel(), targetLang, translated);
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping langOpt = event.getOption("target_lang");
        OptionMapping textOpt = event.getOption("text");

        if (langOpt == null || textOpt == null) {
            event.getHook().sendMessage("⚠️ Please specify both target language and text.").queue();
            return;
        }

        String targetLang = langOpt.getAsString();
        String text = textOpt.getAsString();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String translated = translate(targetLang, text);
            String prefix = "🌐 **Translation — " + targetLang + "**\n\n";
            org.ashcrest.core.MessageUtils.sendLongMessageSlash(event, prefix, translated);
        });
    }

    private String translate(String lang, String content) {
        String prompt = "Translate the following text accurately to " + lang + ":\n\n" + content;
        try {
            Map<String, Object> payloadMap = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String jsonPayload = objectMapper.writeValueAsString(payloadMap);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));

            String url = Config.GEMINI_API_URL + "?key=" + Config.GEMINI_API_KEY;
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return "❌ Translation error.";
                JsonNode root = objectMapper.readTree(response.body().string());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    private void sendEmbed(net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel, String lang, String text) {
        String prefix = "🌐 **Translation — " + lang + "**\n\n";
        org.ashcrest.core.MessageUtils.sendLongMessage(channel, prefix, text);
    }

    @Override
    public String getName() { return "translate"; }

    @Override
    public String getDescription() { return "Translate text into any target language via Gemini 3.6 Flash"; }

    @Override
    public String getUsage() { return "!translate <language> <text> or /translate target_lang:<string> text:<string>"; }
}
