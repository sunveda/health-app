# AGENTS.md — health-app

Operating notes for any coding agent working in this repository.

## Read first

1. **[docs/CONTEXT.md](docs/CONTEXT.md)** — living product status, blockers, who owns what. **Update it** on every active workday and on every major direction change (SunVeda house rule for all projects).
2. [README.md](README.md) — monorepo layout and native strategy
3. [docs/architecture.md](docs/architecture.md), [docs/security.md](docs/security.md), [docs/privacy-baseline.md](docs/privacy-baseline.md), [docs/release-checklist.md](docs/release-checklist.md)

## Product in one line

Native iOS/Android My Number Health & Wellness Portal shells with NotConfigured adapters — contracts shared, UI not shared.

## Engineering rules

- Keep platform capability access behind CompositionRoot adapters; screens must not import HealthKit / Health Connect / NFC / Keychain APIs directly
- Do not invent IdP endpoints, upload URLs, or API keys
- Secrets and signing material only via secure stores / CI secrets — never commit
- Prefer small PRs; Dependabot noise is OK to batch with CoE guidance
- Coordinate standards with **Chief of Engineering**; product ownership with bot **Health App**
- As of 2026-09-15: eng is paused — CONTEXT/docs only unless owner reopens

## Context hygiene (required)

After meaningful work (feature merge, blocker change, toolchain decision, scope pivot):

1. Update the dated **Current status** section in `docs/CONTEXT.md`
2. Keep bullets short; link PRs by number
3. If you only ship code and skip CONTEXT, the next agent starts blind — treat that as a bug
