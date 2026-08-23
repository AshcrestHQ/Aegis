# Contributing to Aegis

Thank you for your interest in contributing to **Aegis**! We welcome contributions from developers, security researchers, and Discord community managers.

---

## 📜 Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please report any unacceptable behavior to security@ashcrest.org or open a confidential issue.

---

## 🌿 Branching Strategy

Aegis follows a structured branching strategy to maintain stability:

- **`main`**: The primary stable production branch. Code in `main` is always tested, verified, and aligned with current release milestones (e.g., `v4.0.0`).
- **`develop`**: The active integration branch for upcoming features and non-breaking enhancements.
- **`feature/<feature-name>` / `fix/<bug-name>`**: Topic branches created from `develop` for individual pull requests.

> **Note:** Direct pushes to `main` are restricted. All contributions must be submitted via Pull Requests targeting `main` (for critical security hotfixes) or `develop` (for standard development).

---

## 🛠️ Local Development Environment

### Prerequisites

- **Java JDK 26** (with preview features enabled for Virtual Threads)
- **Apache Maven 3.8+**
- **PostgreSQL 14+**

### Building and Testing

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AshcrestHQ/Aegis.git
   cd Aegis
   ```

2. **Configure local environment variables:**
   ```bash
   cp .env.example .env
   # Populate .env with test bot credentials & PostgreSQL connection info
   ```

3. **Compile and run unit/integration verification:**
   ```bash
   mvn clean verify
   ```

4. **Package shade executable JAR:**
   ```bash
   mvn clean package
   # Produces target/aegis-4.0.0.jar
   ```

---

## 📐 Coding Conventions & Formatting

- **Java Style Guide**: Follow standard Google Java Style Guide conventions.
- **Java 26 Features**: Leverage Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`), modern record classes, pattern matching, and text blocks where appropriate.
- **Command Architecture**: All new commands should implement `ICommand` and be registered in `AegisBot.java`.
- **Database Access**: Perform database queries asynchronously using virtual threads and HikariCP connection pooling (`DatabaseManager.java`). Avoid blocking gateway event handling threads.

---

## 📬 Submitting Pull Requests

1. Fork the repository and create your topic branch from `main` or `develop`.
2. Ensure your code compiles cleanly without warnings: `mvn clean package`.
3. Keep commits atomic, concise, and descriptive.
4. Open a Pull Request on GitHub with a detailed description of the changes and relevant issue references.
5. Automated CI checks will run against your pull request to verify compilation and build integrity.

---

## 🛡️ Security Vulnerabilities

For security vulnerability reports, please refer to our [Security Policy](SECURITY.md) and avoid filing public issues for unpatched vulnerabilities.
