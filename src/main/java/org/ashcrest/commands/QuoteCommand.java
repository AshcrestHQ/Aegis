package org.ashcrest.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class QuoteCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        Message referencedMessage = event.getMessage().getMessageReference() != null ? event.getMessage().getMessageReference().resolve().complete() : null;
        if (referencedMessage == null) {
            event.getChannel().sendMessage("⚠️ Please reply to a message and type `!quote` to quote it.").queue();
            return;
        }

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            generateAndSendQuote(event.getChannel(), referencedMessage);
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("message_id");
        if (opt == null) {
            event.reply("⚠️ Please provide a message ID.").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue();
        event.getChannel().retrieveMessageById(opt.getAsString()).queue(message -> {
            Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
                generateAndSendQuoteSlash(event, message);
            });
        }, failure -> {
            event.getHook().sendMessage("⚠️ Could not find message with that ID.").queue();
        });
    }

    public void executeContext(MessageContextInteractionEvent event) {
        event.deferReply().queue();
        Message target = event.getTarget();
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            generateAndSendQuoteSlash(event, target);
        });
    }

    private void generateAndSendQuote(net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel, Message message) {
        try {
            byte[] imageBytes = createQuoteImage(message);
            channel.sendFiles(FileUpload.fromData(imageBytes, "quote.png")).queue();
        } catch (Exception e) {
            channel.sendMessage("❌ Failed to generate quote image.").queue();
            e.printStackTrace();
        }
    }

    private void generateAndSendQuoteSlash(net.dv8tion.jda.api.interactions.callbacks.IReplyCallback event, Message message) {
        try {
            byte[] imageBytes = createQuoteImage(message);
            event.getHook().sendFiles(FileUpload.fromData(imageBytes, "quote.png")).queue();
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Failed to generate quote image.").queue();
            e.printStackTrace();
        }
    }

    private byte[] createQuoteImage(Message message) throws Exception {
        int width = 800;
        int height = 300;
        
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2d.setColor(new Color(24, 25, 28)); // Discord dark theme color
        g2d.fillRect(0, 0, width, height);

        // Draw Avatar
        String avatarUrl = message.getAuthor().getEffectiveAvatarUrl() + "?size=256";
        BufferedImage avatar = ImageIO.read(new URL(avatarUrl));
        
        int avatarSize = 120;
        int avatarX = 40;
        int avatarY = (height - avatarSize) / 2;

        g2d.setClip(new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize));
        g2d.drawImage(avatar, avatarX, avatarY, avatarSize, avatarSize, null);
        g2d.setClip(null);

        // Draw Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 28));
        
        String text = "“" + message.getContentDisplay() + "”";
        int textX = 190;
        int textY = avatarY + 40;
        int maxWidth = width - textX - 40;

        List<String> lines = wrapText(text, g2d.getFontMetrics(), maxWidth);
        for (String line : lines) {
            g2d.drawString(line, textX, textY);
            textY += g2d.getFontMetrics().getHeight();
        }

        // Draw Author Name
        g2d.setColor(new Color(185, 187, 190));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        String authorName = "— " + message.getAuthor().getEffectiveName();
        g2d.drawString(authorName, textX, textY + 20);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    private List<String> wrapText(String text, FontMetrics metrics, int maxWidth) {
        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (metrics.stringWidth(currentLine + word) < maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word + " ");
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    @Override
    public String getName() { return "quote"; }

    @Override
    public String getDescription() { return "Quote a message as an image"; }

    @Override
    public String getUsage() { return "!quote (while replying) or /quote message_id:<id>"; }
}
