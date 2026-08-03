package org.ashcrest.db;

import java.lang.reflect.Method;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Economy and Hacker Profile Manager for Ashcrest Aegis.
 */
public class EconomyManager {
    private static volatile EconomyManager instance;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private EconomyManager() {}

    public static EconomyManager getInstance() {
        if (instance == null) {
            synchronized (EconomyManager.class) {
                if (instance == null) {
                    instance = new EconomyManager();
                }
            }
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Method getDataSourceMethod = DatabaseManager.class.getDeclaredMethod("getDataSource");
            getDataSourceMethod.setAccessible(true);
            javax.sql.DataSource ds = (javax.sql.DataSource) getDataSourceMethod.invoke(dbManager);
            return ds.getConnection();
        } catch (Exception e) {
            // Fallback connection via Config
            return DriverManager.getConnection(
                    org.ashcrest.core.Config.DB_URL,
                    org.ashcrest.core.Config.DB_USER,
                    org.ashcrest.core.Config.DB_PASSWORD
            );
        }
    }

    public record UserProfile(
            long userId,
            String username,
            long credits,
            long xp,
            int level,
            int successfulHacks,
            int failedHacks,
            Timestamp lastDaily
    ) {
        public String getRankName() {
            if (level >= 25) return "👑 Cyber Sovereign";
            if (level >= 15) return "🔮 Security Architect";
            if (level >= 10) return "⚡ Netrunner Specialist";
            if (level >= 5) return "🛡️ Cyber Analyst";
            return "💻 Script Kiddie";
        }

        public long getXpForNextLevel() {
            return level * 150L;
        }

        public double getWinRate() {
            int total = successfulHacks + failedHacks;
            if (total == 0) return 0.0;
            return (double) successfulHacks / total * 100.0;
        }
    }

    public record DailyClaimResult(boolean success, long claimedAmount, String message, Duration timeUntilNext) {}

    public CompletableFuture<UserProfile> getUserProfile(long userId, String username) {
        return CompletableFuture.supplyAsync(() -> {
            String selectSql = "SELECT * FROM user_economy WHERE user_id = ?";
            String insertSql = "INSERT INTO user_economy (user_id, username, credits, xp, level, successful_hacks, failed_hacks) VALUES (?, ?, 100, 0, 1, 0, 0) ON CONFLICT (user_id) DO UPDATE SET username = EXCLUDED.username RETURNING *";
            
            try (Connection conn = getConnection()) {
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                    pstmt.setLong(1, userId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return mapRowToProfile(rs);
                        }
                    }
                }
                
                // Initialize default profile if non-existent
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setLong(1, userId);
                    pstmt.setString(2, username);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return mapRowToProfile(rs);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error fetching user profile: " + e.getMessage());
            }
            return new UserProfile(userId, username, 100, 0, 1, 0, 0, null);
        }, executor);
    }

    public CompletableFuture<UserProfile> recordHackOutcome(long userId, String username, boolean success, long creditDelta, long xpDelta) {
        return CompletableFuture.supplyAsync(() -> {
            UserProfile current = getUserProfile(userId, username).join();
            long newCredits = Math.max(0, current.credits + creditDelta);
            long newXp = Math.max(0, current.xp + xpDelta);
            int newLevel = current.level;
            
            // Check for level-ups
            long reqXp = newLevel * 150L;
            while (newXp >= reqXp) {
                newXp -= reqXp;
                newLevel++;
                reqXp = newLevel * 150L;
            }

            int newSuccesses = current.successfulHacks + (success ? 1 : 0);
            int newFailures = current.failedHacks + (success ? 0 : 1);

            String sql = """
                INSERT INTO user_economy (user_id, username, credits, xp, level, successful_hacks, failed_hacks, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (user_id) DO UPDATE SET
                    username = EXCLUDED.username,
                    credits = EXCLUDED.credits,
                    xp = EXCLUDED.xp,
                    level = EXCLUDED.level,
                    successful_hacks = EXCLUDED.successful_hacks,
                    failed_hacks = EXCLUDED.failed_hacks,
                    updated_at = CURRENT_TIMESTAMP
                RETURNING *
                """;

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, userId);
                pstmt.setString(2, username);
                pstmt.setLong(3, newCredits);
                pstmt.setLong(4, newXp);
                pstmt.setInt(5, newLevel);
                pstmt.setInt(6, newSuccesses);
                pstmt.setInt(7, newFailures);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToProfile(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error updating hack outcome: " + e.getMessage());
            }
            return new UserProfile(userId, username, newCredits, newXp, newLevel, newSuccesses, newFailures, current.lastDaily);
        }, executor);
    }

    public CompletableFuture<DailyClaimResult> claimDailyReward(long userId, String username, long rewardAmount) {
        return CompletableFuture.supplyAsync(() -> {
            UserProfile profile = getUserProfile(userId, username).join();
            Instant now = Instant.now();

            if (profile.lastDaily() != null) {
                Instant lastClaim = profile.lastDaily().toInstant();
                Duration timePassed = Duration.between(lastClaim, now);
                if (timePassed.toHours() < 24) {
                    Duration remaining = Duration.ofHours(24).minus(timePassed);
                    return new DailyClaimResult(
                            false,
                            0,
                            "⏳ You have already claimed your daily reward today! Next claim available in " 
                                    + remaining.toHours() + "h " + (remaining.toMinutes() % 60) + "m.",
                            remaining
                    );
                }
            }

            long newCredits = profile.credits() + rewardAmount;
            Timestamp currentTimestamp = Timestamp.from(now);

            String sql = """
                UPDATE user_economy
                SET credits = ?, last_daily = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ?
                """;

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, newCredits);
                pstmt.setTimestamp(2, currentTimestamp);
                pstmt.setLong(3, userId);
                pstmt.executeUpdate();
                return new DailyClaimResult(true, rewardAmount, "🎉 Successfully claimed your daily **" + rewardAmount + " 🪙 Ashcrest Credits**!", Duration.ZERO);
            } catch (SQLException e) {
                System.err.println("Error processing daily claim: " + e.getMessage());
                return new DailyClaimResult(false, 0, "❌ Failed to process daily claim due to a database error.", Duration.ZERO);
            }
        }, executor);
    }

    public CompletableFuture<List<UserProfile>> getLeaderboard(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<UserProfile> list = new ArrayList<>();
            String sql = "SELECT * FROM user_economy ORDER BY level DESC, xp DESC, credits DESC LIMIT ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRowToProfile(rs));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error fetching leaderboard: " + e.getMessage());
            }
            return list;
        }, executor);
    }

    private UserProfile mapRowToProfile(ResultSet rs) throws SQLException {
        return new UserProfile(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getLong("credits"),
                rs.getLong("xp"),
                rs.getInt("level"),
                rs.getInt("successful_hacks"),
                rs.getInt("failed_hacks"),
                rs.getTimestamp("last_daily")
        );
    }
}
