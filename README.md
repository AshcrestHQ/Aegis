# 🛡️ Aegis - CyberHeaven Security Engineering Bot

> **Official Discord Bot for the CyberHeaven Security Education Community.**
> Aegis is a high-performance Discord security bot built with **Java 26** and **Virtual Threads**, engineered specifically for server protection, anti-raid monitoring, permission auditing, role verification, and security-education tools.

---

## 🦅 Heritage & Tribute to Apex

> *From the ashes of Apex, Aegis arises.*
> 
> Aegis carries forward the foundational architectural lessons, high-throughput non-blocking design, and core principles born in [Apex](https://github.com/medhansh5/Apex). While Apex laid the groundwork for high-performance Discord utility processing, Aegis elevates the vision into dedicated security engineering, zero-trust server protection, and community verification for **CyberHeaven**.

---

## ⚡ Key Features

- 🛡️ **Permission & Guild Security Auditing**: Real-time identification of dangerous permission allocations (`ADMINISTRATOR`, `MANAGE_ROLES`, `BAN_MEMBERS`) and privilege escalation risks.
- 🚨 **Anti-Raid & Guild Security Engine**: Non-blocking monitoring using Java 26 Virtual Threads for ultra-low latency under heavy raid loads.
- 🔐 **Role & Access Verification**: Modular schema support for verifying members prior to gaining access to security labs and CTF channels.
- 🗄️ **PostgreSQL Audit & Wiki**: Integrated audit logging and security knowledgebase powered by HikariCP connection pooling.
- 🤖 **Async Architecture**: Every event and command executes asynchronously without blocking Discord gateway threads.

For complete documentation on all bot commands and usage, see the **[Commands Guide](COMMANDS.md)**.

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

For complete detailed installation, environment setup, and Discord developer portal configuration, see the **[Setup Guide](SETUP.md)**.

### Quick Start:

```bash
# 1. Clone
git clone https://github.com/Cyberheaven/Aegis.git
cd Aegis

# 2. Configuration
cp .env.example .env
# Edit .env with your DISCORD_TOKEN and PostgreSQL credentials

# 3. Build & Run
mvn clean package
java --enable-preview -jar target/aegis-2.0.0.jar
```

---

## 📝 License

Released under the **MIT License**.

```
MIT License

Copyright (c) 2026 CyberHeaven

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
