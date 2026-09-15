# Agent context (living)

**Purpose:** Single source of truth so any agent (or human) can pick up this repo without chat history.

**Rule (SunVeda, all projects):** Update this file **at least once per active workday** while the project is the focus, and **immediately** on any major direction change (scope, stack, blockers, live URL, merge policy). Prefer a short dated entry at the top of Status over rewriting the whole file.

Do not put secrets here. Env var *names* and where they live are fine. Never log My Number, health payloads, or credentials.

---

## What this product is

**My Number Health & Wellness Portal** — native mobile monorepo (iOS SwiftUI + Android Kotlin/Compose) for Japan My Number–related health & wellness flows. Platforms share contracts, not UI.

Repo: https://github.com/sunveda/health-app

## Owner / bots

- Product owner: Sarveshwar Singh (SunVeda)
- Specialist bot: **Health App**
- Standards: **Chief of Engineering**
- Coordination: **Chief of Staff**
- As of **2026-09-15:** feature work **HELD**. Dual SunVeda active focus is **jkk-watch** + **Learn AI Now** LINE/WhatsApp automation. This repo: **CONTEXT hygiene only** until CoS resumes health-app.

## Stack (locked / scaffold)

- `apps/ios` — SwiftUI; Core adapters via composition root (NotConfigured for HealthKit, Keychain, biometrics, NFC, etc.)
- `apps/android` — Kotlin + Jetpack Compose; platform adapters via composition root (NotConfigured for Health Connect, Keystore, BiometricPrompt, NFC, etc.)
- `packages/contracts` — JSON Schemas; `packages/domain` — synthetic medical-expense eligibility fixtures
- Docs: architecture, security, privacy, wellness/clinical baselines, release checklist
- **Identity (My Number IdP):** deliberately **not wired** — blocked pending official provider docs, registration, compliance, threat-model review

## Current status (2026-09-15)

### `main` (approx.)

- Native app shells + Core/platform composition roots with NotConfigured adapters
- Security/privacy baseline docs and release checklist
- Wellness NotConfigured stubs and Clinical NotConfigured stubs previously merged (no real kit uploads / no real clinical pipeline)
- Production-readiness prep slices previously reviewed by CoE where applicable
- **Identity still blocked**

### Focus / merge policy

- **No new feature PRs** while health-app is held
- Allowed: `docs/CONTEXT.md` refresh, trivial docs/AGENTS hygiene, security fixes if owner asks
- Do not invent endpoints, scopes, or credentials for My Number / HealthKit / Health Connect / NFC

### Blockers before production path

1. Official My Number identity-provider integration (after compliance + security review)
2. Real HealthKit / Health Connect permission flows (not guessed)
3. Secure upload / clinical review pipeline (not stubs)
4. CoS resume of health-app eng track

## Non-goals / constraints

- No guessed production endpoints or permission scopes
- Sensitive identity and health data excluded from logs/analytics by default
- Do not expand into other SunVeda products from this repo while held

## Related docs

- [architecture.md](architecture.md)
- [security.md](security.md)
- [privacy-baseline.md](privacy-baseline.md)
- [wellness-baseline.md](wellness-baseline.md)
- [clinical-baseline.md](clinical-baseline.md)
- [release-checklist.md](release-checklist.md)
- Root [AGENTS.md](../AGENTS.md)
