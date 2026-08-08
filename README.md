# 🛡️ Aegis - Wyzer Rewrite

> **Universal Discord Bot rewritten in the highly experimental Wyzer Programming Language.**
> Aegis has been completely ported from Java 26 to Wyzer, utilizing Choreographic Programming and a Perceus memory model.

---

## ⚡ Key Features (Wyzer Edition)

- 🎮 **Interactive Cyber Breach RPG**: High-octane cyber security penetration testing mini-game ported directly from the Java version. Features PostgreSQL integration via the custom `Wyzer-Postgres` library.
- 🤖 **Google Gemini AI Assistant**: Native command handling for AI API integrations.
- 🔒 **Discord Voice DAVE E2EE & Audio Engine**: Full-featured Audio processing using the custom `Wyzer-Audio` engine, supporting Opus encoding and libsodium xchacha20poly1305 encryption.
- 🛡️ **Security Recon**: DNS validation, SPF checking, and other security utilities.
- ⚡ **Choreographic Architecture**: Replaces traditional Java Virtual Threads with Wyzer's strict node-based Role architecture (`@BotLogic`, `@DiscordGateway`).

---

## 🏗️ Architecture & Package Structure

```
wyzer_bot/
├── src/
│   ├── config.wyz        # Configuration and .env parser
│   ├── main.wyz          # Main Choreographic Event Loop
│   ├── core/
│   │   └── events.wyz    # Gateway event router
│   ├── commands/
│   │   ├── handler.wyz   # Modular command registry
│   │   ├── rpg.wyz       # Cyber Breach RPG logic
│   │   └── ai.wyz        # AI Integration logic
│   ├── db/
│   │   ├── postgres.wyz  # Wyzer-Postgres connection manager
│   │   └── economy.wyz   # RPG Economy state
│   └── security/
│       └── recon.wyz     # Recon and DNS utilities
```

---

## 🛠️ Required Custom Libraries

Because Wyzer is an experimental language, the following foundational libraries had to be built from scratch (located in the Wyzer `lib/` directory):

1. **Wyzer-Postgres**: TCP-based PostgreSQL driver using C FFI (`libpq`).
2. **Wyzer-Audio**: Audio packet engine using C FFI (`libsodium`, `opus`) for DAVE E2EE.
3. **Wyzer-Discord**: A Discord Gateway and HTTP API boundary (currently mocked due to Wyzer build system linking limitations).

---

## 🚀 Running Locally

Ensure you have the `wyzerc` compiler installed.

```bash
# Compile and run the BotLogic role
wyzerc -- src/main.wyz --role BotLogic
```
