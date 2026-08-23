# Security Policy - Aegis

Security is at the core of **Aegis**. We appreciate the effort of security researchers and developers in keeping our bot engine and Discord communities safe.

---

## 🛡️ Supported Versions

We actively maintain and provide security updates for the following versions of Aegis:

| Version | Supported          | Security Maintenance Status           |
| ------- | ------------------ | ------------------------------------- |
| 4.x     | :white_check_mark: | Active production releases & security patches |
| 3.x     | :x:                | Deprecated / EOL                     |
| < 3.0   | :x:                | Unsupported                          |

---

## 🔒 Reporting a Vulnerability

**Do NOT report security vulnerabilities in public GitHub issues, discussions, or Discord channels.**

If you discover a security flaw or vulnerability in Aegis:

1. Use [GitHub Private Vulnerability Reporting](https://github.com/AshcrestHQ/Aegis/security/advisories/new) if available on the repository.
2. Alternatively, email the AshcrestHQ Security Team directly at **security@ashcrest.org**.

### What to Include in Your Report

To help us investigate and resolve the issue quickly, please include:

- Description of the vulnerability and potential impact.
- Steps to reproduce or Proof of Concept (PoC).
- Affected module/component (e.g., Command Execution, Database Access, OAuth / Gateway Handler).
- Any proposed remediation or patch if available.

---

## ⏱️ Response & Disclosure Timeline

- **Acknowledgement**: Within **24 to 48 hours** of receiving your report.
- **Triage & Assessment**: Within **5 business days** to confirm and determine severity.
- **Fix & Patch Release**: Security updates will be published to `main` as a patch release (e.g., `v4.0.1`).
- **Public Disclosure**: Coordinated after the fix has been deployed and released.

---

## 🚨 Security Best Practices for Operators

When hosting an instance of Aegis in production:

1. **Environment Credentials**: Never commit `.env` or hardcode tokens (`DISCORD_TOKEN`, `DB_PASSWORD`, `GEMINI_API_KEY`).
2. **Database Permissions**: Use a dedicated PostgreSQL database user (`aegis_user`) with restricted privileges.
3. **Java Version**: Maintain runtime on **Java 26 JDK** with security updates applied.
4. **Privileged Gateway Intents**: Only enable intents required by your deployment configuration.
