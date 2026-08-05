package org.ashcrest.commands;

import org.ashcrest.db.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * Role-restricted Wiki Add command for contributing entries to the PostgreSQL security wiki.
 */
public class WikiAddCommand implements ICommand {
    private final DatabaseManager dbManager;
    private static final List<String> ALLOWED_ROLES = List.of("Wiki Editor", "Staff", "Moderator");

    public WikiAddCommand(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            event.getChannel().sendMessage("❌ This command can only be used in a server.").queue();
            return;
        }

        Member member = event.getMember();
        if (!hasPermission(member)) {
            event.getChannel().sendMessage("❌ **Access Denied**: You require `ADMINISTRATOR` permission or a `Wiki Editor`/`Staff` role to add wiki entries.").queue();
            return;
        }

        if (args.length < 2) {
            event.getChannel().sendMessage("❌ **Usage**: `!wikiadd <keyword> <content>`").queue();
            return;
        }

        String keyword = args[0];
        String content = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        String contributor = event.getAuthor().getName();

        // Offload database insertion to Virtual Threads
        dbManager.addWikiEntry(keyword, content, contributor).thenAccept(success -> {
            if (success) {
                event.getChannel().sendMessage("✅ **Wiki Entry Created/Updated**: `" + keyword + "`").queue();
            } else {
                event.getChannel().sendMessage("❌ Failed to save wiki entry to database.").queue();
            }
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        if (!hasPermission(member)) {
            event.reply("❌ **Access Denied**: You require `ADMINISTRATOR` permission or a `Wiki Editor`/`Staff` role to execute `/wiki add`.").setEphemeral(true).queue();
            return;
        }

        OptionMapping keywordOpt = event.getOption("keyword");
        OptionMapping contentOpt = event.getOption("content");

        if (keywordOpt == null || contentOpt == null) {
            event.reply("❌ Please provide both a keyword and content.").setEphemeral(true).queue();
            return;
        }

        String keyword = keywordOpt.getAsString();
        String content = contentOpt.getAsString();
        String contributor = event.getUser().getName();

        // Defer reply so gateway stays 100% non-blocking
        event.deferReply().queue();

        // Non-blocking database insertion handled on Virtual Threads
        dbManager.addWikiEntry(keyword, content, contributor).thenAccept(success -> {
            if (success) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📚 Wiki Entry Saved")
                        .setColor(Color.GREEN)
                        .addField("🔑 Keyword", keyword, false)
                        .addField("📄 Content", content.length() > 1000 ? content.substring(0, 997) + "..." : content, false)
                        .addField("👤 Contributor", contributor, true)
                        .setFooter("AshcrestHQ Aegis | PostgreSQL Knowledge Base")
                        .setTimestamp(Instant.now());

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            } else {
                event.getHook().sendMessage("❌ Database Error: Unable to save wiki entry.").queue();
            }
        });
    }

    /**
     * Permission check: User must have ADMINISTRATOR OR belong to an allowed staff role.
     */
    private boolean hasPermission(Member member) {
        if (member == null) return false;
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;

        return member.getRoles().stream()
                .anyMatch(role -> ALLOWED_ROLES.stream()
                        .anyMatch(allowed -> role.getName().equalsIgnoreCase(allowed)));
    }

    @Override
    public String getName() { return "wikiadd"; }

    @Override
    public String getDescription() { return "Add or update an entry in the community security wiki (Staff/Admin restricted)"; }

    @Override
    public String getUsage() { return "!wikiadd <keyword> <content> or /wiki add keyword:<key> content:<val>"; }
}
