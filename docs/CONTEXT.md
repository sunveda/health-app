# Agent context (living)

**Purpose:** Single source of truth so any agent (or human) can pick up this repo without chat history.

**Rule (SunVeda, all projects):** Update this file **at least once per active workday** while the project is the focus, and **immediately** on any major direction change (scope, stack, blockers, live URL, merge policy). Prefer a short dated entry at the top of Status over rewriting the whole file.

Do not put secrets here. Env var *names* and where they live are fine. Never log My Number, health payloads, or credentials.

---

## What this product is

**My Number Health & Wellness Portal** — client monorepo (iOS SwiftUI + Android Kotlin/Compose + **web React/Vite**) for Japan My Number–related health & wellness flows. Platforms share contracts, not UI.

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
- `apps/web` — React + Vite + TypeScript; Core adapters via composition root (NotConfigured identity/clinical; **Unavailable** HealthKit/Health Connect/NFC)
- `packages/contracts` — JSON Schemas; `packages/domain` — synthetic medical-expense eligibility fixtures
- Docs: architecture, security, privacy, wellness/clinical/**web** baselines, release checklist, **STACK**, **SPEC**
- **Identity (My Number IdP):** deliberately **not wired** — blocked pending official provider docs, registration, compliance, threat-model review
- Full version matrix: [STACK.md](STACK.md)

## Current status (2026-09-29)

### Direction change — docs & specification first (+ web client)

- Owner restart: prioritize documentation and specifications ([STACK.md](STACK.md), [SPEC.md](SPEC.md)). (#23)
- Owner add: **web app version** — specified in [web-baseline.md](web-baseline.md) and scaffolded under `apps/web` (fail-closed shell only). (#24)
- Web tab screenshots captured into [web-screens.md](web-screens.md) / `docs/images/web/`.
- **Still no Identity / kit / clinical / backend wiring** until the matching SPEC row is owner-approved.
- Allowed PRs: STACK/SPEC/baselines, fail-closed client shells, CONTEXT updates, owner-requested security fixes.
- Disallowed: inventing endpoints/scopes/credentials; real kit or IdP wiring.

### `main` (approx.)

- Native app shells + Core/platform composition roots with NotConfigured adapters
- Web shell (`apps/web`) — same fail-closed pattern (#24)
- Security/privacy baseline docs and release checklist
- Wellness / Clinical NotConfigured stubs (no real kit uploads / no clinical pipeline)
- **Identity still blocked**

### Spec writing order (active)

1. Confirm S0 STACK + S16 web baseline with owner  
2. S1 vision & non-goals → S2 IA/screens (all three clients) → S4/S5 security/privacy pass  
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
- [web-baseline.md](web-baseline.md) — web capability matrix
- [web-screens.md](web-screens.md) — web tab screenshots
- [architecture.md](architecture.md)
- [security.md](security.md)
- [privacy-baseline.md](privacy-baseline.md)
- [wellness-baseline.md](wellness-baseline.md)
- [clinical-baseline.md](clinical-baseline.md)
- [release-checklist.md](release-checklist.md)
- Root [AGENTS.md](../AGENTS.md)
