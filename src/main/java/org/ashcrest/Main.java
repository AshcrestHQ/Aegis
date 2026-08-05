package org.ashcrest;

import org.ashcrest.core.AegisBot;

import java.util.concurrent.CountDownLatch;

/**
 * Entry point for Aegis Security Bot.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🛡️ Starting Aegis Bot (AshcrestHQ)");
        System.out.println("Java Version: " + System.getProperty("java.version"));

        AegisBot bot = null;
        try {
            bot = new AegisBot();
            bot.startAsync().get();

            System.out.println("✅ Aegis Bot active & guarding AshcrestHQ!");
            new CountDownLatch(1).await();
        } catch (Exception e) {
            System.err.println("❌ Aegis startup error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (bot != null) {
                bot.shutdown();
            }
        }
    }
}
