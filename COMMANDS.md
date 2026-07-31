# 📜 Aegis Bot Command Documentation

This document lists all available commands for **Aegis** (`org.cyberheaven`), their category, usage, permissions, and descriptions.

---

## ⚙️ System & Health Commands

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `status` | `!status` or `/status` | Displays Aegis bot system health, gateway ping latency, uptime, Java 26 version, and Virtual Thread execution status. | ✅ Yes |

---

## 🤖 AI Assistant Commands (Google Gemini)

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `ask` | `!ask <prompt>` or `/ask prompt:<string>` | Asks Aegis AI questions powered by Google Gemini 2.5 Flash API. | ✅ Yes |

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
| `learn` | `!learn <keyword> <content>` | Contributes or updates an entry in the community security wiki. | ✅ Yes |

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

## ⚡ Execution Architecture

- All commands execute **asynchronously** using Java 26 **Virtual Threads** via `CompletableFuture.runAsync()`.
- Commands do not block gateway websocket processing.
- Database queries use HikariCP non-blocking connection pooling.
- Apple Music links resolve metadata asynchronously using iTunes REST API and web OpenGraph meta parsing, automatically finding streaming sources.

