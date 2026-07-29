# 🛡️ Aegis - CyberHeaven Security Engineering Bot

> **Official Discord Bot for the CyberHeaven Security Education Community.**
> Aegis is a high-performance Discord security bot built with **Java 26** and **Virtual Threads**, engineered specifically for server protection, anti-raid monitoring, permission auditing, role verification, and security-education tools.

---

## ⚡ Key Features

- 🛡️ **Permission & Guild Security Auditing**: Real-time identification of dangerous permission allocations (`ADMINISTRATOR`, `MANAGE_ROLES`, `BAN_MEMBERS`) and privilege escalation risks.
- 🚨 **Anti-Raid & Guild Security Engine**: Non-blocking monitoring using Java 26 Virtual Threads for low latency under heavy raid loads.
- 🔐 **Role & Access Verification**: Modular schema support for verifying members prior to gaining access to security labs and CTF channels.
- 🗄️ **PostgreSQL Audit & Wiki**: Integrated audit logging and security knowledgebase powered by HikariCP connection pooling.
- 🤖 **Async Architecture**: Every event and command executes asynchronously without blocking Discord gateway threads.

---

## 🏗️ Architecture & Package Structure

```
org.cyberheaven/
├── core/             # AegisBot core, Virtual Threads executor & config
├── commands/         # Modular command pattern (audit, status, etc.)
├── security/         # Security audit, permission analysis & anti-raid engine
├── db/               # PostgreSQL DatabaseManager & HikariCP pool
└── Main.java         # Bot initialization & lifecycle manager
```

---

## 📋 Requirements

- **Java 26** (Virtual Thread support enabled)
- **PostgreSQL 14+**
- **Discord Bot Token** (with `GUILD_MESSAGES`, `MESSAGE_CONTENT`, and `GUILD_MEMBERS` intent enabled)

---

## 🛠️ Setup & Local Development

### 1. Clone
```bash
git clone https://github.com/Cyberheaven/Aegis.git
cd Aegis
```

### 2. Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Fill in your `DISCORD_TOKEN` and PostgreSQL credentials.

### 3. Build & Run
```bash
mvn clean package
java -jar target/aegis-2.0.0.jar
```

---

## 📝 License

MIT License - Copyright (c) 2026 CyberHeaven
