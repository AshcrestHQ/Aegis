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
 * Text & Code Explanation Command powered by Gemini 3.6 Flash.
 */
public class ExplainCommand implements ICommand {
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("⚠️ Usage: `!explain <concept, text, or code>`").queue();
            return;
        }

        String query = String.join(" ", args);
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String explanation = explain(query);
            sendEmbed(event.getChannel().asTextChannel(), query, explanation);
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping opt = event.getOption("topic");
        if (opt == null) {
            event.getHook().sendMessage("⚠️ Please provide a topic or code snippet to explain.").queue();
            return;
        }

        String query = opt.getAsString();
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String explanation = explain(query);
            String prefix = "💡 **AI Concept & Code Explanation**\n\n**❓ Query:** `" + query + "`\n\n";
            org.ashcrest.core.MessageUtils.sendLongMessageSlash(event, prefix, explanation);
        });
    }

    private String explain(String topic) {
        String prompt = "Please explain the following concept or code clearly with breakdown and examples:\n\n" + topic;
        try {
            Map<String, Object> payloadMap = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String jsonPayload = objectMapper.writeValueAsString(payloadMap);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));

            String url = Config.GEMINI_API_URL + "?key=" + Config.GEMINI_API_KEY;
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return "❌ Explanation error.";
                JsonNode root = objectMapper.readTree(response.body().string());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    private void sendEmbed(net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel, String query, String explanation) {
        String prefix = "💡 **AI Concept & Code Explanation**\n\n**❓ Query:** `" + query + "`\n\n";
        org.ashcrest.core.MessageUtils.sendLongMessage(channel, prefix, explanation);
    }

    @Override
    public String getName() { return "explain"; }

    @Override
    public String getDescription() { return "Explain complex technical concepts, snippets, or errors via Gemini 3.6 Flash"; }

    @Override
    public String getUsage() { return "!explain <topic> or /explain topic:<string>"; }
}
