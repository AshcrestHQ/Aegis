# Changelog

All notable changes to **Aegis** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [4.0.0] - 2026-08-23

### Added
- **Moderation Suite**: Full suite of moderation commands including `/warn`, `/warnings`, `/mute`, `/unmute`, `/kick`, `/ban`, `/unban`, `/modlogs`, `/purge`, `/slowmode`, `/lockdown`, and `/unlock`.
- **Anti-Raid System**: Added `/raidmode`, `/verify` (interactive button verification gate), `/automod`, and `/report` functionality.
- **Certificate Transparency Subdomain Discovery**: Added `/subdomains` command utilizing crt.sh passive certificate logs.
- **Repository Standards & Infrastructure**:
  - Community governance files (`CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md`).
  - GitHub issue templates for bug reports, feature requests, and security issues.
  - Dependabot automated dependency updates configuration (`.github/dependabot.yml`).
  - Automated release workflow (`.github/workflows/release.yml`).
  - CI status badge and comprehensive production deployment guide.

### Changed
- **Java 26 Virtual Thread Engine**: Refactored command dispatch loop to leverage Java 26 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`) for non-blocking execution under high gateway event load.
- **Discord JDA 6.5.0 Upgrade**: Integrated DAVE (Discord Audio Very Encrypted) E2EE protocol support with `jdave-api`.
- **AI Integration**: Updated stateful assistant and `/ask` commands to Google Gemini 3.6 Flash engine.
- **Project Version Alignment**: Promoted project version from `3.0.0` to `4.0.0` across pom.xml and all documentation.

### Fixed
- Fixed YouTube audio search HTTP 400 cipher resolution via `dev.lavalink.youtube` source plugin integration.
- Resolved PostgreSQL database connection pool initialization under high concurrent request spikes using HikariCP optimization.

---

## [3.0.0] - 2026-02-15

### Added
- Interactive Cyber Breach RPG mini-game (`/hack`, `/profile`, `/leaderboard`, `/daily`) with credit economy.
- Security recon toolkit (`/audit`, `/dnsscan`, `/headers`, `/ssl`, `/securitytxt`, `/hashid`, `/iplookup`, `/passwordstrength`, `/breach`, `/blacklist`, `/phishcheck`, `/cve`).
- PostgreSQL community security wiki (`/wikiadd`).
- Voice Channel audio playback engine via Lavaplayer with Apple Music metadata resolution.
