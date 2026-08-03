# 📜 Ashcrest Aegis Bot Command Documentation

This document lists all available commands for **Aegis** (`org.ashcrest`), their category, usage, permissions, and descriptions.

---

## 🎮 Cyber Operations RPG & Economy Commands

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `hack` | `!hack` or `/hack` | Initiates an interactive penetration test mini-game against target mainframes (`Quantum Vault`, `Satellite Array`, `AI Core`) using Discord Buttons. Earn Ashcrest Credits (`🪙`) and XP! | ✅ Yes |
| `profile` | `!profile` or `/profile` | Displays your hacker rank, level, XP progress bar, credit balance, win rate, and total breach stats. | ✅ Yes |
| `leaderboard` | `!leaderboard` or `/leaderboard` | Displays the top 10 global/server hackers ranked by level, XP, and credits with medal badges. | ✅ Yes |
| `daily` | `!daily` or `/daily` | Claim your daily 250 🪙 Ashcrest credits reward to fund hacking missions. | ✅ Yes |

---

## 🤖 AI Assistant Commands (Google Gemini)

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `ask` | `!ask <prompt>` or `/ask prompt:<string>` | Asks Aegis AI questions powered by Google Gemini 2.5 Flash API on Java 26 Virtual Threads. | ✅ Yes |

---

## 🛡️ Security & Permission Engineering Commands

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `audit` | `!audit` or `/audit` | Audits server permission configurations, elevated privilege users, and identifies dangerous roles (`ADMINISTRATOR`, `MANAGE_ROLES`, `BAN_MEMBERS`). | ✅ Yes |

---

## 📚 Community & Knowledge Base Commands (PostgreSQL)

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `wiki` | `!wiki <keyword>` or `/wiki` | Queries the Aegis PostgreSQL security knowledgebase for community wiki entries. | ✅ Yes |
| `wikiadd` | `!wikiadd <keyword> <content>` | Contributes or updates an entry in the community security wiki (*Staff/Admin restricted*). | ✅ Yes |

---

## 🎵 Voice Channel Music & Apple Music Commands

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `play` | `!play <input>` or `/play input:<string>` | Joins user's VC and plays/queues audio. Supports Apple Music links (`music.apple.com`), YouTube titles/URLs, and direct streams. | ✅ Yes |
| `skip` | `!skip` or `/skip` | Skips the currently playing track. | ✅ Yes |
| `stop` | `!stop` / `!leave` or `/stop` | Stops playback, clears the queue, and disconnects Aegis from VC. | ✅ Yes |
| `queue` | `!queue` or `/queue` | Displays the upcoming music queue list and now playing track. | ✅ Yes |
| `nowplaying` | `!nowplaying` or `/nowplaying` | Displays progress bar, time elapsed, and metadata for current track. | ✅ Yes |
| `pause` | `!pause` or `/pause` | Pauses audio playback. | ✅ Yes |
| `resume` | `!resume` or `/resume` | Resumes paused audio playback. | ✅ Yes |

---

## ⚙️ System & Community Info Commands

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `status` | `!status` or `/status` | Displays Aegis bot system health, gateway ping latency, uptime, Java 26 version, and Virtual Thread execution status. | ✅ Yes |
| `ashcrest` | `!ashcrest` or `/ashcrest` | Displays official AshcrestHQ community links, documentation, public bot invite link, and open-source repo details. | ✅ Yes |
| `source` / `aegis` | `!source` or `/source` | Displays GitHub repository link, license details, and open-source information. | ✅ Yes |
| `help` | `!help` or `/help` | Displays categorized command directory. | ✅ Yes |

---

## ⚡ Execution Architecture

- All commands execute **asynchronously** using Java 26 **Virtual Threads** via `CompletableFuture.runAsync()`.
- Interactive RPG buttons (`/hack`) process non-blocking state updates via `onButtonInteraction()`.
- Discord Voice connections support end-to-end encryption (DAVE/JDAVE protocol).
- Database operations utilize HikariCP connection pooling (`user_economy`, `aegis_wiki`, `aegis_audit_log`).
