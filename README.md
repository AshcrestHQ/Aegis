# 🛡️ Aegis - AshcrestHQ Universal Discord Operations Bot

> **Universal Discord Bot for Security, Community Operations, Cyber RPG, AI & VC Audio.**
> Aegis is a high-performance, public Discord bot built with **Java 26** and **Virtual Threads**, engineered for server protection, anti-raid monitoring, permission auditing, community security wiki, interactive Cyber RPG mini-games, Google Gemini 3.6 Flash AI assistant, and full VC music playback.
> 
> 🔗 **[Click Here to Invite Aegis to Your Discord Server](https://discord.com/oauth2/authorize?client_id=1497620745604563096&permissions=824938978566&integration_type=0&scope=applications.commands+bot)**

---

## 🦅 Heritage & Universal Mission

> *From the ashes of Apex, Aegis arises.*
> 
> Aegis carries forward the foundational architectural lessons, high-throughput non-blocking design, and core principles born in [Apex](https://github.com/medhansh5/Apex). Now evolved into a universal, public bot hosted by **AshcrestHQ** (`AshcrestHQ/Aegis`), Aegis is designed for any Discord community to invite, deploy, and utilize for security, AI assistance, voice channel audio, and interactive cyber gaming.

---

## ⚡ Key Features

- 🎮 **Interactive Cyber Breach RPG**: High-octane cyber security penetration testing mini-game (`/hack`, `/profile`, `/leaderboard`, `/daily`) featuring interactive Discord Buttons, tactical intrusion vectors, hacker rank progression (`Script Kiddie` → `Netrunner` → `Security Architect` → `Cyber Sovereign`), and PostgreSQL vault economy (`🪙`).
- 🤖 **Google Gemini AI Assistant**: Native `/ask` command powered by Google Gemini 3.6 Flash API executing asynchronously on Java 26 Virtual Threads.
- 🔒 **Discord Voice DAVE E2EE & Lavaplayer Audio**: Full-featured Voice Channel music engine supporting Discord's end-to-end voice encryption (DAVE/JDAVE protocol), Apple Music metadata resolution (`music.apple.com`), YouTube multi-client fallback pipeline (`Web`, `Music`, `WebEmbedded`, `AndroidVr`, `Tv`, `Ios`, `Android`), interactive progress bars, and queue management.
- 🛡️ **Security Recon & Permission Auditing**: Real-time permission risk audits (`/audit`), DNSSEC & SPF policy checks (`/dnsscan`), HTTP security headers grading (`/headers`), TLS cert inspection (`/ssl`), RFC 9116 checks (`/securitytxt`), hash algorithm identification (`/hashid`), IP geolocation & ASN lookup (`/iplookup`), ephemeral password entropy analysis (`/passwordstrength`), DNSBL Spamhaus blacklist queries (`/blacklist`), NVD CVE vulnerability lookups (`/cve`), and passive subdomain enumeration via Certificate Transparency (`/subdomains`). Note: `/breach` and `/phishcheck` require their respective API keys (`HIBP_API_KEY`, `GOOGLE_SAFE_BROWSING_API_KEY`) if enabled.
- 📚 **PostgreSQL Community Wiki**: Integrated security knowledgebase and audit logging powered by HikariCP connection pooling (`/wikiadd`).
- ⚡ **Async Virtual Thread Architecture**: Non-blocking event loop running on Java 26 Virtual Threads for ultra-low latency gateway responsiveness under heavy server load.

For complete documentation on all bot commands and usage, see the **[Commands Guide](COMMANDS.md)**.

---

## 🏗️ Architecture & Package Structure

```
org.ashcrest/
├── core/             # AegisBot core, Virtual Threads executor & config
├── commands/         # Modular command pattern (audit, ashcrest, status, ask, hack, profile, leaderboard, daily)
│   └── music/        # Voice channel music commands (play, skip, stop, queue, etc.)
├── music/            # Lavaplayer audio engine, DAVE protocol config, Apple Music resolver & scheduler
├── security/         # Security audit, permission analysis & anti-raid engine
├── db/               # PostgreSQL DatabaseManager, EconomyManager & HikariCP pool
└── Main.java         # Bot initialization & lifecycle manager
```

---

## 📋 Requirements

- **Java 26** (Virtual Thread support enabled)
- **PostgreSQL 14+**
- **Discord Bot Token** (with `GUILD_MESSAGES`, `MESSAGE_CONTENT`, `GUILD_MEMBERS`, and `GUILD_VOICE_STATES` intents enabled)

---

## 🛠️ Setup & Local Development

For complete detailed installation, environment setup, and Discord developer portal configuration, see the **[Setup Guide](SETUP.md)**.

### Quick Start:

```bash
# 1. Clone
git clone https://github.com/AshcrestHQ/Aegis.git
cd Aegis

# 2. Configuration
cp .env.example .env
# Edit .env with your DISCORD_TOKEN and PostgreSQL credentials

# 3. Build & Run
mvn clean package
java --enable-preview -jar target/aegis-3.0.0.jar
```

---

## 📝 License

Released under the **MIT License**.

```
MIT License

Copyright (c) 2026 AshcrestHQ

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
