# Agent context (living)

**Purpose:** Single source of truth so any agent (or human) can pick up this repo without chat history.

**Rule (SunVeda, all projects):** Update this file **at least once per active workday** while the project is the focus, and **immediately** on any major direction change (scope, stack, blockers, live URL, merge policy). Prefer a short dated entry at the top of Status over rewriting the whole file.

Do not put secrets here. Env var *names* and where they live are fine. Never log My Number, health payloads, or credentials.

---

## What this product is

**My Number Health & Wellness Portal** — native mobile monorepo (iOS SwiftUI + Android Kotlin/Compose) for Japan My Number–related health & wellness flows. Platforms (web planned, iOS, Android) share contracts, not UI.

Repo: https://github.com/sunveda/health-app

**Start here for tech:** [STACK.md](STACK.md) (one-screen stack). **Spec index:** [SPEC.md](SPEC.md).

## Owner / bots

- Product owner: Sarveshwar Singh (SunVeda)
- Specialist bot: **Health App**
- Standards: **Chief of Engineering**
- Coordination: **Chief of Staff**

## Stack (locked / scaffold)

- `apps/ios` — SwiftUI; Core adapters via composition root (NotConfigured for HealthKit, Keychain, biometrics, NFC, etc.)
- `apps/android` — Kotlin + Jetpack Compose; platform adapters via composition root (NotConfigured for Health Connect, Keystore, BiometricPrompt, NFC, etc.)
- `packages/contracts` — JSON Schemas; `packages/domain` — synthetic medical-expense eligibility fixtures
- Docs: architecture, security, privacy, wellness/clinical baselines, release checklist, **STACK**, **SPEC**
- **Identity (My Number IdP):** deliberately **not wired** — blocked pending official provider docs, registration, compliance, threat-model review
- Full version matrix: [STACK.md](STACK.md)

## Current status (2026-09-29)

### Direction change — docs & specification first

- Owner restart: **pause implementation**; prioritize documentation and specifications so the full stack is understandable from one screen ([STACK.md](STACK.md)) and work is tracked in [SPEC.md](SPEC.md). (#23)
- Previous hold (2026-09-15, dual focus jkk-watch + Learn AI Now) is superseded for this repo’s **docs track**: CONTEXT + STACK + SPEC hygiene is the active work.
- **Still no feature coding** (Identity, kits, clinical upload, backend) until the matching SPEC row is owner-approved.
- Allowed PRs: STACK/SPEC/architecture/security/privacy drafts, CONTEXT updates, trivial AGENTS/README links, owner-requested security fixes.
- Disallowed: inventing endpoints/scopes/credentials; new feature UI or kit wiring.

- **2026-10-17 (PR #28):** Added architecture & design docs: [system-architecture](architecture/system-architecture.md), [data-model](architecture/data-model.md), [api-contracts](architecture/api-contracts.md), [threat-model](security/threat-model.md), [ADRs 0001-0009](adr/README.md), [development guide](development.md). **Owner scope addition: ship as web + iOS + Android.** Web is **planned only** (`apps/web` not created); proposed stack Next.js + React + TS with BFF/httpOnly cookies (ADR-0008/0009, awaiting owner approval). Web has no HealthKit/Health Connect/NFC equivalent; Identity still blocked. Still no feature coding.
- **Draft specs added (pending owner review):** [product-spec.md](product-spec.md) (S1), [technical-spec.md](technical-spec.md), [mvp-screens.md](mvp-screens.md) (S2). Identity remains blocked; no feature coding.

### `main` (approx.)

- Native app shells + Core/platform composition roots with NotConfigured adapters
- Security/privacy baseline docs and release checklist
- Wellness / Clinical NotConfigured stubs (no real kit uploads / no clinical pipeline)
- Production-readiness prep slices previously reviewed by CoE where applicable
- **Identity still blocked**

### Spec writing order (active)

1. Confirm S0 STACK with owner  
2. S1 vision & non-goals → S2 IA/screens → S4/S5 security/privacy pass  
3. Then contracts plan, backend sketch, Identity/Wellness/Clinical shells (still fail closed)

### Blockers before production path

1. Official My Number identity-provider integration (after compliance + security review)
2. Real HealthKit / Health Connect permission flows (not guessed)
3. Secure upload / clinical review pipeline (not stubs)
4. Owner-approved specs (SPEC catalog) before each implementation stage

## Non-goals / constraints

- No guessed production endpoints or permission scopes
- Sensitive identity and health data excluded from logs/analytics by default
- Do not expand into other SunVeda products from this repo while in docs-first mode
- Do not treat docs-first as permission to sketch fake IdP or upload URLs “for completeness”

## Related docs

- [STACK.md](STACK.md) — **one-screen tech map**
- [SPEC.md](SPEC.md) — specification index & writing order
- [architecture.md](architecture.md)
- [architecture/system-architecture.md](architecture/system-architecture.md), [data-model](architecture/data-model.md), [api-contracts](architecture/api-contracts.md)
- [security/threat-model.md](security/threat-model.md), [adr/README.md](adr/README.md), [development.md](development.md)
- [security.md](security.md)
- [privacy-baseline.md](privacy-baseline.md)
- [wellness-baseline.md](wellness-baseline.md)
- [clinical-baseline.md](clinical-baseline.md)
- [release-checklist.md](release-checklist.md)
- Root [AGENTS.md](../AGENTS.md)
