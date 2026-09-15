# Agent context (living)

**Purpose:** Single source of truth so any agent (or human) can pick up this repo without chat history.

**Rule (SunVeda, all projects):** Update this file **at least once per active workday** while the project is the focus, and **immediately** on any major direction change (scope, stack, blockers, live URL, merge policy). Prefer a short dated entry at the top of Status over rewriting the whole file.

Do not put secrets here. Env var *names* and where they live are fine. My Number credentials, HealthKit/Health Connect payloads, and signing secrets never belong in git or in this file.

---

## What this product is

Private SunVeda native mobile monorepo: **My Number Health & Wellness Portal** — iOS and Android clients that share contracts/domain fixtures, not UI. Each platform uses native security, health-data, NFC, and accessibility APIs through a composition-root adapter pattern.

Repo: https://github.com/sunveda/health-app

## Owner / bots

- Product owner: Sarveshwar Singh (SunVeda)
- Specialist bot: **Health App**
- Standards: **Chief of Engineering**
- Coordination: **Chief of Staff**

## Stack (known)

- Monorepo: `apps/ios` (SwiftUI), `apps/android` (Kotlin + Jetpack Compose), `packages/contracts`, `packages/domain`, reserved `packages/api`
- pnpm workspace for shared JS packages (contracts/domain tooling); Prettier + TypeScript for package tooling
- iOS: SwiftUI + Core adapters (HealthKit, Keychain, biometrics, NFC) — currently **NotConfigured** stubs
- Android: Compose, compile SDK 35 / min 26 on `main`; Health Connect / Keystore / BiometricPrompt / NFC via platform adapters — **NotConfigured** stubs
- CI: forbidden-pattern gates, unit tests; Dependabot for Gradle / Actions / TypeScript

## Current status (2026-09-15)

### Engineering focus

- **Paused / held.** Active SunVeda eng focus is **jkk-watch** (+ Learn AI Now, no repo). This repo is **CONTEXT-only** until the owner reopens Health App eng.
- Do **not** wire My Number IdP, HealthKit/Health Connect, NFC, upload endpoints, or Vertex AI against guessed credentials while paused.

### `main` (as of last product merges)

- Foundation hygiene: docs, CompositionRoot + NotConfigured stubs, tests, CI gates
- Security/privacy baseline (stage 1.5): consent/crash/telemetry stubs + docs
- Production-readiness prep: synthetic domain eligibility fixtures, TestFlight/Play checklist, CI simulator UDID fix
- Wellness NotConfigured stubs (HealthDataSource + sync client)
- Clinical NotConfigured stubs (upload/review surface; no picker/network)

### Open PRs (held)

- **PR #15** — https://github.com/sunveda/health-app/pull/15 — `cursor/android-toolchain-upgrade-dd04` — Android toolchain modernization (AGP 9.4.0, Kotlin 2.4.10, Gradle 9.7.1, SDK 37). Open; not merged; treat as held while eng paused.
- Dependabot: #3–#6, #9, #16–#19 (Actions / Gradle / Compose BOM / Kotlin / TypeScript)

### Blockers / next when reopened

1. Official My Number IdP docs + app registration + threat-model review before any identity wiring
2. HealthKit / Health Connect permission flows only after product/legal consent copy
3. Bundle id `com.sunveda.healthapp` frozen; signing ownership TBD (see release checklist)
4. Decide fate of PR #15 (toolchain) vs Dependabot #9 overlap with CoE

## Non-goals / constraints

- No invented production endpoints or credentials
- Feature screens must not import platform health/security frameworks directly — go through Core/platform adapters
- Sensitive identity and health data excluded from logs/analytics by default

## Related docs

- [Architecture](architecture.md), [Security](security.md), [Privacy baseline](privacy-baseline.md)
- [Wellness baseline](wellness-baseline.md), [Clinical baseline](clinical-baseline.md), [Release checklist](release-checklist.md)
- Root [AGENTS.md](../AGENTS.md) — agent operating rules
- Root [README.md](../README.md)
