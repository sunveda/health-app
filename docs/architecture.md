# Architecture

## Purpose

This document turns the initial product specification into a staged architecture for a mobile client serving iOS and Android. It separates the device experience from the cloud trust boundary so that native permissions, identity verification, health-data ingestion, AI processing, and tax workflows can be reviewed independently.

**One-screen tech map:** [STACK.md](STACK.md). **Specification index (docs-first):** [SPEC.md](SPEC.md).
## System boundaries

| Boundary | Initial responsibility | Data sensitivity |
|---|---|---|
| Mobile client | Consent, authentication handoff, local session unlock, health-data permission flows, document capture, user-facing review | High; no raw sensitive data in logs |
| API gateway and service layer | Session validation, authorization, consent records, data transformation, orchestration | Very high |
| Identity provider | Official My Number Card / Digital Authentication App authentication | Extremely high |
| Health-data adapters | HealthKit and Health Connect acquisition with user-granted scopes | High |
| Clinical document pipeline | Secure upload, malware/file validation, extraction, summarization, user review | Very high |
| Tax service | Expense aggregation, eligibility calculation, export preparation, audit trail | High |
| AI service | Grounded summarization and longitudinal trend analysis with safety disclaimers | High |

## Mobile application structure

The clients live in a single monorepo: SwiftUI on iOS, Kotlin/Jetpack Compose on Android, and React/Vite on web. There is no Expo or Expo Router app. Platforms share **contracts**, not UI. Shared product contracts live in `packages/contracts`. `packages/domain` holds synthetic medical-expense eligibility fixtures and a platform-neutral placeholder engine (not tax-law guidance; clients do not import it at runtime). `packages/api` remains reserved (README-only) until transport clients are designed.

```text
apps/ios/
  HealthApp/
    App/                # SwiftUI entry; constructs the composition root once
    Core/               # Platform protocols, NotConfigured/Unavailable stubs, composition root, privacy, wellness, and clinical helpers
    Features/           # Tab shells (home, health, expenses, settings)
  HealthAppTests/       # Unit tests of mapping helpers and adapter stubs
apps/android/
  app/src/main/java/com/sunveda/healthapp/
    MainActivity.kt     # Compose entry; constructs the composition root once
    core/               # UI theme only
    platform/           # Platform interfaces, NotConfigured/Unavailable stubs, composition root, privacy, wellness, and clinical helpers
    features/           # Tab shells (home, health, expenses, settings)
  app/src/test/         # JVM unit tests of mapping helpers and adapter stubs
apps/web/
  src/
    main.tsx / App.tsx  # React entry; constructs the composition root once
    core/               # Adapters, NotConfigured/Unavailable stubs, composition root
    features/           # Tab shells (home, health, expenses, settings)
packages/contracts/     # Live JSON Schemas shared by all clients
packages/domain/        # Synthetic expense-eligibility fixtures + pure tests
packages/api/           # Reserved — no networking clients yet
```

A platform adapter must expose capability availability, permission state, read scope, and failure state. Screens and feature modules must never import or call HealthKit, Health Connect, NFC, biometrics, secure-storage, WebAuthn, or browser credential APIs directly. Capability access is only through Core/platform adapters constructed by a single composition root. Web treats device health kits and NFC as **Unavailable** ([web-baseline.md](web-baseline.md)). This keeps platform-specific behavior testable and prevents accidental permission escalation.
Today those adapters are NotConfigured (and Unavailable) stubs: they return explicit unavailable states and do not request permissions or touch device kits. Stage 1.5 adds the same pattern for `ConsentStore`, `CrashReporter`, and `TelemetryPolicy` (no persistence of sensitive consent payloads, no crash SDK, no telemetry send). Wellness deepens `HealthDataSource` (availability, permission state, read scope, failure) and adds `WellnessSyncClient` behind a disabled feature flag — still NotConfigured, still no HealthKit or Health Connect calls. Clinical adds `ClinicalDocumentPipeline`, `ReportUploadClient`, `FileValidation`, and `QuarantineStore` — still NotConfigured, still no document picker, network upload, or file pipeline. Real HealthKit, Health Connect, Keychain, Keystore, LocalAuthentication, BiometricPrompt, CoreNFC, document-picker, and upload implementations may be added later only inside the Core/platform adapter paths, and only the composition root may construct them. CI greps for concrete `NotConfigured*` / `Unavailable*` / `InMemory*` constructors outside `CompositionRoot` and unit tests.

## Signing & release placeholders

Both clients use the placeholder bundle / application ID `com.sunveda.healthapp`. Production signing certificates, provisioning profiles, upload keystores, and API credentials are not stored in this repository. Native CI builds iOS with `CODE_SIGNING_ALLOWED=NO`.

Apple Developer Program membership, App Store Connect / TestFlight, Google Play Console ownership, Play App Signing, and a Play internal testing track are required before the Clinical stage. Account ownership and team emails are TBD with the product owner and are not recorded here. The operational checklist is [`release-checklist.md`](release-checklist.md).

## Backend target

The supplied specification proposes Cloud Run for containerized services, Firestore for user health metadata and longitudinal profiles, Vertex AI for AI-assisted processing, Cloud KMS with CMEK for key management, and VPC Service Controls plus Cloud Armor for perimeter and ingress protection. These are target decisions, not yet deployed infrastructure. The backend should be split into independently authorized services for identity, health data, clinical documents, expenses, AI jobs, and exports.

Sensitive values should be partitioned by purpose. The pairwise subject identifier is the application-facing identity key. Direct identifiers and the individual number should be isolated from general health records, encrypted with managed keys, and accessed only by narrowly authorized services. The mobile app should receive the minimum data required for each screen and should not persist the individual number unless a reviewed product requirement makes this unavoidable.

## Identity flow

The mobile client should use the authorization-code flow with PKCE through the official identity-provider integration. The implementation must generate unpredictable `state` and `nonce` values, validate issuer, audience, redirect URI, authorization code, and token claims, and keep tokens in platform-protected storage. Local re-entry should use passkeys and native biometrics only after the initial identity verification has completed.

The scopes listed in the product specification require confirmation against the official provider documentation, legal basis, and app registration. In particular, access to the individual number and future picture scope must be treated as separately governed capabilities rather than assumed defaults.

## Data lifecycle

Data acquisition, normalization, storage, AI processing, export, and deletion must each have an explicit consent and audit event. AI-generated explanations are advisory and must show source measurements, timestamps, uncertainty, and a clear instruction to consult a qualified professional for medical decisions. A report upload should be resumable, encrypted in transit, validated before processing, and deletable by the user according to the retention policy.

## Delivery stages

| Stage | Deliverable | Exit criteria |
|---|---|---|
| Foundation | Monorepo, typed contracts, navigation shell, security policy | CI passes; no secrets committed |
| Security/privacy baseline | Data classification, device-boundary note, PII-safe logging, consent/purpose scaffolding, crash-reporting decision | Data-classification and device-boundary note published; PII-safe logging policy; consent/purpose scaffolding interfaces present; crash-reporting decision recorded |
| Identity | OIDC/PKCE handoff and local biometric unlock | Threat model and provider integration review complete |
| Wellness | Minimized HealthKit / Health Connect read adapters behind CompositionRoot and a feature flag | Smallest necessary read scopes with purpose-tied permission UX; adapters constructed only by CompositionRoot; feature flag off until kit wiring is reviewed; platform tests cover not-configured, unavailable, denied, and ready paths |
| Clinical | Secure report upload and review surface behind CompositionRoot | Secure upload, file validation, quarantine, user review, and deletion tested; adapters constructed only by CompositionRoot; no invented upload URLs or scanner vendors |
| Expenses | Expense import, calculation, and export preview | Calculation fixtures and user confirmation flow pass |
| AI insights | Grounded summaries and trend views | Safety review, provenance display, and fallback states pass |
| Production hardening | Cloud controls, monitoring, incident response | Independent security and privacy review complete |

Classification, device-boundary principles, logging redaction, consent scaffolding, and the crash-reporting decision are in [`privacy-baseline.md`](privacy-baseline.md). That stage does not add backend URLs or unblock Identity.

Wellness intended read scopes, deferred kit wiring, and the feature-flag approach are in [`wellness-baseline.md`](wellness-baseline.md). Current clients ship NotConfigured wellness stubs only; that does not complete Wellness, Identity, or Clinical.

Clinical intended pipeline stages (selection, validation, quarantine, upload, review, deletion) and the NotConfigured upload/review posture are in [`clinical-baseline.md`](clinical-baseline.md). Current clients ship NotConfigured clinical stubs only; that does not complete Clinical, Identity, or Wellness.
