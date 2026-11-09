**This project has reached the end of its active development for now.

It was a fun project to work on and taught me quite a bit along the way. I've since moved on to other things, but I didn't want to take the work down. I'm leaving the repository here in case it's useful to someone, or I decide to revisit it someday.

Thanks for stopping by!**



# 🛡️ Aegis - AshcrestHQ Universal Discord Operations Bot Engine

[![CI](https://github.com/AshcrestHQ/Aegis/actions/workflows/ci.yml/badge.svg)](https://github.com/AshcrestHQ/Aegis/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-4.0.0-blue.svg)](pom.xml)
[![Java](https://img.shields.io/badge/Java-26-orange.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Discord](https://img.shields.io/badge/Discord-JDA%206.5.0-5865F2.svg)](https://github.com/discord-jda/JDA)

> **Universal Public Discord Bot Engine for Security Recon, Moderation, Anti-Raid Protection, Cyber RPG, AI Assistance & VC Audio.**
> Aegis is a high-performance Discord operations bot built with **Java 26** and **Virtual Threads**, engineered for server protection, permission risk auditing, community moderation, interactive Cyber RPG gaming, Google Gemini 3.6 Flash AI, server activity dashboard, and voice channel music with Discord DAVE E2EE support. All features rely solely on local/free resources and the Gemini API — no paid third-party security APIs required.
> 
> 🔗 **[Click Here to Invite Aegis to Your Discord Server](https://discord.com/oauth2/authorize?client_id=1497620745604563096&permissions=824938978566&integration_type=0&scope=applications.commands+bot)**

---

## 🦅 Heritage & Universal Mission

> *From the ashes of Apex, Aegis arises.*
> 
> Aegis carries forward the foundational architectural lessons, high-throughput non-blocking design, and core principles born in [Apex](https://github.com/medhansh5/Apex). Evolved into a universal, public bot hosted by **AshcrestHQ** (`AshcrestHQ/Aegis`), Aegis is designed for any Discord community to invite, deploy, and utilize for security recon, community moderation, AI assistance, voice channel audio, and interactive gaming.

---

## 🌿 Branch & Release Strategy

Aegis follows a clean stable/development branch workflow:

- **`main`** *(Stable)*: Production-ready release branch matching current official release state (**v4.0.0**). All releases and production builds are deployed from `main`.
- **`v4-dev`** *(Development)*: Active integration branch for upcoming v4 features, enhancements, and dependency updates.
- **`feature/*` / `fix/*`**: Topic branches targeting `v4-dev` or `main` via reviewed Pull Requests.

---

## ⚡ Key Features & Implementation Audit

- 🛠️ **Full Moderation Suite**: Enterprise-grade moderation commands (`/warn`, `/warnings`, `/mute`, `/unmute`, `/kick`, `/ban`, `/unban`, `/modlogs`, `/purge`, `/slowmode`, `/lockdown`, `/unlock`) backed by PostgreSQL audit logging.
- 🚨 **Anti-Raid & Join Protection**: Automated account age verification (kicks accounts < 24h old), suspicious username pattern detection (5+ repeating characters), join rate-limiting (>5 joins per 10s), verification gate (`/verify`), and manual raid mode toggle (`/raidmode`).
- 📊 **Server Activity Dashboard**: Real-time server activity breakdown (`/dashboard`) querying audit events recorded over the last 24 hours.
- 🎮 **Interactive Cyber Breach RPG**: Penetration testing mini-game (`/hack`, `/profile`, `/leaderboard`, `/daily`) featuring tactical intrusion vectors, hacker rank progression (`Script Kiddie` → `Netrunner` → `Security Architect` → `Cyber Sovereign`), and PostgreSQL vault economy (`🪙`).
- 🤖 **Google Gemini 3.6 Flash AI**: Asynchronous AI commands (`/ask`, `/chat`, `/summarize`, `/translate`, `/explain`, `/code`) running stateful and stateless prompts on Java 26 Virtual Threads.
- 🔒 **Voice Channel Audio & DAVE E2EE Protocol**: Lavaplayer audio engine supporting Discord's Voice E2EE (DAVE/JDAVE protocol), Apple Music metadata resolution (`music.apple.com`), YouTube multi-client fallback pipeline, progress bars, and queue management (`/play`, `/skip`, `/stop`, `/queue`, `/nowplaying`, `/pause`, `/resume`).
- 🔍 **Security Recon & Permission Auditing**: Real-time server role permission risk audits (`/audit`), DNSSEC/SPF checks (`/dnsscan`), HTTP security headers (`/headers`), TLS cert inspection (`/ssl`), RFC 9116 security policy (`/securitytxt`), hash algorithm identification (`/hashid`), IP geolocation & ASN lookup (`/iplookup`), password entropy (`/passwordstrength`), DNSBL Spamhaus query (`/blacklist`), and passive subdomain enumeration via CT logs (`/subdomains`). All recon features use free/local resources — no external API keys required.
- 📚 **PostgreSQL Community Wiki & Database Manager**: Integrated security knowledgebase (`/wikiadd`) and high-concurrency HikariCP connection pooling.
- ⚡ **Async Virtual Thread Architecture**: Event loop built on Java 26 Virtual Threads for ultra-low latency gateway responsiveness under heavy concurrent guild loads.

For complete documentation on all bot commands and usage, see the **[Commands Guide](COMMANDS.md)**.

---

## 🏗️ Architecture & Package Structure

```
org.ashcrest/
├── core/             # AegisBot core, Virtual Threads executor, CommandListener & config
├── commands/         # Modular command pattern (audit, ashcrest, status, ask, hack, profile, leaderboard, daily, dashboard)
│   ├── antiraid/     # Anti-raid commands (raidmode, verify, automod, report)
│   ├── moderation/   # Moderation commands (warn, mute, ban, kick, purge, lockdown, slowmode)
│   └── music/        # Voice channel music commands (play, skip, stop, queue, etc.)
├── music/            # Lavaplayer audio engine, DAVE protocol config, Apple Music resolver & scheduler
├── security/         # Security audit & permission analysis engine
├── db/               # PostgreSQL DatabaseManager, EconomyManager & HikariCP pool
└── Main.java         # Bot initialization & lifecycle manager
```

---

## 📋 Requirements & Supported Java Version

- **Java JDK 26** (with `--enable-preview` Virtual Thread support enabled)
- **Apache Maven 3.8+**
- **PostgreSQL 14+**
- **Discord Bot Token** (with `GUILD_MESSAGES`, `MESSAGE_CONTENT`, `GUILD_MEMBERS`, and `GUILD_VOICE_STATES` intents enabled)

---

## 🔑 Environment Variables & Configuration

Configure Aegis using a `.env` file in the project root directory or system environment variables:

| Variable | Description | Required | Default / Example |
| -------- | ----------- | -------- | ----------------- |
| `DISCORD_TOKEN` | Discord Bot Application Token | **Yes** | `MTQ5...` |
| `DB_HOST` | PostgreSQL Database Host | **Yes** | `localhost` |
| `DB_PORT` | PostgreSQL Port | **Yes** | `5432` |
| `DB_NAME` | PostgreSQL Database Name | **Yes** | `ashcrest_aegis` |
| `DB_USER` | PostgreSQL Username | **Yes** | `aegis_user` |
| `DB_PASSWORD` | PostgreSQL Password | **Yes** | `your_secure_password` |
| `GEMINI_API_KEY` | Google Gemini AI API Key | Optional | `AIzaSy...` |
| `BOT_PREFIX` | Prefix for text-based commands | Optional | `!` |
| `BOT_STATUS` | Bot Discord presence status text | Optional | `🛡️ Aegis \| Ashcrest Operations` |

---

## 🚀 Quick Start & Building

```bash
# 1. Clone repository
git clone https://github.com/AshcrestHQ/Aegis.git
cd Aegis

# 2. Configure environment
cp .env.example .env
# Edit .env with your DISCORD_TOKEN and PostgreSQL credentials

# 3. Build shade executable JAR (v4.0.0)
mvn clean package

# 4. Execute bot
java --enable-preview -jar target/aegis-4.0.0.jar
```

For complete installation details and Discord Developer Portal setup, see the **[Setup Guide](SETUP.md)**.

---

## 🏢 Production Deployment

### Option A: Systemd Service Unit (Linux)

Create `/etc/systemd/system/aegis.service`:

```ini
[Unit]
Description=Aegis Discord Bot Engine (v4.0.0)
After=network.target postgresql.service

[Service]
Type=simple
User=aegis
WorkingDirectory=/opt/aegis
ExecStart=/usr/bin/java --enable-preview -jar /opt/aegis/target/aegis-4.0.0.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
EnvironmentFile=/opt/aegis/.env

[Install]
WantedBy=multi-user.target
```

Enable and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now aegis
```

### Option B: Docker Containerization

Build and run using Docker:

```bash
# Build image
docker build -t ashcresthq/aegis:4.0.0 .

# Run container with environment file
docker run -d \
  --name aegis-bot \
  --restart unless-stopped \
  --env-file .env \
  ashcresthq/aegis:4.0.0
```

---

## 🏷️ Repository Topics

`java` · `java-26` · `discord-bot` · `security` · `moderation` · `postgresql` · `open-source`

---

## 🤝 Community & Governance

- **[CONTRIBUTING.md](CONTRIBUTING.md)**: Developer setup, code standards, and PR guidelines.
- **[SECURITY.md](SECURITY.md)**: Security policy and vulnerability disclosure procedures.
- **[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)**: Contributor Covenant Code of Conduct.
- **[CHANGELOG.md](CHANGELOG.md)**: Release history and version migration notes.

---

## 📝 License

Released under the **MIT License**. Copyright (c) 2026 AshcrestHQ.
