package org.ashcrest.core;

import org.ashcrest.commands.*;
import org.ashcrest.commands.music.*;
import org.ashcrest.db.DatabaseManager;
import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Aegis Bot core manager.
 */
public class AegisBot {
    private static volatile JDA jdaInstance;
    private static volatile AegisBot botInstance;
    private final JDA jda;
    private final Map<String, ICommand> commands = new HashMap<>();
    private final DatabaseManager dbManager;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public AegisBot() {
        botInstance = this;
        Config.validate();
        this.dbManager = DatabaseManager.getInstance();

        this.jda = JDABuilder.createDefault(Config.DISCORD_TOKEN)
                .setAudioModuleConfig(
                        new AudioModuleConfig()
                                .withDaveSessionFactory(new JDaveSessionFactory())
                )
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .setActivity(Activity.watching("Ashcrest Operations"))
                .addEventListeners(new CommandListener(this))
                .build();

        jdaInstance = this.jda;
        registerCommands();
    }

    public static AegisBot getInstance() {
        return botInstance;
    }

    public static JDA getJda() {
        return jdaInstance;
    }

    private void registerCommands() {
        registerCommand(new StatusCommand());
        registerCommand(new AuditCommand());
        registerCommand(new HelpCommand());
        registerCommand(new WikiAddCommand(dbManager));
        registerCommand(new AshcrestCommand());
        registerCommand(new CyberHeavenCommand()); // Alias for backward compatibility
        registerCommand(new SourceCommand());
        registerCommand(new AskCommand());

        // Cyber Hack RPG Game Commands
        registerCommand(new HackCommand());
        registerCommand(new ProfileCommand());
        registerCommand(new LeaderboardCommand());
        registerCommand(new DailyCommand());

        // Config & Moderation
        registerCommand(new ConfigCommand());
        registerCommand(new org.ashcrest.commands.moderation.WarnCommand());
        registerCommand(new org.ashcrest.commands.moderation.WarningsCommand());
        registerCommand(new org.ashcrest.commands.moderation.MuteCommand());
        registerCommand(new org.ashcrest.commands.moderation.UnmuteCommand());
        registerCommand(new org.ashcrest.commands.moderation.KickCommand());
        registerCommand(new org.ashcrest.commands.moderation.BanCommand());
        registerCommand(new org.ashcrest.commands.moderation.ModLogsCommand());
        registerCommand(new org.ashcrest.commands.moderation.PurgeCommand());
        registerCommand(new org.ashcrest.commands.moderation.SlowmodeCommand());
        registerCommand(new org.ashcrest.commands.moderation.LockdownCommand());

        // Anti-Raid & Onboarding
        registerCommand(new org.ashcrest.commands.antiraid.RaidModeCommand());
        registerCommand(new org.ashcrest.commands.antiraid.VerifyCommand());
        registerCommand(new org.ashcrest.commands.antiraid.AutoModCommand());
        registerCommand(new org.ashcrest.commands.antiraid.ReportCommand());

        // Security Recon
        registerCommand(new DnsScanCommand());
        registerCommand(new HeadersCommand());
        registerCommand(new SslCommand());
        registerCommand(new SecurityTxtCommand());
        registerCommand(new HashIdCommand());
        registerCommand(new IpLookupCommand());
        registerCommand(new PasswordStrengthCommand());
        registerCommand(new BreachCommand());
        registerCommand(new BlacklistCommand());
        registerCommand(new PhishCheckCommand());
        registerCommand(new CveCommand());
        registerCommand(new SubdomainsCommand());

        // AI Suite
        registerCommand(new ChatCommand());
        registerCommand(new SummarizeCommand());
        registerCommand(new TranslateCommand());
        registerCommand(new ExplainCommand());
        registerCommand(new CodeCommand());

        // Fun Commands
        FunCommands fun = new FunCommands();
        registerCommand(fun);
        commands.put("roll", fun);
        commands.put("8ball", fun);
        commands.put("joke", fun);
        commands.put("meme", fun);

        // VC Music & Apple Music Commands
        registerCommand(new PlayCommand());
        registerCommand(new SkipCommand());
        StopCommand stopCmd = new StopCommand();
        registerCommand(stopCmd);
        commands.put("leave", stopCmd);
        registerCommand(new QueueCommand());
        registerCommand(new NowPlayingCommand());
        registerCommand(new PauseResumeCommand(true));
        registerCommand(new PauseResumeCommand(false));
    }

    private void registerCommand(ICommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public ICommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public Map<String, ICommand> getCommands() {
        return new HashMap<>(commands);
    }

    public ExecutorService getVirtualThreadExecutor() {
        return virtualThreadExecutor;
    }

    public CompletableFuture<Void> startAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                jda.awaitReady();
                registerSlashCommandsWithJDA();
                System.out.println("🛡️ Aegis Bot is online and guarding AshcrestHQ!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, virtualThreadExecutor);
    }

    private void registerSlashCommandsWithJDA() {
        jda.updateCommands().addCommands(
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("help", "Display Aegis commands categorized by security, game, wiki, and music"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("status", "Check Aegis bot system health, ping, and uptime"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("audit", "Audit server permissions, roles, and administrative risk levels"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("ashcrest", "View official links, public bot invite, and resources for AshcrestHQ"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("cyberheaven", "View official links for CyberHeaven (alias)"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("source", "View open-source GitHub repository and MIT license info for Aegis"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("aegis", "View open-source GitHub repository and MIT license info for Aegis"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("ask", "Ask Aegis AI (powered by Google Gemini 3.6 Flash)")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "prompt", "The question or prompt for Gemini AI", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("hack", "Initiate interactive cyber penetration test mini-game for credits & XP"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("profile", "View your Ashcrest hacker rank, level, XP, credits, and hack stats"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("leaderboard", "View top hacker leaderboard ranked by level and credits"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("daily", "Claim daily Ashcrest credits (250 🪙)"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("wikiadd", "Add or update an entry in the community security wiki")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "keyword", "Wiki topic keyword", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "content", "Wiki content body", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("config", "Configure Aegis server settings")
                        .addSubcommands(
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("view", "View current configuration"),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("modlog", "Set moderation log channel")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.CHANNEL, "channel", "Target channel", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("welcome-channel", "Set welcome channel")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.CHANNEL, "channel", "Target channel", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("welcome-message", "Set welcome message ({user}/{server} supported)")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "message", "Welcome message text", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("autorole", "Set auto-assigned role on join")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.ROLE, "role", "Role to assign", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("unverified-role", "Set unverified role for join verification gate")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.ROLE, "role", "Role to assign on join before verification", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("automod", "Enable or disable AutoMod")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.BOOLEAN, "enabled", "On or off", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("spam-threshold", "Set AutoMod spam message threshold")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER, "count", "Messages per interval before flagging", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("banned-words", "Set AutoMod banned word list (comma-separated)")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "words", "Comma-separated banned words", true),
                                new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("prefix", "Set a per-guild command prefix override")
                                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "prefix", "New prefix (e.g. ?)", true)
                        )
                        .setDefaultPermissions(net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.MANAGE_SERVER)),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("warn", "Warn a server member")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "Member to warn", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "reason", "Reason for warning", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("warnings", "View member warnings")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "Member to query", false),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("mute", "Timeout member")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "Member to mute", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER, "minutes", "Duration in minutes", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "reason", "Reason", false),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("unmute", "Remove timeout from member")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "Member to unmute", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("kick", "Kick member")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "Member to kick", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "reason", "Reason", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("ban", "Ban member")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "User to ban", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "reason", "Reason", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("unban", "Unban user by ID")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "user_id", "Target User ID", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("modlogs", "View full moderation case history for a user")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "Target member", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("purge", "Bulk delete recent channel messages")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER, "count", "Number of messages to delete (1-100)", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("slowmode", "Set channel slowmode delay in seconds")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER, "seconds", "Slowmode seconds", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("lockdown", "Lockdown current channel for @everyone"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("unlock", "Unlock current channel for @everyone"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("raidmode", "Toggle anti-raid verification level lock")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.BOOLEAN, "enabled", "Enable or disable raidmode", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("verify", "Post button-based human verification gate"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("automod", "Enable or disable chat AutoMod filter")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.BOOLEAN, "enabled", "Enable or disable AutoMod", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("report", "Report a member to server moderators")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.USER, "user", "Reported member", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "reason", "Reason", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("dnsscan", "Audit domain DNS records, SPF, DMARC, DNSSEC")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "domain", "Target domain", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("headers", "Audit HTTP security response headers")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "url", "Target URL or domain", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("ssl", "Audit TLS/SSL certificate validity and issuer")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "domain", "Target domain", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("securitytxt", "Audit domain RFC 9116 security.txt policy")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "domain", "Target domain", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("hashid", "Identify cryptographic hash algorithm")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "hash", "Target hash string", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("iplookup", "Passive IP geolocation and ASN lookup")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "ip", "Target IP address", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("passwordstrength", "Ephemerally test password entropy")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "password", "Target password", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("breach", "Check if an email address has been compromised in data breaches")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "email", "Target email address", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("blacklist", "Perform passive DNSBL blacklist lookup against Spamhaus & RBLs")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "target", "Target IP or domain", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("phishcheck", "Check URL against Google Safe Browsing threat list")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "url", "Target URL", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("cve", "Lookup CVE details, CVSS scores, and NVD report")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "id", "CVE ID (e.g. CVE-2024-3094)", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("subdomains", "Enumerate subdomains via Certificate Transparency logs (crt.sh)")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "domain", "Target domain", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("chat", "Stateful AI conversation context via Gemini 3.6 Flash")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "message", "User message", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("summarize", "Summarize text into key takeaways")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "text", "Content to summarize", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("translate", "Translate text into target language")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "target_lang", "Target language", true)
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "text", "Text to translate", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("explain", "Explain complex technical topics or code")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "topic", "Topic or code snippet", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("code", "Ask software engineering and coding Q&A questions")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "question", "Coding question", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("coinflip", "Flip a coin"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("roll", "Roll a die")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER, "sides", "Number of sides (default 6)", false),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("8ball", "Ask the Magic 8-Ball a question")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "question", "Question", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("joke", "Get a programming joke"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("meme", "Get a cyber meme"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("play", "Play audio in your VC (supports Apple Music, YouTube, direct audio URLs)")
                        .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "input", "Song title, URL, or Apple Music link", true),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("skip", "Skip the currently playing track"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("stop", "Stop playback, clear queue, and disconnect Aegis from VC"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("queue", "Display current music queue and now playing track"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("nowplaying", "Show progress bar and details of currently playing track"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("pause", "Pause audio playback"),
                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("resume", "Resume paused audio playback")
        ).queue();
    }

    public void shutdown() {
        if (virtualThreadExecutor != null && !virtualThreadExecutor.isShutdown()) {
            virtualThreadExecutor.shutdown();
        }
        if (dbManager != null) {
            dbManager.close();
        }
        if (jda != null) {
            jda.shutdown();
        }
        System.out.println("🔴 Aegis Bot shutdown complete");
    }
}
