package org.ashcrest.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.awt.Color;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;

/**
 * SSL/TLS Certificate Inspection Command.
 */
public class SslCommand implements ICommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ **Usage**: `!ssl <domain>`").queue();
            return;
        }

        String domain = args[0];
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = inspectSsl(domain);
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping opt = event.getOption("domain");
        if (opt == null) {
            event.reply("❌ Please provide a domain.").setEphemeral(true).queue();
            return;
        }

        String domain = opt.getAsString();
        event.deferReply().queue();

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            EmbedBuilder embed = inspectSsl(domain);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        });
    }

    private EmbedBuilder inspectSsl(String rawDomain) {
        String domain = rawDomain.toLowerCase().replaceAll("^https?://", "").replaceAll("/.*$", "").trim();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔐 SSL/TLS Certificate Audit — " + domain)
                .setTimestamp(Instant.now());

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            SSLSocketFactory factory = sslContext.getSocketFactory();

            try (SSLSocket socket = (SSLSocket) factory.createSocket(domain, 443)) {
                socket.setSoTimeout(5000);
                socket.startHandshake();

                X509Certificate[] certs = (X509Certificate[]) socket.getSession().getPeerCertificates();
                if (certs.length > 0) {
                    X509Certificate cert = certs[0];
                    Date notAfter = cert.getNotAfter();
                    long daysRemaining = ChronoUnit.DAYS.between(Instant.now(), notAfter.toInstant());

                    Color color = daysRemaining > 30 ? Color.GREEN : (daysRemaining > 7 ? Color.ORANGE : Color.RED);
                    embed.setColor(color);
                    embed.addField("📜 Subject CN", cert.getSubjectX500Principal().getName(), false);
                    embed.addField("🏢 Issuer", cert.getIssuerX500Principal().getName(), false);
                    embed.addField("📅 Valid Until", notAfter.toString(), true);
                    embed.addField("⏳ Days Remaining", daysRemaining + " days", true);
                    embed.addField("🔑 Signature Algorithm", cert.getSigAlgName(), true);
                    embed.setFooter("AshcrestHQ Aegis Security Inspection", null);
                }
            }
        } catch (Exception e) {
            embed.setColor(Color.RED)
                    .addField("❌ Connection Error", "Unable to establish TLS handshake: " + e.getMessage(), false);
        }

        return embed;
    }

    @Override
    public String getName() { return "ssl"; }

    @Override
    public String getDescription() { return "Inspect TLS/SSL certificate issuer, validity dates, and expiration days"; }

    @Override
    public String getUsage() { return "!ssl <domain> or /ssl domain:<string>"; }
}
