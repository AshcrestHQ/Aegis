package org.ashcrest.db;

import org.ashcrest.core.Config;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Per-Guild Configuration Manager for Ashcrest Aegis.
 */
public class GuildSettingsManager {
    private static volatile GuildSettingsManager instance;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private GuildSettingsManager() {}

    public static GuildSettingsManager getInstance() {
        if (instance == null) {
            synchronized (GuildSettingsManager.class) {
                if (instance == null) {
                    instance = new GuildSettingsManager();
                }
            }
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        try {
            return DatabaseManager.getInstance().getDataSource().getConnection();
        } catch (Exception e) {
            return DriverManager.getConnection(Config.DB_URL, Config.DB_USER, Config.DB_PASSWORD);
        }
    }

    public record GuildSettings(
            long guildId,
            long modLogChannelId,
            long welcomeChannelId,
            String welcomeMessage,
            long autoroleId,
            boolean automodEnabled,
            int automodSpamThreshold,
            String automodBannedWords,
            String prefixOverride
    ) {
        public String getEffectivePrefix() {
            return (prefixOverride != null && !prefixOverride.trim().isEmpty()) ? prefixOverride : Config.BOT_PREFIX;
        }
    }

    public CompletableFuture<GuildSettings> getSettings(long guildId) {
        return CompletableFuture.supplyAsync(() -> {
            String selectSql = "SELECT * FROM guild_settings WHERE guild_id = ?";
            String insertSql = "INSERT INTO guild_settings (guild_id) VALUES (?) ON CONFLICT (guild_id) DO NOTHING";

            try (Connection conn = getConnection()) {
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                    pstmt.setLong(1, guildId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return mapRowToSettings(rs);
                        }
                    }
                }

                // Insert default row if absent
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setLong(1, guildId);
                    pstmt.executeUpdate();
                }

                // Fetch newly inserted row
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                    pstmt.setLong(1, guildId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return mapRowToSettings(rs);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error fetching guild settings for guild " + guildId + ": " + e.getMessage());
            }

            return new GuildSettings(guildId, 0L, 0L, "Welcome {user} to {server}!", 0L, false, 5, "", null);
        }, executor);
    }

    public CompletableFuture<Boolean> updateModLogChannel(long guildId, long channelId) {
        return updateField(guildId, "mod_log_channel_id", channelId);
    }

    public CompletableFuture<Boolean> updateWelcomeChannel(long guildId, long channelId) {
        return updateField(guildId, "welcome_channel_id", channelId);
    }

    public CompletableFuture<Boolean> updateWelcomeMessage(long guildId, String message) {
        return updateField(guildId, "welcome_message", message);
    }

    public CompletableFuture<Boolean> updateAutorole(long guildId, long roleId) {
        return updateField(guildId, "autorole_id", roleId);
    }

    public CompletableFuture<Boolean> updateAutoMod(long guildId, boolean enabled) {
        return updateField(guildId, "automod_enabled", enabled);
    }

    public CompletableFuture<Boolean> updateAutoModBannedWords(long guildId, String words) {
        return updateField(guildId, "automod_banned_words", words);
    }

    public CompletableFuture<Boolean> updateAutoModSpamThreshold(long guildId, int threshold) {
        return updateField(guildId, "automod_spam_threshold", threshold);
    }

    public CompletableFuture<Boolean> updatePrefix(long guildId, String prefix) {
        return updateField(guildId, "prefix_override", prefix);
    }

    private CompletableFuture<Boolean> updateField(long guildId, String column, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            // First ensure guild row exists
            getSettings(guildId).join();

            String sql = "UPDATE guild_settings SET " + column + " = ? WHERE guild_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, value);
                pstmt.setLong(2, guildId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error updating field " + column + " for guild " + guildId + ": " + e.getMessage());
                return false;
            }
        }, executor);
    }

    private GuildSettings mapRowToSettings(ResultSet rs) throws SQLException {
        return new GuildSettings(
                rs.getLong("guild_id"),
                rs.getLong("mod_log_channel_id"),
                rs.getLong("welcome_channel_id"),
                rs.getString("welcome_message"),
                rs.getLong("autorole_id"),
                rs.getBoolean("automod_enabled"),
                rs.getInt("automod_spam_threshold"),
                rs.getString("automod_banned_words"),
                rs.getString("prefix_override")
        );
    }
}
