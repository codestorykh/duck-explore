# duck-explore

A Spring Boot payment orchestration and reconciliation service built with Java 21, enforcing strict engineering governance, null safety, and architecture boundary gates.

---

## Architecture Governance & Quality Gates

This repository enforces automated quality gates locally via Git hooks before code can be committed or pushed:

```text
[ Local Dev ]
      │
      ├──> git commit ──> [ pre-commit ] ──> NullAway / JSpecify (Compile safety)
      │                         │
      │                         └──> [ commit-msg ] ──> Jira prefix & Conventional format
      │
      └──> git push   ──> [ pre-push ] ────> ArchUnit Architecture Engine
                                │
                                └──> [ Remote GitHub Repository ]
```

## ArchUnit Policy Rules
The standalone architecture runner verifies production code under com.duck.explore:

Layer Isolation: Unidirectional flow: Controller -> Service -> Repository. Controllers cannot bypass Services to touch Repositories.

Injection Standards: Field injection (@Autowired on fields) is prohibited; constructor injection is required.

Stream & Logging Discipline: Direct calls to System.out or System.err are blocked; structured logging via SLF4J is mandatory.

Entity Protection: Domain JPA entities must not leak into Web/API controllers (DTOs only).

## Tech Stack & Prerequisites
JDK: OpenJDK 21 (Temurin / Homebrew OpenJDK 21)

Framework: Spring Boot 3.x

Build Tool: Apache Maven (wrapper included via ./mvnw)

Governance Tools: ArchUnit 1.3+, NullAway, ErrorProne

## Configure Local Git Hooks

```bash
# Point Git to the tracked hooks folder
git config core.hooksPath .githooks

# Grant execute rights (macOS / Linux)
chmod +x .githooks/*
```

## Emergency Bypass (Break-Glass Protocol)
```bash
# Skip pre-commit and commit-msg:
git commit --no-verify -m "HOTFIX-001: emergency bug fix"

# Skip pre-push architecture gate:
git push --no-verify origin <branch-name>
```