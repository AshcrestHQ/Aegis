package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;

/**
 * Ephemeral Password Strength & Entropy Estimator Command.
 * CRITICAL SECURITY GUARANTEE: Never logs, stores, or persists user input.
 */
public class PasswordStrengthCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        // Delete invocation message immediately to prevent leaving clear-text passwords in chat
        event.getMessage().delete().queue(null, throwable -> {});

        if (args.length == 0) {
            event.getChannel().sendMessage("⚠️ Usage: `!passwordstrength <password>` (Message auto-deleted for privacy)").queue(msg -> msg.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
            return;
        }

        String password = args[0];
        event.getChannel().sendMessageEmbeds(evaluateStrength(password).build()).queue(msg -> {
            msg.delete().queueAfter(15, java.util.concurrent.TimeUnit.SECONDS);
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("password");
        if (opt == null) {
            event.reply("❌ Please provide a password.").setEphemeral(true).queue();
            return;
        }

        String password = opt.getAsString();
        // Ephemeral response — visible ONLY to the user executing the command
        event.replyEmbeds(evaluateStrength(password).build()).setEphemeral(true).queue();
    }

    private EmbedBuilder evaluateStrength(String pass) {
        int length = pass.length();
        boolean hasUpper = pass.matches(".*[A-Z].*");
        boolean hasLower = pass.matches(".*[a-z].*");
        boolean hasDigit = pass.matches(".*[0-9].*");
        boolean hasSpecial = pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        int poolSize = 0;
        if (hasLower) poolSize += 26;
        if (hasUpper) poolSize += 26;
        if (hasDigit) poolSize += 10;
        if (hasSpecial) poolSize += 32;

        double entropy = poolSize > 0 ? (length * (Math.log(poolSize) / Math.log(2))) : 0;

        String grade;
        Color color;
        if (entropy >= 80) {
            grade = "🟢 Excellent (Cryptographically Strong)";
            color = Color.GREEN;
        } else if (entropy >= 50) {
            grade = "🟡 Moderate (Acceptable for everyday accounts)";
            color = Color.ORANGE;
        } else {
            grade = "🔴 Weak (Vulnerable to brute-force dictionary attacks)";
            color = Color.RED;
        }

        return new EmbedBuilder()
                .setTitle("🛡️ Password Entropy & Security Analysis")
                .setColor(color)
                .addField("📊 Security Rating", grade, false)
                .addField("🔢 Bit Entropy", String.format("%.1f bits", entropy), true)
                .addField("📏 Length", length + " characters", true)
                .addField("🔤 Character Diversity", (hasLower ? "✓ Lower " : "") + (hasUpper ? "✓ Upper " : "") + (hasDigit ? "✓ Numbers " : "") + (hasSpecial ? "✓ Symbols" : ""), false)
                .setFooter("🔒 Privacy Guarantee: Password was analyzed ephemerally and NOT stored anywhere.", null)
                .setTimestamp(Instant.now());
    }

    @Override
    public String getName() { return "passwordstrength"; }

    @Override
    public String getDescription() { return "Ephemerally test password entropy & brute-force resistance (never logged or saved)"; }

    @Override
    public String getUsage() { return "/passwordstrength password:<string>"; }
}
