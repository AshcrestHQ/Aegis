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
    private static final OkHttpClient httpClient = new OkHttpClient();
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
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🌐 Translation — " + targetLang)
                    .setColor(new Color(0x00A8FC))
                    .setDescription(translated)
                    .setFooter("Powered by Google Gemini 3.6 Flash", null)
                    .setTimestamp(Instant.now());
            event.getHook().sendMessageEmbeds(embed.build()).queue();
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
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🌐 Translation — " + lang)
                .setColor(new Color(0x00A8FC))
                .setDescription(text)
                .setFooter("Powered by Google Gemini 3.6 Flash", null)
                .setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public String getName() { return "translate"; }

    @Override
    public String getDescription() { return "Translate text into any target language via Gemini 3.6 Flash"; }

    @Override
    public String getUsage() { return "!translate <language> <text> or /translate target_lang:<string> text:<string>"; }
}
