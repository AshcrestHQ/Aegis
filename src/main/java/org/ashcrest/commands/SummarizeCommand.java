package org.ashcrest.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ashcrest.core.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Text & Article Summarization Command powered by Gemini 3.6 Flash.
 */
public class SummarizeCommand implements ICommand {
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("⚠️ Usage: `!summarize <text>`").queue();
            return;
        }

        String text = String.join(" ", args);
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String summary = summarize(text);
            sendEmbed(event.getChannel().asTextChannel(), summary);
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping opt = event.getOption("text");
        if (opt == null) {
            event.getHook().sendMessage("⚠️ Please provide text to summarize.").queue();
            return;
        }

        String text = opt.getAsString();
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String summary = summarize(text);
            String prefix = "📄 **AI Summary**\n\n";
            org.ashcrest.core.MessageUtils.sendLongMessageSlash(event, prefix, summary);
        });
    }

    private String summarize(String content) {
        String prompt = "Summarize the following text accurately and concisely:\n\n" + content;
        try {
            Map<String, Object> payloadMap = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String jsonPayload = objectMapper.writeValueAsString(payloadMap);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));

            String url = Config.GEMINI_API_URL + "?key=" + Config.GEMINI_API_KEY;
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return "❌ Summary error.";
                JsonNode root = objectMapper.readTree(response.body().string());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    private void sendEmbed(net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel, String summary) {
        String prefix = "📄 **AI Summary**\n\n";
        org.ashcrest.core.MessageUtils.sendLongMessage(channel, prefix, summary);
    }

    @Override
    public String getName() { return "summarize"; }

    @Override
    public String getDescription() { return "Summarize text or long articles into bulleted key points via Gemini 3.6 Flash"; }

    @Override
    public String getUsage() { return "!summarize <text> or /summarize text:<string>"; }
}
