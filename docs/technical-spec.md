# Technical specification — My Number Health & Wellness Portal

**Status:** Draft for review (docs-first resume, 2026-09-29)  
**Repo:** https://github.com/sunveda/health-app  
**Current `main` tip (reference):** `428abe1` (was 1c0d819 when drafted) — Foundation hygiene, security/privacy baseline, wellness & clinical NotConfigured stubs, `packages/domain` synthetic expense fixtures, CONTEXT+AGENTS via #20.  

Identity is **not wired**. Feature hold is lifted **only for docs/spec**; no feature coding until specs are reviewed.

Cross-links: [`architecture.md`](architecture.md), [`security.md`](security.md), [`privacy-baseline.md`](privacy-baseline.md), [`wellness-baseline.md`](wellness-baseline.md), [`clinical-baseline.md`](clinical-baseline.md), [`release-checklist.md`](release-checklist.md), [`product-spec.md`](product-spec.md), [`mvp-screens.md`](mvp-screens.md), [`CONTEXT.md`](CONTEXT.md).

**Hard rule:** Never invent My Number/OIDC endpoints, client IDs, or scopes as confirmed facts — mark **UNCONFIRMED / TBD / blocked**. No secrets or real PII/health data in examples.

---

## 1. Monorepo layout

```text
apps/ios/
  HealthApp/
    App/                # SwiftUI entry; constructs CompositionRoot once
    Core/               # Protocols, NotConfigured/Unavailable stubs, CompositionRoot,
                        # privacy / wellness / clinical helpers, LogRedaction
    Features/           # Tab shells: home, health, expenses, settings
  HealthAppTests/
apps/android/
  app/src/main/java/com/sunveda/healthapp/
    MainActivity.kt     # Compose entry; constructs CompositionRoot once
    core/               # UI theme only
    platform/           # Interfaces, NotConfigured/Unavailable stubs, CompositionRoot,
                        # privacy / wellness / clinical helpers
    features/           # Tab shells: home, health, expenses, settings
  app/src/test/
packages/contracts/     # Live JSON Schemas shared by both clients
packages/domain/        # Synthetic medical-expense eligibility fixtures + pure tests
packages/api/           # Reserved — README only; no networking clients yet
docs/                   # Architecture, security, baselines, specs, CONTEXT, release checklist
```

Bundle / application ID: `com.sunveda.healthapp` on both platforms (see [`release-checklist.md`](release-checklist.md)). Native CI builds iOS with `CODE_SIGNING_ALLOWED=NO`.

## 2. Adapter pattern (CompositionRoot-only)

### Rule

Screens and feature modules **must not** import or call HealthKit, Health Connect, NFC, biometrics, Keychain/Keystore, document pickers, or upload SDKs directly. Capability access is **only** through Core / platform adapters constructed by a **single CompositionRoot**.

CI greps for concrete `NotConfigured*` / `Unavailable*` / `InMemory*` constructors outside `CompositionRoot` and unit tests.

### Responsibilities of every adapter

Expose at minimum:

- Capability **availability**
- **Permission** / consent-related state (where applicable)
- **Read scope** or active pipeline stage (where applicable)
- Last **failure** state (not-configured, unavailable, denied, etc.)

### Construction

| Who | May construct real or stub adapters |
|---|---|
| CompositionRoot | Yes — sole production wiring site |
| Unit tests | Yes — `Unavailable*` / `InMemory*` for isolation |
| Feature screens / ViewModels | **No** — receive injected surfaces only |

When real kit wiring is approved later, only CompositionRoot may flip feature flags and construct live adapters.

## 3. Built-vs-stub matrix

| Capability | iOS surface | Android surface | Status on `main` |
|---|---|---|---|
| Tab navigation shell | SwiftUI Features | Compose features | **Built** |
| CompositionRoot | Core | platform | **Built** |
| Secure storage (Keychain / Keystore) | Adapter protocol | Adapter protocol | **NotConfigured stub** |
| Biometrics / LocalAuthentication / BiometricPrompt | Adapter protocol | Adapter protocol | **NotConfigured stub** |
| NFC / CoreNFC | Adapter protocol | Adapter protocol | **NotConfigured stub** |
| ConsentStore | Core | platform | **NotConfigured** (InMemory for tests only) |
| CrashReporter | Core | platform | **NotConfigured** — no SDK/DSN |
| TelemetryPolicy | Core | platform | **NotConfigured** — deny all categories |
| LogRedaction | Helpers | Helpers | **Built** (policy helpers) |
| HealthDataSource | Core | platform | **NotConfigured stub** |
| WellnessSyncClient + feature flag | Core | platform | **NotConfigured**; flag **disabled** |
| ClinicalDocumentPipeline | Core | platform | **NotConfigured stub** |
| ReportUploadClient | Core | platform | **NotConfigured stub** |
| FileValidation | Core | platform | **NotConfigured stub** |
| QuarantineStore | Core | platform | **NotConfigured stub** |
| OIDC / My Number IdP client | — | — | **Blocked / not present** |
| Backend HTTP clients (`packages/api`) | — | — | **Reserved empty** |
| Domain expense fixtures | — | — | **Built** in `packages/domain` (synthetic) |
| AI / Vertex clients | — | — | **Not started** |

## 4. Auth design (blocked)

### Current

No IdP client, no redirect handler, no token storage wiring, no biometric gate after identity. UI must present Identity as blocked / not configured ([`mvp-screens.md`](mvp-screens.md)).

### Target shape (UNCONFIRMED)

Align with [`architecture.md`](architecture.md) Identity flow:

1. Authorization-code + **PKCE** via official provider (**issuer, client ID, redirect URI, scopes — TBD / UNCONFIRMED**).
2. Generate and validate `state` and `nonce`; validate issuer, audience, redirect, code, claims, expiry.
3. Store tokens only in platform-protected storage via CompositionRoot-wired adapters.
4. Prefer **pairwise subject** as application identity key; isolate individual number from general health records.
5. Local re-entry with passkeys / biometrics **only after** initial verified identity session.

### Explicit non-facts

The following must **not** appear in code or docs as confirmed:

- Specific OIDC discovery / token / authorize URLs
- Client IDs or secrets
- Scope strings claimed as approved (including individual-number or picture scopes)
- Certificate PIN handling or NFC APDU sequences

Mark any draft examples as `<!-- UNCONFIRMED / TBD -->` and keep them out of runnable config.

## 5. Contracts

### Live

- `packages/contracts` JSON Schemas, including wellness sync status values: `not_connected`, `connected`, `permission_denied`, `error` (see [`wellness-baseline.md`](wellness-baseline.md)).
- Pure mappers translate adapter snapshots → contract / local surface status for shells.

### Not yet

- No `clinicalDocument` wire contract object yet ([`clinical-baseline.md`](clinical-baseline.md)).
- No Identity token / session schema for production IdP claims.
- `packages/api` has no transport clients.

### Domain

- `packages/domain` holds **synthetic** medical-expense eligibility fixtures and pure tests.
- Not tax-law guidance; native apps do not import it at runtime today.
- Fixtures must remain obviously fake (no real names, My Numbers, or claim IDs).

## 6. Staging plan

| Order | Stage | Exit criteria (summary) | Docs |
|---|---|---|---|
| 1 | **Hygiene** | Monorepo, typed contracts, navigation shell, CI green, no secrets | architecture |
| 2 | **Security / privacy** | Classification, device boundary, logging redaction, consent scaffolding, crash decision | privacy-baseline, security |
| 3 | **Identity** | OIDC/PKCE + local unlock after threat model + provider registration | **BLOCKED** |
| 4 | **Wellness** | Minimized HealthKit / Health Connect reads behind CompositionRoot + flag | wellness-baseline |
| 5 | **Clinical** | Secure upload, validation, quarantine, review, deletion behind CompositionRoot | clinical-baseline |
| 6 | **Expenses** | Import / calculation / export preview with confirmation | domain fixtures → deepen |
| 7 | **AI** | Grounded summaries, provenance, safety review | architecture AI boundary |
| 8 | **Release hardening** | Cloud controls, monitoring, IR, independent security/privacy review | release-checklist |

**2026-09-29 policy:** Docs/spec may advance. Feature coding waits for owner + CoE review of product/technical/MVP-screen specs. Identity remains blocked irrespective of docs approval.

## 7. CI gates

Expected gates (keep / harden; do not weaken):

1. **iOS unit tests** on resolved simulator via `scripts/ios_simulator_destination.py` (concrete UDID; fail if unresolved — no `generic/platform=iOS Simulator` escape hatch).
2. **Android** unit tests / `assembleDebug` as configured in Native CI.
3. **CompositionRoot constructor grep** — forbid `NotConfigured*` / `Unavailable*` / `InMemory*` construction outside CompositionRoot and tests.
4. **Secret / prohibited sample scanning** — no tokens, individual numbers, or real clinical content.
5. **Schema / domain tests** for contracts and synthetic fixtures.
6. **CODE_SIGNING_ALLOWED=NO** for iOS CI builds.

Pull requests should include: data-classification note for new fields; statement if a new native permission is introduced; redaction/auth tests where relevant ([`security.md`](security.md)).

## 8. Non-goals

- Shared UI toolkit or Expo / React Native clients
- Guessed production endpoints, buckets, DSNs, or permission scopes
- Wiring Identity, kits, or upload SDKs “to unblock demos”
- Persisting individual number without explicit data-flow + legal approval
- Logging tokens, pairwise subjects, diagnoses, report bytes, or expense narratives
- Treating Cloud Run / Firestore / Vertex target decisions as deployed infrastructure
- Expanding this repo into other SunVeda products

## 9. Backend target (not deployed)

Per architecture: Cloud Run services, Firestore for health metadata, Vertex AI for grounded jobs, Cloud KMS / CMEK, VPC Service Controls + Cloud Armor. Split independently authorized services for identity, health data, clinical documents, expenses, AI jobs, and exports. Mobile receives minimum data per screen.

These are **targets**, not current implementations. Clients must not hardcode service URLs.

## 9b. Web client (PLANNED)

Owner scope (2026-10-17): ship as web + iOS + Android. `apps/web` is not in the repo. Proposed design: [ADR-0008](adr/0008-web-client-stack.md), [ADR-0009](adr/0009-web-session-and-token-handling.md), [system-architecture §5.4](architecture/system-architecture.md). Same CompositionRoot + NotConfigured pattern; web CI gates must be added first.

## 10. Related links

- Architecture set: [system-architecture](architecture/system-architecture.md), [data-model](architecture/data-model.md), [api-contracts](architecture/api-contracts.md), [threat-model](security/threat-model.md), [ADRs](adr/README.md), [development](development.md)

| Doc | Role |
|---|---|
| [`product-spec.md`](product-spec.md) | Vision, scope, personas, success criteria |
| [`mvp-screens.md`](mvp-screens.md) | Per-screen purpose, actions, gates |
| [`CONTEXT.md`](CONTEXT.md) | Living handoff |
| [`architecture.md`](architecture.md) | Boundaries, stages, layout |
| [`security.md`](security.md) | Non-negotiable controls |
| [`privacy-baseline.md`](privacy-baseline.md) | Stage 1.5 classification & consent |
| [`wellness-baseline.md`](wellness-baseline.md) | Device-health stub record |
| [`clinical-baseline.md`](clinical-baseline.md) | Upload/review stub record |
| [`release-checklist.md`](release-checklist.md) | TestFlight / Play prerequisites |
| Root `AGENTS.md` | Agent operating rules |

---

*Last updated: 2026-09-29 (Asia/Tokyo).*
