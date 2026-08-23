package org.ashcrest.commands;

import com.vdurmont.emoji.EmojiParser;
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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuoteCommand implements ICommand {

    private static final int EMOJI_SIZE = 32;

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
        int minHeight = 300;

        String rawContent = message.getContentDisplay();
        if (rawContent.isEmpty() && !message.getAttachments().isEmpty()) {
            rawContent = "[Attachment]";
        }
        
        List<RenderElement> elements = parseElements("“" + rawContent + "”", message);

        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempG = tempImage.createGraphics();
        tempG.setFont(new Font("SansSerif", Font.ITALIC, 28));
        FontMetrics fm = tempG.getFontMetrics();
        
        int textX = 190;
        int maxWidth = width - textX - 40;
        
        List<List<RenderElement>> lines = wrapElements(elements, fm, maxWidth);
        
        int textHeight = lines.size() * (fm.getHeight() + 10);
        int height = Math.max(minHeight, textHeight + 100);
        tempG.dispose();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2d.setColor(new Color(24, 25, 28));
        g2d.fillRect(0, 0, width, height);

        // Draw Avatar
        String avatarUrl = message.getAuthor().getEffectiveAvatarUrl() + "?size=256";
        BufferedImage avatar = null;
        try {
            avatar = ImageIO.read(URI.create(avatarUrl).toURL());
        } catch (Exception ignored) {}
        
        int avatarSize = 120;
        int avatarX = 40;
        int avatarY = (height - avatarSize) / 2;

        if (avatar != null) {
            g2d.setClip(new Ellipse2D.Float(avatarX, avatarY, avatarSize, avatarSize));
            g2d.drawImage(avatar, avatarX, avatarY, avatarSize, avatarSize, null);
            g2d.setClip(null);
        }

        // Draw Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 28));

        int currentY = (height - textHeight) / 2 + fm.getAscent() - 20;
        
        for (List<RenderElement> line : lines) {
            int currentX = textX;
            for (RenderElement el : line) {
                if (el.isText) {
                    g2d.drawString(el.content, currentX, currentY);
                    currentX += fm.stringWidth(el.content);
                } else {
                    try {
                        BufferedImage emj = ImageIO.read(URI.create(el.content).toURL());
                        if (emj != null) {
                            g2d.drawImage(emj, currentX, currentY - EMOJI_SIZE + fm.getDescent() + 4, EMOJI_SIZE, EMOJI_SIZE, null);
                        }
                    } catch (Exception ignored) {}
                    currentX += EMOJI_SIZE + 4;
                }
            }
            currentY += fm.getHeight() + 10;
        }

        // Draw Author Name
        g2d.setColor(new Color(185, 187, 190));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        String authorName = "— " + message.getAuthor().getEffectiveName();
        g2d.drawString(authorName, textX, currentY + 10);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    private List<RenderElement> parseElements(String text, Message message) {
        List<RenderElement> elements = new ArrayList<>();
        
        // Custom Emojis in Discord format <:name:id> or <a:name:id>
        Pattern customEmojiPattern = Pattern.compile("<a?:([^:]+):([0-9]+)>");
        Matcher m = customEmojiPattern.matcher(text);
        
        int lastIndex = 0;
        while (m.find()) {
            if (m.start() > lastIndex) {
                String sub = text.substring(lastIndex, m.start());
                parseUnicodeEmojis(sub, elements);
            }
            String id = m.group(2);
            String ext = m.group().startsWith("<a:") ? "gif" : "png";
            elements.add(new RenderElement(false, "https://cdn.discordapp.com/emojis/" + id + "." + ext));
            lastIndex = m.end();
        }
        
        if (lastIndex < text.length()) {
            parseUnicodeEmojis(text.substring(lastIndex), elements);
        }
        
        return elements;
    }
    
    private void parseUnicodeEmojis(String text, List<RenderElement> elements) {
        String htmlText = EmojiParser.parseToHtmlHexadecimal(text);
        Pattern hexPattern = Pattern.compile("&#x([0-9a-fA-F]+);");
        Matcher m = hexPattern.matcher(htmlText);
        
        int lastIndex = 0;
        while (m.find()) {
            if (m.start() > lastIndex) {
                elements.add(new RenderElement(true, htmlText.substring(lastIndex, m.start())));
            }
            
            String hex = m.group(1).toLowerCase();
            String url = "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/" + hex + ".png";
            elements.add(new RenderElement(false, url));
            
            lastIndex = m.end();
        }
        
        if (lastIndex < htmlText.length()) {
            elements.add(new RenderElement(true, htmlText.substring(lastIndex)));
        }
    }

    private List<List<RenderElement>> wrapElements(List<RenderElement> elements, FontMetrics fm, int maxWidth) {
        List<List<RenderElement>> lines = new ArrayList<>();
        List<RenderElement> currentLine = new ArrayList<>();
        int currentWidth = 0;

        for (RenderElement el : elements) {
            if (el.isText) {
                String[] words = el.content.split(" ", -1);
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    int wWidth = fm.stringWidth(word);
                    
                    if (currentWidth + wWidth > maxWidth && !currentLine.isEmpty()) {
                        lines.add(new ArrayList<>(currentLine));
                        currentLine.clear();
                        currentWidth = 0;
                    }
                    
                    if (!word.isEmpty()) {
                        currentLine.add(new RenderElement(true, word));
                        currentWidth += wWidth;
                    }
                    
                    // Add space width back except for the last element
                    if (i < words.length - 1) {
                        currentLine.add(new RenderElement(true, " "));
                        currentWidth += fm.stringWidth(" ");
                    }
                }
            } else {
                if (currentWidth + EMOJI_SIZE + 4 > maxWidth && !currentLine.isEmpty()) {
                    lines.add(new ArrayList<>(currentLine));
                    currentLine.clear();
                    currentWidth = 0;
                }
                currentLine.add(el);
                currentWidth += EMOJI_SIZE + 4;
            }
        }
        
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }
        
        return lines;
    }

    @Override
    public String getName() { return "quote"; }

    @Override
    public String getDescription() { return "Quote a message as an image"; }

    @Override
    public String getUsage() { return "!quote (while replying) or /quote message_id:<id>"; }

    private static class RenderElement {
        boolean isText;
        String content;

        public RenderElement(boolean isText, String content) {
            this.isText = isText;
            this.content = content;
        }
    }
}
