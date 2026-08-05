package org.ashcrest.db;

import org.ashcrest.core.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Moderation Case History Manager for Ashcrest Aegis.
 */
public class ModerationManager {
    private static volatile ModerationManager instance;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private ModerationManager() {}

    public static ModerationManager getInstance() {
        if (instance == null) {
            synchronized (ModerationManager.class) {
                if (instance == null) {
                    instance = new ModerationManager();
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

    public record ModCase(
            int id,
            long guildId,
            long userId,
            long moderatorId,
            String action,
            String reason,
            Timestamp timestamp,
            boolean active
    ) {}

    public CompletableFuture<ModCase> createCase(long guildId, long userId, long moderatorId, String action, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO mod_cases (guild_id, user_id, moderator_id, action, reason, active)
                VALUES (?, ?, ?, ?, ?, TRUE)
                RETURNING *
                """;

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, guildId);
                pstmt.setLong(2, userId);
                pstmt.setLong(3, moderatorId);
                pstmt.setString(4, action.toUpperCase());
                pstmt.setString(5, reason);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToCase(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error creating mod case: " + e.getMessage());
            }
            return new ModCase(0, guildId, userId, moderatorId, action, reason, new Timestamp(System.currentTimeMillis()), true);
        }, executor);
    }

    public CompletableFuture<Integer> getActiveWarningsCount(long guildId, long userId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM mod_cases WHERE guild_id = ? AND user_id = ? AND action = 'WARN' AND active = TRUE";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, guildId);
                pstmt.setLong(2, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error fetching warning count: " + e.getMessage());
            }
            return 0;
        }, executor);
    }

    public CompletableFuture<List<ModCase>> getCaseHistory(long guildId, long userId) {
        return CompletableFuture.supplyAsync(() -> {
            List<ModCase> cases = new ArrayList<>();
            String sql = "SELECT * FROM mod_cases WHERE guild_id = ? AND user_id = ? ORDER BY id DESC LIMIT 25";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, guildId);
                pstmt.setLong(2, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        cases.add(mapRowToCase(rs));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error fetching case history: " + e.getMessage());
            }
            return cases;
        }, executor);
    }

    public CompletableFuture<Boolean> revokeCase(long guildId, int caseId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE mod_cases SET active = FALSE WHERE guild_id = ? AND id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, guildId);
                pstmt.setInt(2, caseId);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error revoking case " + caseId + ": " + e.getMessage());
                return false;
            }
        }, executor);
    }

    private ModCase mapRowToCase(ResultSet rs) throws SQLException {
        return new ModCase(
                rs.getInt("id"),
                rs.getLong("guild_id"),
                rs.getLong("user_id"),
                rs.getLong("moderator_id"),
                rs.getString("action"),
                rs.getString("reason"),
                rs.getTimestamp("timestamp"),
                rs.getBoolean("active")
        );
    }
}
