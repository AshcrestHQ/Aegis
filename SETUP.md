# 🛠️ Aegis Bot Setup & Deployment Guide

This guide provides step-by-step instructions for installing, configuring, running, and testing **Aegis** on your system.

---

## 📋 Prerequisites

Before setting up Aegis, make sure you have installed:

1. **Java 26 JDK** (with Virtual Thread support)
   - Verify with: `java -version`
2. **Apache Maven 3.8+**
   - Verify with: `mvn -version`
3. **PostgreSQL Database** (Version 14+)
   - Running locally or on a remote host (default port `5432`).
4. **Discord Bot Token**
   - Created in the [Discord Developer Portal](https://discord.com/developers/applications).

---

## 🔑 Step 1: Obtain Discord Bot Credentials

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications).
2. Click **New Application** and give it a name (e.g., `Aegis Bot`).
3. Navigate to **Bot** tab on the left menu.
4. Click **Reset Token** and copy your **Bot Token** (save this securely).
5. Scroll down to **Privileged Gateway Intents** and enable:
   - ✅ **Presence Intent**
   - ✅ **Server Members Intent**
   - ✅ **Message Content Intent**
6. Click **Save Changes**.
7. Navigate to **OAuth2 -> URL Generator**:
   - Scopes: `bot`, `applications.commands`
   - Bot Permissions:
     - **General**: `Read Messages/View Channels`, `Manage Roles`, `Kick Members`, `Ban Members`
     - **Text**: `Send Messages`, `Embed Links`, `Attach Files`, `Read Message History`
     - **Voice**: `Connect`, `Speak`, `Use Voice Activity`
   - Copy the generated URL or click the official public invite link: **[Invite Aegis to Your Discord Server](https://discord.com/oauth2/authorize?client_id=1497620745604563096&permissions=824938978566&integration_type=0&scope=applications.commands+bot)**.

---

## 🗄️ Step 2: Database Setup (PostgreSQL)

Run the following SQL commands to create the database and user:

```sql
CREATE DATABASE ashcrest_aegis;
CREATE USER aegis_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE ashcrest_aegis TO aegis_user;
```

*(Note: Aegis automatically initializes all required tables (`user_economy`, `aegis_wiki`, `aegis_audit_log`) on first startup.)*

---

## ⚙️ Step 3: Environment Configuration (`.env`)

1. Clone the repository:
   ```bash
   git clone https://github.com/AshcrestHQ/Aegis.git
   cd Aegis
   ```

2. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

3. Edit `.env` with your actual credentials:
   ```env
   # Discord Bot Credentials
   DISCORD_TOKEN=your_actual_discord_bot_token_here

   # Database Configuration (PostgreSQL)
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=ashcrest_aegis
   DB_USER=aegis_user
   DB_PASSWORD=your_secure_password

   # Optional Remote AI Integration (Google Gemini)
   GEMINI_API_KEY=your_gemini_api_key_here

   # Bot Customization
   BOT_PREFIX=!
   BOT_STATUS=🛡️ Aegis | Ashcrest Operations
   ```

---

## 🚀 Step 4: Build & Run

### Option A: Build and Run JAR
```bash
mvn clean package
java --enable-preview -jar target/aegis-3.0.0.jar
```

### Option B: Run via Maven Directly
```bash
mvn exec:java -Dexec.mainClass="org.ashcrest.Main"
```

---

## 🧪 Step 5: Verification & Testing

Once Aegis reports `🛡️ Aegis Bot is online and guarding AshcrestHQ!`, test the following commands in your Discord server:

1. **`!status` / `/status`**: Verifies latency, system uptime, and confirms Java 26 Virtual Threads are operational.
2. **`!hack` / `/hack`**: Initiates interactive cyber breach penetration testing mini-game with interactive Discord Buttons.
3. **`!profile` / `/profile`**: Displays your hacker level, rank, XP progress bar, win rate, and credits balance.
4. **`!leaderboard` / `/leaderboard`**: Displays top hackers ranked by level and vault credits.
5. **`!daily` / `/daily`**: Claims daily 250 🪙 Ashcrest credits.
6. **`!audit` / `/audit`**: Performs a security audit on server roles and alerts on permission risks (`ADMINISTRATOR`, `MANAGE_ROLES`, `BAN_MEMBERS`).
7. **`!play` / `/play`**: Join a Voice Channel and play music via direct search, YouTube link, or Apple Music URL (`https://music.apple.com/...`).

---

## 🆘 Troubleshooting

- **`Missing required environment variable: DISCORD_TOKEN`**: Ensure `.env` is located in the working directory and contains a valid token.
- **Database Connection Warnings**: Verify PostgreSQL service is running on port 5432 and the credentials in `.env` match your PostgreSQL user.
- **Bot Doesn't Respond to Messages**: Double check that **Message Content Intent** is enabled in the Discord Developer Portal.
- **Audio Doesn't Play in VC**: Ensure the bot has `Connect` and `Speak` permissions in the target Voice Channel.
