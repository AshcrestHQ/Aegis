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
 * Software Engineering & Coding Q&A Assistant Command.
 */
public class CodeCommand implements ICommand {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("⚠️ Usage: `!code <coding question or prompt>`").queue();
            return;
        }

        String question = String.join(" ", args);
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String answer = answerCodeQuestion(question);
            sendEmbed(event.getChannel().asTextChannel(), question, answer);
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping opt = event.getOption("question");
        if (opt == null) {
            event.getHook().sendMessage("⚠️ Please provide a coding question.").queue();
            return;
        }

        String question = opt.getAsString();
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            String answer = answerCodeQuestion(question);
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("💻 Coding & Engineering Q&A")
                    .setColor(new Color(0x9B59B6))
                    .addField("❓ Question", question.length() > 250 ? question.substring(0, 247) + "..." : question, false)
                    .setDescription(answer.length() > 3800 ? answer.substring(0, 3797) + "..." : answer)
                    .setFooter("Powered by Google Gemini 3.6 Flash | Software Engineering Q&A", null)
                    .setTimestamp(Instant.now());
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private String answerCodeQuestion(String q) {
        String prompt = "You are an expert Principal Software Architect. Answer the following programming and software design question with code snippets and best practices:\n\n" + q;
        try {
            Map<String, Object> payloadMap = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            String jsonPayload = objectMapper.writeValueAsString(payloadMap);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));

            String url = Config.GEMINI_API_URL + "?key=" + Config.GEMINI_API_KEY;
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return "❌ Coding assistant error.";
                JsonNode root = objectMapper.readTree(response.body().string());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    private void sendEmbed(net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel, String q, String answer) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("💻 Coding & Engineering Q&A")
                .setColor(new Color(0x9B59B6))
                .addField("❓ Question", q.length() > 250 ? q.substring(0, 247) + "..." : q, false)
                .setDescription(answer.length() > 3800 ? answer.substring(0, 3797) + "..." : answer)
                .setFooter("Powered by Google Gemini 3.6 Flash | Software Engineering Q&A", null)
                .setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public String getName() { return "code"; }

    @Override
    public String getDescription() { return "Ask programming, refactoring, and software design questions (Q&A mode)"; }

    @Override
    public String getUsage() { return "!code <question> or /code question:<string>"; }
}
