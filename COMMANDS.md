# 🛡️ Aegis Command Reference & Directory (v3.0.0+)

Welcome to the master command directory for **AshcrestHQ Aegis**. All commands support both traditional prefix execution (`!command`) and native Discord Slash Commands (`/command`).

---

## 📂 Command Categories

### 🛡️ Security Recon & Audit

| Command | Usage | Description |
| :--- | :--- | :--- |
| `dnsscan` | `!dnsscan <domain>` or `/dnsscan domain:<string>` | Audit DNS A, AAAA, MX, NS, TXT, SPF (`v=spf1`), DMARC (`_dmarc`), and DNSSEC (`DNSKEY`). |
| `headers` | `!headers <url>` or `/headers url:<string>` | Audit HTTP security response headers (CSP, HSTS, X-Frame-Options, X-Content-Type-Options, Referrer-Policy). |
| `ssl` | `!ssl <domain>` or `/ssl domain:<string>` | Inspect TLS/SSL certificate CN, issuer, valid dates, and expiration days. |
| `securitytxt` | `!securitytxt <domain>` or `/securitytxt domain:<string>` | Check RFC 9116 security vulnerability policy (`/.well-known/security.txt`). |
| `hashid` | `!hashid <hash>` or `/hashid hash:<string>` | Identify probable cryptographic hash algorithm (MD5, SHA-1, SHA-256, bcrypt, etc.). |
| `iplookup` | `!iplookup <ip>` or `/iplookup ip:<string>` | Passive IP geolocation and ASN provider registry lookup. |
| `passwordstrength` | `/passwordstrength password:<string>` | Ephemerally test password bit entropy and brute-force resistance (never logged or saved). |
| `audit` | `!audit` or `/audit` | Audit server roles, elevated administrator privileges, and misconfigurations. |

---

### 🔨 Moderation Engine

| Command | Usage | Description |
| :--- | :--- | :--- |
| `warn` | `!warn <@user> <reason>` or `/warn user:<user> reason:<text>` | Issue formal warning. (3 active warnings trigger automatic 1-hour timeout). |
| `warnings` | `!warnings <@user>` or `/warnings user:<user>` | View member warning case history. (Use `/delwarn case_id:<id>` to revoke a warning). |
| `mute` | `!mute <@user> <minutes> <reason>` or `/mute user:<user> minutes:<int>` | Timeout member natively in Discord for up to 28 days. |
| `unmute` | `!unmute <@user>` or `/unmute user:<user>` | Remove timeout from member. |
| `kick` | `!kick <@user> <reason>` or `/kick user:<user> reason:<text>` | Kick member from server with DM notice. |
| `ban` | `!ban <@user> <reason>` or `/ban user:<user> reason:<text>` | Ban user from server or unban by user ID (`/unban user_id:<id>`). |
| `modlogs` | `!modlogs <@user>` or `/modlogs user:<user>` | View full moderation case history log for a member. |
| `purge` | `!purge <count>` or `/purge count:<int>` | Bulk delete up to 100 recent channel messages in a batch. |
| `slowmode` | `!slowmode <seconds>` or `/slowmode seconds:<int>` | Set channel message slowmode delay in seconds (0 to disable). |
| `lockdown` | `!lockdown` or `/lockdown` / `/unlock` | Restrict or restore message sending permissions for `@everyone`. |

---

### 🚨 Anti-Raid & Onboarding

| Command | Usage | Description |
| :--- | :--- | :--- |
| `raidmode` | `!raidmode on\|off` or `/raidmode enabled:<boolean>` | Lock server verification level to Highest during active raid attacks. |
| `verify` | `!verify` or `/verify` | Post button-based human verification gate for new member onboarding. |
| `automod` | `!automod banned-words add <word>` or `/automod` | Manage banned words filter and chat spam flood rate limits. |
| `report` | `!report <@user> <reason>` or `/report user:<user> reason:<text>` | Report a user to server moderators via mod log channel. |
| `config` | `!config view` or `/config` | Manage per-guild settings (modlog channel, welcome channel/message, autorole, automod). |

---

### 🤖 AI Assistant Suite (Google Gemini 3.6 Flash)

| Command | Usage | Description |
| :--- | :--- | :--- |
| `ask` | `!ask <prompt>` or `/ask prompt:<string>` | Single-shot AI assistant query. |
| `chat` | `!chat <message>` or `/chat message:<string>` | Stateful multi-turn conversation maintaining channel sliding context. |
| `summarize` | `!summarize <text>` or `/summarize text:<string>` | Summarize long articles into bulleted key takeaways. |
| `translate` | `!translate <lang> <text>` or `/translate target_lang:<string> text:<string>` | Translate text into target language. |
| `explain` | `!explain <topic>` or `/explain topic:<string>` | Explain complex technical concepts, errors, or code snippets. |
| `code` | `!code <question>` or `/code question:<string>` | Software engineering and programming Q&A assistant. |

---

### 🎮 Cyber Operations RPG & Vault Economy

| Command | Usage | Description |
| :--- | :--- | :--- |
| `hack` | `!hack` or `/hack` | Initiate interactive terminal penetration test mini-game against target mainframes. |
| `profile` | `!profile` or `/profile` | View hacker rank (`Script Kiddie` → `Cyber Sovereign`), level, XP progress bar, credits (`🪙`), and win rate. |
| `leaderboard` | `!leaderboard` or `/leaderboard` | View top 10 global/server hacker leaderboard with leader badges. |
| `daily` | `!daily` or `/daily` | Claim daily 250 🪙 Ashcrest credits reward. |

---

### 🎉 Fun & Utilities

| Command | Usage | Description |
| :--- | :--- | :--- |
| `coinflip` | `!coinflip` or `/coinflip` | Flip a coin (Heads or Tails). |
| `roll` | `!roll <sides>` or `/roll sides:<int>` | Roll a die with optional custom side count. |
| `8ball` | `!8ball <question>` or `/8ball question:<string>` | Ask the Magic 8-Ball a question. |
| `joke` | `!joke` or `/joke` | Get a programming joke. |
| `meme` | `!meme` or `/meme` | Get a cyber meme image. |

---

### 📚 Community Wiki

| Command | Usage | Description |
| :--- | :--- | :--- |
| `wikiadd` | `!wikiadd <keyword> <content>` or `/wikiadd keyword:<key> content:<val>` | Add or update an entry in the PostgreSQL community security wiki. |

---

### 🎵 Voice Channel Music Audio

| Command | Usage | Description |
| :--- | :--- | :--- |
| `play` | `!play <input>` or `/play input:<string>` | Play audio in VC (supports YouTube fallback, Apple Music metadata, direct audio URLs). |
| `skip` | `!skip` or `/skip` | Skip the currently playing track. |
| `stop` | `!stop` or `/stop` | Stop playback, clear queue, and disconnect bot from VC. |
| `queue` | `!queue` or `/queue` | Display current music queue and now playing track. |
| `nowplaying` | `!nowplaying` or `/nowplaying` | Show progress bar and details of currently playing track. |
| `pause` | `!pause` or `/pause` | Pause audio playback. |
| `resume` | `!resume` or `/resume` | Resume paused audio playback. |

---

### ℹ️ System & Community Info

| Command | Usage | Description |
| :--- | :--- | :--- |
| `status` | `!status` or `/status` | View gateway ping, uptime, Java 26 status, and system health. |
| `ashcrest` | `!ashcrest` or `/ashcrest` | View official AshcrestHQ community links, bot invite, and open-source repo details. |
| `source` | `!source` or `/source` | View open-source GitHub repository details (`AshcrestHQ/Aegis`). |
| `cyberheaven` | `!cyberheaven` or `/cyberheaven` | Legacy community alias (use `/ashcrest` or `/source`). |
