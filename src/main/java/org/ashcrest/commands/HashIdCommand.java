package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;

/**
 * Hash Algorithm Identification Command.
 */
public class HashIdCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!hashid <hash_string>`").queue();
            return;
        }

        String hash = args[0];
        event.getChannel().sendMessageEmbeds(identifyHash(hash).build()).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("hash");
        if (opt == null) {
            event.reply("❌ Please provide a hash.").setEphemeral(true).queue();
            return;
        }

        String hash = opt.getAsString();
        event.replyEmbeds(identifyHash(hash).build()).queue();
    }

    private EmbedBuilder identifyHash(String input) {
        String trimmed = input.trim();
        int len = trimmed.length();
        boolean isHex = trimmed.matches("^[a-fA-F0-9]+$");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔍 Hash Algorithm Identification")
                .setColor(new Color(0x00A8FC))
                .addField("🔑 Input Hash", "`" + (trimmed.length() > 50 ? trimmed.substring(0, 47) + "..." : trimmed) + "`", false)
                .addField("📏 Length", len + " characters", true)
                .setTimestamp(Instant.now());

        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) {
            embed.addField("💡 Likely Algorithm", "**bcrypt** Password Hash", true);
        } else if (trimmed.startsWith("$5$")) {
            embed.addField("💡 Likely Algorithm", "**SHA-256 Crypt** (Linux /etc/shadow)", true);
        } else if (trimmed.startsWith("$6$")) {
            embed.addField("💡 Likely Algorithm", "**SHA-512 Crypt** (Linux /etc/shadow)", true);
        } else if (trimmed.startsWith("$argon2")) {
            embed.addField("💡 Likely Algorithm", "**Argon2** Password Hash", true);
        } else if (isHex) {
            String algo = switch (len) {
                case 32 -> "**MD5** or **NTLM**";
                case 40 -> "**SHA-1** or **RIPEMD-160**";
                case 56 -> "**SHA-224** or **SHA3-224**";
                case 64 -> "**SHA-256** or **BLAKE2s** or **SHA3-256**";
                case 96 -> "**SHA-384** or **SHA3-384**";
                case 128 -> "**SHA-512** or **BLAKE2b** or **SHA3-512**";
                default -> "Unknown Hex Hash (" + len + " chars)";
            };
            embed.addField("💡 Likely Algorithm(s)", algo, true);
        } else {
            embed.addField("💡 Likely Algorithm", "Base64 or Custom Encrypted String", true);
        }

        embed.setFooter("AshcrestHQ Aegis Security Tools", null);
        return embed;
    }

    @Override
    public String getName() { return "hashid"; }

    @Override
    public String getDescription() { return "Identify probable cryptographic hash algorithm based on formatting and length"; }

    @Override
    public String getUsage() { return "!hashid <hash> or /hashid hash:<string>"; }
}
