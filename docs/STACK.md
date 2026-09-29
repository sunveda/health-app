# Stack at a glance

**One screen for the whole product.** Read this first to see every technology in play — shipped today vs target later. No secrets. Detail lives in linked docs.

**Product:** My Number Health & Wellness Portal — native iOS + Android; share **contracts**, not UI.  
**Repo:** https://github.com/sunveda/health-app · **Bundle / app ID:** `com.sunveda.healthapp` · **Version:** `0.1.0`  
**Phase (2026-09-29):** Docs & specification first — no feature implementation until specs are owner-approved.

---

## System map

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│  MOBILE CLIENTS (in repo today)                                             │
│  ┌──────────────────────────┐  ┌──────────────────────────┐                 │
│  │ iOS · SwiftUI            │  │ Android · Compose        │                 │
│  │ Swift 5.9 · iOS 17+      │  │ Kotlin 2.0 · min 26      │                 │
│  │ XcodeGen → Xcode         │  │ AGP 8.7 · compile 35     │                 │
│  │ CompositionRoot → stubs  │  │ CompositionRoot → stubs  │                 │
│  └────────────┬─────────────┘  └────────────┬─────────────┘                 │
│               └──────────┬──────────────────┘                               │
│                          ▼                                                  │
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
│  My Number IdP (OIDC + PKCE) · Digital Authentication App / NFC             │
│  Apple HealthKit · Google Health Connect                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Client stacks (versions from repo)

| Layer | iOS (`apps/ios`) | Android (`apps/android`) |
|---|---|---|
| UI | SwiftUI | Jetpack Compose + Material 3 |
| Language | Swift **5.9** | Kotlin **2.0.21** |
| OS floor | iOS **17.0** | minSdk **26**, target/compile **35** |
| Tooling | XcodeGen (`project.yml`), Xcode **15+** | Gradle **8.10**, AGP **8.7.3**, JDK **17** |
| Compose BOM | — | `2025.02.00` (+ Activity Compose 1.10.1, Lifecycle 2.8.7) |
| Entry | `HealthAppApp` → `CompositionRoot` | `MainActivity` → `CompositionRoot` |
| Features | Tab shells: home, health, expenses, settings | Same tab shells |
| Tests | XCTest (`HealthAppTests`) | JUnit 4 |
| Signing | Placeholder; CI `CODE_SIGNING_ALLOWED=NO` | Debug assemble; Play signing TBD |

**Not used:** Expo, React Native, Flutter, shared UI kits, guessed IdP/API SDKs.

---

## Shared monorepo packages

| Path | Tech | Role today |
|---|---|---|
| `packages/contracts` | JSON Schema (draft 2020-12) | Live portal contract: identity, wellnessSync, medicalExpenses |
| `packages/domain` | Python 3 + unittest | Synthetic medical-expense eligibility fixtures (**not** tax-law advice; apps do **not** import at runtime) |
| `packages/api` | README only | Reserved for versioned transport clients |
| Root workspace | pnpm **10.12.1**, Prettier, TypeScript (dev) | Lint/format hooks; native apps are not Node runtimes |

---

## Platform capabilities (fail closed)

All device kits go through **Core / platform adapters** built only by `CompositionRoot`. Feature screens must not import kit APIs.

| Capability | Intended tech | Status |
|---|---|---|
| Health data | HealthKit (iOS) / Health Connect (Android) | **NotConfigured** stubs |
| Secure storage | Keychain / Keystore | **NotConfigured** |
| Local unlock | LocalAuthentication / BiometricPrompt (+ passkeys later) | **NotConfigured** |
| Card / NFC | CoreNFC + official Digital Auth App path | **NotConfigured** — no invented readers |
| Consent / crash / telemetry | App policy stubs | **NotConfigured** (no SDK, no PII send) |
| Wellness sync | `WellnessSyncClient` + feature flag | Stub; flag off |
| Clinical upload | picker → validate → quarantine → upload → review | Stub; no network URLs |
| Identity session | OIDC auth-code + PKCE | **Not wired** |

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
| Identity | Official My Number IdP · OIDC · PKCE · `state`/`nonce` | Scopes/endpoints **not invented** |
| App identity key | Pairwise subject (`pairwiseSub`) | Individual number isolated; never a general client key |

**Env names (no values in git):** `HEALTH_APP_API_BASE_URL`, `HEALTH_APP_OIDC_ISSUER`, `HEALTH_APP_OIDC_CLIENT_ID`, `HEALTH_APP_OIDC_REDIRECT_URI`

---

## Delivery stages vs stack

| Stage | Stack focus | State |
|---|---|---|
| Foundation | Monorepo, contracts, nav shells, CI | **Done** (scaffold) |
| Security / privacy baseline | Classification, logging, consent stubs | **Docs + stubs** |
| Identity | OIDC/PKCE, Keychain/Keystore, biometrics | Blocked — needs official docs + review |
| Wellness | HealthKit / Health Connect adapters | Spec + NotConfigured only |
| Clinical | Secure upload pipeline | Spec + NotConfigured only |
| Expenses | Domain rules → native calculators | Python fixtures only |
| AI insights | Vertex AI grounded jobs | Spec only |
| Production hardening | GCP controls, monitoring, IR | Checklist only |

---

## CI & repo tooling

| Tool | Purpose |
|---|---|
| GitHub Actions `native-ci.yml` | contracts JSON validate · forbidden-pattern scan · domain tests · Android `test assembleDebug` · iOS XcodeGen + `xcodebuild test` |
| `scripts/check-forbidden-patterns.py` | Reject secrets / prohibited samples / kit constructors outside CompositionRoot |
| `scripts/ios_simulator_destination.py` | Pick simulator destination for CI |
| Dependabot | Dependency PRs |
| Prettier / TypeScript | Workspace formatting / types (packages side) |

---

## Doc map (spec phase)

| Doc | What it covers |
|---|---|
| **[STACK.md](STACK.md)** (this file) | All technologies on one screen |
| [SPEC.md](SPEC.md) | Product specification index & writing order |
| [CONTEXT.md](CONTEXT.md) | Living status / owners / blockers |
| [architecture.md](architecture.md) | Boundaries, layout, stages |
| [security.md](security.md) · [privacy-baseline.md](privacy-baseline.md) | Threat & privacy posture |
| [wellness-baseline.md](wellness-baseline.md) · [clinical-baseline.md](clinical-baseline.md) | Kit / upload intent |
| [release-checklist.md](release-checklist.md) | Store / signing prerequisites |

---

*Last updated: 2026-09-29 — docs-first restart. Update this file when any locked version, package, or target cloud choice changes.*
