package org.ashcrest.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Fun Utility Commands (Coinflip, Roll, 8Ball, Joke, Meme).
 */
public class FunCommands implements ICommand {
    private static final Random random = new Random();
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> EIGHT_BALL_ANSWERS = List.of(
            "It is certain.", "It is decidedly so.", "Without a doubt.", "Yes definitely.",
            "You may rely on it.", "As I see it, yes.", "Most likely.", "Outlook good.", "Yes.",
            "Reply hazy, try again.", "Ask again later.", "Better not tell you now.", "Cannot predict now.",
            "Concentrate and ask again.", "Don't count on it.", "My reply is no.", "My sources say no.",
            "Outlook not so good.", "Very doubtful."
    );

    private static final List<String> JOKES = List.of(
            "Why do programmers prefer dark mode? Because light attracts bugs!",
            "There are 10 types of people in the world: those who understand binary, and those who don't.",
            "A SQL query walks into a bar, walks up to two tables and asks: 'Can I join you?'",
            "Why was the JavaScript developer sad? Because he didn't Know how to 'null' his feelings.",
            "An optimist says the glass is half full. A pessimist says the glass is half empty. A programmer says the glass is twice as large as necessary."
    );

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String cmd = getName();
        switch (cmd) {
            case "coinflip" -> {
                boolean heads = random.nextBoolean();
                event.getChannel().sendMessage("🪙 Coinflip Result: **" + (heads ? "HEADS" : "TAILS") + "**!").queue();
            }
            case "roll" -> {
                int sides = 6;
                if (args.length > 0) {
                    try { sides = Math.max(2, Integer.parseInt(args[0])); } catch (Exception ignored) {}
                }
                int roll = random.nextInt(sides) + 1;
                event.getChannel().sendMessage("🎲 You rolled a **" + roll + "** (1-" + sides + ")!").queue();
            }
            case "8ball" -> {
                if (args.length == 0) {
                    event.getChannel().sendMessage("🔮 Ask the Magic 8-Ball a question! Usage: `!8ball <question>`").queue();
                    return;
                }
                String ans = EIGHT_BALL_ANSWERS.get(random.nextInt(EIGHT_BALL_ANSWERS.size()));
                event.getChannel().sendMessage("🔮 **Magic 8-Ball**: `" + ans + "`").queue();
            }
            case "joke" -> {
                String joke = JOKES.get(random.nextInt(JOKES.size()));
                event.getChannel().sendMessage("😄 " + joke).queue();
            }
            case "meme" -> fetchMemeAndSend(
                    embed -> event.getChannel().sendMessageEmbeds(embed.build()).queue(),
                    () -> event.getChannel().sendMessage("⚠️ Couldn't fetch a meme right now, try again.").queue()
            );
        }
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        switch (cmd) {
            case "coinflip" -> {
                boolean heads = random.nextBoolean();
                event.reply("🪙 Coinflip Result: **" + (heads ? "HEADS" : "TAILS") + "**!").queue();
            }
            case "roll" -> {
                OptionMapping sidesOpt = event.getOption("sides");
                int sides = sidesOpt != null ? Math.max(2, sidesOpt.getAsInt()) : 6;
                int roll = random.nextInt(sides) + 1;
                event.reply("🎲 You rolled a **" + roll + "** (1-" + sides + ")!").queue();
            }
            case "8ball" -> {
                OptionMapping qOpt = event.getOption("question");
                String q = qOpt != null ? qOpt.getAsString() : "the future";
                String ans = EIGHT_BALL_ANSWERS.get(random.nextInt(EIGHT_BALL_ANSWERS.size()));
                event.reply("🔮 **Question**: `" + q + "`\n🔮 **Magic 8-Ball**: `" + ans + "`").queue();
            }
            case "joke" -> {
                String joke = JOKES.get(random.nextInt(JOKES.size()));
                event.reply("😄 " + joke).queue();
            }
            case "meme" -> {
                event.deferReply().queue();
                fetchMemeAndSend(
                        embed -> event.getHook().sendMessageEmbeds(embed.build()).queue(),
                        () -> event.getHook().sendMessage("⚠️ Couldn't fetch a meme right now, try again.").queue()
                );
            }
        }
    }

    private void fetchMemeAndSend(Consumer<EmbedBuilder> callback, Runnable onError) {
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            try {
                Request request = new Request.Builder()
                        .url("https://meme-api.com/gimme/ProgrammerHumor")
                        .header("User-Agent", "AshcrestAegis/3.0")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonNode json = objectMapper.readTree(response.body().string());
                        String title = json.path("title").asText("Cyber Meme");
                        String imageUrl = json.path("url").asText();
                        String postLink = json.path("postLink").asText("https://reddit.com/r/ProgrammerHumor");
                        String author = json.path("author").asText("Reddit");
                        String subreddit = json.path("subreddit").asText("ProgrammerHumor");

                        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setTitle("🎭 " + title, postLink)
                                    .setImage(imageUrl)
                                    .setColor(new Color(0x9B59B6))
                                    .setFooter("r/" + subreddit + " • Posted by u/" + author, null)
                                    .setTimestamp(Instant.now());
                            callback.accept(embed);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Meme API error: " + e.getMessage());
            }
            onError.run();
        });
    }

    @Override
    public String getName() { return "coinflip"; }

    @Override
    public String getDescription() { return "Flip a coin, roll dice, or ask Magic 8-Ball"; }

    @Override
    public String getUsage() { return "!coinflip | !roll <sides> | !8ball <question> | !joke | !meme"; }
}
