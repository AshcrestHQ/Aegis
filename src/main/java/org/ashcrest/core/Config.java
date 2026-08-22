package org.ashcrest.core;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration manager for Aegis Security System.
 */
public class Config {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static final String DISCORD_TOKEN = getEnv("DISCORD_TOKEN");
    public static final String BOT_PREFIX = getEnvOrDefault("BOT_PREFIX", "/");
    public static final String BOT_STATUS = getEnvOrDefault("BOT_STATUS", "🛡️ Aegis | Ashcrest Operations");

    public static final String GEMINI_API_KEY = getEnvOrDefault("GEMINI_API_KEY", "");
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent";
    public static final String GEMINI_LOGO_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8a/Google_Gemini_logo.svg/512px-Google_Gemini_logo.svg.png";
    public static final String PUBLIC_INVITE_URL = "https://discord.com/oauth2/authorize?client_id=1497620745604563096&permissions=824938978566&integration_type=0&scope=applications.commands+bot";

    public static final String DB_HOST = getEnvOrDefault("DB_HOST", "localhost");
    public static final String DB_PORT = getEnvOrDefault("DB_PORT", "5432");
    public static final String DB_NAME = getEnvOrDefault("DB_NAME", "ashcrest_aegis");
    public static final String DB_USER = getEnvOrDefault("DB_USER", "postgres");
    public static final String DB_PASSWORD = getEnvOrDefault("DB_PASSWORD", "postgres");

    public static final String DB_URL = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    private static String getEnv(String key) {
        String value = dotenv.get(key);
        if (value == null) value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = dotenv.get(key);
        if (value == null) value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public static void validate() {
        getEnv("DISCORD_TOKEN");
    }
}
