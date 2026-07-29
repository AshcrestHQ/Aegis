# 📜 Aegis Bot Command Documentation

This document lists all available commands for **Aegis** (`org.cyberheaven`), their category, usage, permissions, and descriptions.

---

## ⚙️ System & Health Commands

| Command | Usage | Description | Slash Support |
|---|---|---|---|
| `status` | `!status` or `/status` | Displays Aegis bot system health, gateway ping latency, uptime, Java 26 version, and Virtual Thread execution status. | ✅ Yes |

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

## ⚡ Execution Architecture

- All commands execute **asynchronously** using Java 26 **Virtual Threads** via `CompletableFuture.runAsync()`.
- Commands do not block gateway websocket processing.
- Database queries use HikariCP non-blocking connection pooling.
