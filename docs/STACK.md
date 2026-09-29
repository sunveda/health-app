# Stack at a glance

**One screen for the whole product.** Read this first to see every technology in play — shipped today vs target later. No secrets. Detail lives in linked docs.

**Product:** My Number Health & Wellness Portal — native iOS + Android + **web**; share **contracts**, not UI.  
**Repo:** https://github.com/sunveda/health-app · **Bundle / app ID:** `com.sunveda.healthapp` · **Version:** `0.1.0`  
**Phase (2026-09-29):** Docs & specification first — shells allowed; no Identity/kit/clinical wiring until specs are owner-approved.

---

## System map

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│  CLIENTS (in repo today)                                                    │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐                   │
│  │ iOS · SwiftUI  │ │ Android · Compose│ │ Web · React    │                   │
│  │ Swift 5.9      │ │ Kotlin 2.0       │ │ Vite + TS      │                   │
│  │ iOS 17+        │ │ minSdk 26 / 35   │ │ React 18       │                   │
│  │ CompositionRoot│ │ CompositionRoot │ │ CompositionRoot│                   │
│  └───────┬────────┘ └───────┬────────┘ └───────┬────────┘                   │
│          └──────────────────┼──────────────────┘                            │
│                             ▼                                               │
│               packages/contracts  (JSON Schema)                             │
│               packages/domain     (Python fixtures; not imported by apps)   │
│               packages/api        (reserved — empty)                        │
└─────────────────────────────────────────────────────────────────────────────┘
                          │  (not wired)
                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  TARGET CLOUD (specified, not deployed)                                     │
│  Cloud Run · Firestore · Vertex AI · Cloud KMS (CMEK)                       │
│  VPC Service Controls · Cloud Armor · managed secrets                       │
│  Services (split): identity · health · clinical · expenses · AI · exports   │
└─────────────────────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  EXTERNAL / OFFICIAL (blocked until registration + compliance)              │
│  My Number IdP (OIDC + PKCE) · Digital Authentication App / NFC (mobile)    │
│  Apple HealthKit · Google Health Connect (mobile only)                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Client stacks (versions from repo)

| Layer | iOS (`apps/ios`) | Android (`apps/android`) | Web (`apps/web`) |
|---|---|---|---|
| UI | SwiftUI | Jetpack Compose + Material 3 | React **18.3** |
| Language | Swift **5.9** | Kotlin **2.0.21** | TypeScript **5.9** |
| OS / runtime | iOS **17.0** | minSdk **26**, target/compile **35** | Modern evergreen browsers |
| Tooling | XcodeGen, Xcode **15+** | Gradle **8.10**, AGP **8.7.3**, JDK **17** | Vite **6**, pnpm **10.12** |
| Compose / bundler | — | Compose BOM `2025.02.00` | `@vitejs/plugin-react` |
| Entry | `HealthAppApp` → `CompositionRoot` | `MainActivity` → `CompositionRoot` | `main.tsx` → `createCompositionRoot` |
| Features | Tab shells: home, health, expenses, settings | Same tab shells | Same tab shells |
| Tests | XCTest | JUnit 4 | Vitest |
| Signing / host | CI `CODE_SIGNING_ALLOWED=NO` | Debug assemble; Play TBD | Static build; hosting TBD |

**Not used:** Expo, React Native, Flutter, shared UI kits across platforms, guessed IdP/API SDKs.

**Web ≠ mobile kits:** HealthKit, Health Connect, and NFC are **Unavailable** on web (see [web-baseline.md](web-baseline.md)).

---

## Shared monorepo packages

| Path | Tech | Role today |
|---|---|---|
| `packages/contracts` | JSON Schema (draft 2020-12) | Live portal contract: identity, wellnessSync, medicalExpenses |
| `packages/domain` | Python 3 + unittest | Synthetic medical-expense eligibility fixtures (**not** tax-law advice; apps do **not** import at runtime) |
| `packages/api` | README only | Reserved for versioned transport clients (all surfaces) |
| Root workspace | pnpm **10.12.1**, Prettier, TypeScript (dev) | Workspace for `apps/web` + format hooks |

---

## Platform capabilities (fail closed)

All sensitive capabilities go through **Core / platform adapters** built only by `CompositionRoot`. Feature screens must not import kit or browser credential APIs directly.

| Capability | iOS / Android | Web | Status |
|---|---|---|---|
| Health data | HealthKit / Health Connect | — | Mobile **NotConfigured**; web **Unavailable** |
| Secure storage | Keychain / Keystore | httpOnly cookie / Web Crypto (later) | **NotConfigured** |
| Local unlock | Biometrics (+ passkeys later) | WebAuthn (later) | **NotConfigured** |
| Card / NFC | CoreNFC + official path | — | Mobile **NotConfigured**; web **Unavailable** |
| Consent / crash / telemetry | Policy stubs | Policy stubs | **NotConfigured** (no SDK) |
| Wellness sync | Feature flag off | Feature flag off | Stub |
| Clinical upload | Pipeline stubs | Pipeline stubs | Stub; no network URLs |
| Identity session | OIDC + PKCE | OIDC + PKCE (browser redirect) | **Not wired** |

---

## Backend & identity (target only)

| Concern | Technology | Notes |
|---|---|---|
| Compute | Google Cloud Run | Containerized services; not deployed |
| Metadata store | Firestore | User health metadata / longitudinal profiles |
| AI | Vertex AI | Grounded summaries only; safety disclaimers required |
| Keys | Cloud KMS + CMEK | Partition sensitive identifiers |
| Perimeter | VPC Service Controls, Cloud Armor | Ingress / exfil controls |
| Secrets | Managed secret store + env injection | Names in `.env.example` only |
| Identity | Official My Number IdP · OIDC · PKCE · `state`/`nonce` | Scopes/endpoints **not invented**; web needs registered redirect URI |
| App identity key | Pairwise subject (`pairwiseSub`) | Individual number isolated; never a general client key |

**Env names (no values in git):** `HEALTH_APP_API_BASE_URL`, `HEALTH_APP_OIDC_ISSUER`, `HEALTH_APP_OIDC_CLIENT_ID`, `HEALTH_APP_OIDC_REDIRECT_URI`

---

## Delivery stages vs stack

| Stage | Stack focus | State |
|---|---|---|
| Foundation | Monorepo, contracts, nav shells (iOS/Android/web), CI | **Scaffold** |
| Security / privacy baseline | Classification, logging, consent stubs | **Docs + stubs** |
| Identity | OIDC/PKCE on each client | Blocked — official docs + review |
| Wellness | HealthKit / Health Connect (mobile) | Spec + NotConfigured; web Unavailable |
| Clinical | Secure upload pipeline (all clients) | Spec + NotConfigured only |
| Expenses | Domain rules → client calculators | Python fixtures only |
| AI insights | Vertex AI grounded jobs | Spec only |
| Production hardening | GCP controls, monitoring, IR | Checklist only |

---

## CI & repo tooling

| Tool | Purpose |
|---|---|
| GitHub Actions `native-ci.yml` | contracts · forbidden-pattern scan · domain tests · Android · iOS · **web typecheck/test/build** |
| `scripts/check-forbidden-patterns.py` | Reject secrets / prohibited samples / kit constructors outside CompositionRoot (incl. `apps/web/src/core`) |
| `scripts/ios_simulator_destination.py` | Pick simulator destination for CI |
| Dependabot | Dependency PRs |
| Prettier / TypeScript / pnpm | Workspace formatting; web app scripts |

---

## Doc map (spec phase)

| Doc | What it covers |
|---|---|
| **[STACK.md](STACK.md)** (this file) | All technologies on one screen |
| [SPEC.md](SPEC.md) | Specification index & writing order |
| [CONTEXT.md](CONTEXT.md) | Living status / owners / blockers |
| [architecture.md](architecture.md) | Boundaries, layout, stages |
| [web-baseline.md](web-baseline.md) | Web capability matrix |
| [security.md](security.md) · [privacy-baseline.md](privacy-baseline.md) | Threat & privacy posture |
| [wellness-baseline.md](wellness-baseline.md) · [clinical-baseline.md](clinical-baseline.md) | Kit / upload intent |
| [release-checklist.md](release-checklist.md) | Store / signing prerequisites |

---

*Last updated: 2026-09-29 — web client added to stack. Update when any locked version or target choice changes.*
