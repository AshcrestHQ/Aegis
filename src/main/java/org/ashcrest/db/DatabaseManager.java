package org.ashcrest.db;

import org.ashcrest.core.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private final HikariDataSource dataSource;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private DatabaseManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.DB_URL);
        config.setUsername(Config.DB_USER);
        config.setPassword(Config.DB_PASSWORD);
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);

        this.dataSource = new HikariDataSource(config);
        initializeSchema();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private void initializeSchema() {
        try (Connection conn = dataSource.getConnection()) {
            String createWikiTable = """
                CREATE TABLE IF NOT EXISTS aegis_wiki (
                    id SERIAL PRIMARY KEY,
                    keyword VARCHAR(255) NOT NULL UNIQUE,
                    content TEXT NOT NULL,
                    contributor VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createWikiTable);
            }

            String createAuditLogTable = """
                CREATE TABLE IF NOT EXISTS aegis_audit_log (
                    id SERIAL PRIMARY KEY,
                    event_type VARCHAR(100) NOT NULL,
                    target_id VARCHAR(100) NOT NULL,
                    details TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createAuditLogTable);
            }

            String createEconomyTable = """
                CREATE TABLE IF NOT EXISTS user_economy (
                    user_id BIGINT PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    credits BIGINT DEFAULT 100,
                    xp BIGINT DEFAULT 0,
                    level INT DEFAULT 1,
                    successful_hacks INT DEFAULT 0,
                    failed_hacks INT DEFAULT 0,
                    last_daily TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createEconomyTable);
            }

            String createGuildSettingsTable = """
                CREATE TABLE IF NOT EXISTS guild_settings (
                    guild_id BIGINT PRIMARY KEY,
                    mod_log_channel_id BIGINT DEFAULT 0,
                    welcome_channel_id BIGINT DEFAULT 0,
                    welcome_message TEXT DEFAULT 'Welcome {user} to {server}!',
                    autorole_id BIGINT DEFAULT 0,
                    unverified_role_id BIGINT DEFAULT 0,
                    automod_enabled BOOLEAN DEFAULT FALSE,
                    automod_spam_threshold INT DEFAULT 5,
                    automod_banned_words TEXT DEFAULT '',
                    prefix_override VARCHAR(10) DEFAULT NULL
                );
                ALTER TABLE guild_settings ADD COLUMN IF NOT EXISTS unverified_role_id BIGINT DEFAULT 0;
                """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createGuildSettingsTable);
            }

            String createModCasesTable = """
                CREATE TABLE IF NOT EXISTS mod_cases (
                    id SERIAL PRIMARY KEY,
                    guild_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    moderator_id BIGINT NOT NULL,
                    action VARCHAR(50) NOT NULL,
                    reason TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    active BOOLEAN DEFAULT TRUE
                );
                """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createModCasesTable);
            }

            System.out.println("✅ Aegis Database Schema Initialized");
        } catch (SQLException e) {
            System.err.println("⚠️ Database schema init warning (DB may be offline): " + e.getMessage());
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public CompletableFuture<Boolean> addWikiEntry(String keyword, String content, String contributor) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO aegis_wiki (keyword, content, contributor) VALUES (?, ?, ?) ON CONFLICT (keyword) DO UPDATE SET content = EXCLUDED.content";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, keyword);
                pstmt.setString(2, content);
                pstmt.setString(3, contributor);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Error adding wiki entry: " + e.getMessage());
                return false;
            }
        }, virtualThreadExecutor);
    }

    public CompletableFuture<WikiEntry> getWikiEntry(String keyword) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM aegis_wiki WHERE keyword ILIKE ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, keyword);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new WikiEntry(rs.getInt("id"), rs.getString("keyword"), rs.getString("content"), rs.getString("contributor"));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error fetching wiki entry: " + e.getMessage());
            }
            return null;
        }, virtualThreadExecutor);
    }

    public void close() {
        if (virtualThreadExecutor != null && !virtualThreadExecutor.isShutdown()) {
            virtualThreadExecutor.shutdown();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public record WikiEntry(int id, String keyword, String content, String contributor) {}
}
