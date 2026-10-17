# Product specification — My Number Health & Wellness Portal

**Status:** Draft for review (docs-first resume, 2026-09-29)  
**Repo:** https://github.com/sunveda/health-app  
**Owner:** Sarveshwar Singh (SunVeda)  
**Specialist bot:** Health App · **Standards:** Chief of Engineering · **Coordination:** Chief of Staff  

This document defines product intent for the native mobile clients. It does **not** invent My Number / OIDC endpoints, client IDs, scopes, or credentials. Any such values below are **UNCONFIRMED / TBD / blocked** until official provider documentation, relying-party registration, compliance approval, and threat-model review complete.

Cross-links (do not contradict): [`architecture.md`](architecture.md), [`security.md`](security.md), [`privacy-baseline.md`](privacy-baseline.md), [`wellness-baseline.md`](wellness-baseline.md), [`clinical-baseline.md`](clinical-baseline.md), [`release-checklist.md`](release-checklist.md), [`technical-spec.md`](technical-spec.md), [`mvp-screens.md`](mvp-screens.md), [`CONTEXT.md`](CONTEXT.md).

---

## 1. Vision

Japan residents should be able to authenticate with the official My Number identity path (**when approved**), bring together wellness measurements and clinical documents they choose to share, understand medical-expense eligibility with transparent fixtures and confirmation, and receive grounded, non-diagnostic AI insights — all inside native iOS and Android apps that fail closed on identity and health data until every integration is reviewed.

The product is a **portal**, not a clinical device or a tax-advice engine: it orchestrates consent, permissions, review, and export. Medical and tax decisions stay with qualified professionals and the user’s own filings.

## 2. Personas (MVP)

| Persona | Goal | Constraints |
|---|---|---|
| **Resident user** | Sign in via official My Number path (later), see home status, grant minimal wellness reads, upload/review reports (later), preview expense eligibility with synthetic or confirmed inputs, manage privacy settings | Must understand what is configured vs stubbed; no surprise permissions |
| **Caregiver / household helper** *(out of MVP)* | Manage another person’s data with explicit delegation | Deferred; no multi-user household model in MVP |
| **Product / compliance reviewer** | Verify consent purposes, redaction, and staging gates before each capability unlocks | Specs and baselines are the review surface |
| **Engineering agent / human** | Resume from living CONTEXT without chat history | CompositionRoot-only adapters; no invented IdP facts |

## 3. Platforms

| Platform | UI | Capability access |
|---|---|---|
| iOS | SwiftUI (`apps/ios`) | HealthKit, Keychain, LocalAuthentication, CoreNFC — **only** via Core adapters from CompositionRoot |
| Android | Kotlin + Jetpack Compose (`apps/android`) | Health Connect, Keystore, BiometricPrompt, NFC — **only** via platform adapters from CompositionRoot |

Shared: `packages/contracts` (JSON Schemas), `packages/domain` (synthetic medical-expense eligibility fixtures). **No shared UI.** Bundle / application ID: `com.sunveda.healthapp` (see release checklist).

## 4. In scope for MVP

MVP is a **shippable shell with honest stubs**, not a production My Number integration.

1. **Tab shells:** Home, Health, Expenses, Settings — aligned with existing feature modules.
2. **Consent / auth placeholders:** Settings (and Home status) surface “not configured” / blocked Identity and PrivacyCopyPlaceholder language; no real OIDC.
3. **CompositionRoot adapters:** NotConfigured (and Unavailable test) stubs for secure storage, biometrics, NFC, HealthDataSource, WellnessSyncClient, ConsentStore, CrashReporter, TelemetryPolicy, ClinicalDocumentPipeline, ReportUploadClient, FileValidation, QuarantineStore.
4. **Contracts + domain fixtures:** Schema-backed status mapping; synthetic expense eligibility fixtures in `packages/domain` (not tax-law guidance; native apps do not import domain at runtime today).
5. **Security / privacy baseline behavior:** Classification vocabulary, LogRedaction helpers, crash reporting disabled, telemetry policy deny-all.
6. **Documentation:** This product spec, technical spec, MVP screens, updated CONTEXT; existing architecture/security/baselines/release checklist remain authoritative for their stages.
7. **CI gates:** Builds/tests; CompositionRoot-only constructor greps; no secrets; synthetic fixtures only.

## 5. Out of scope for MVP

- Wired My Number / Digital Authentication App OIDC (issuer, client ID, scopes, redirect URIs) — **blocked / TBD**
- Real HealthKit or Health Connect permission prompts or sample reads
- Real clinical document picker, network upload, quarantine persistence, or malware-scanner vendor
- Production backend (Cloud Run, Firestore, Vertex AI, KMS, etc.) — target architecture only
- Crash / analytics SDKs with real DSNs
- Tax filing submission or official e-Tax integration
- Diagnosis, treatment recommendations, or claims of medical device status
- Household / multi-profile delegation
- Web or Expo clients
- Invented permission strings, upload URLs, or NFC reader commands

## 6. Authentication (high level)

**Current:** Identity is **not wired**. Screens must show blocked / not-configured states. Local biometric unlock is **not** available until after a reviewed initial identity verification.

**Target (UNCONFIRMED until official docs + registration):**

- Authorization-code flow with **PKCE** through the official Japanese My Number / Digital Authentication App identity provider.
- Unpredictable `state` and `nonce`; validate issuer, audience, redirect URI, authorization code, and token claims.
- Tokens only in platform-protected storage (Keychain / Keystore-backed).
- Application-facing identity key: **pairwise subject** where permitted — not the individual number as a general client id.
- Scopes for individual number and any picture/certificate capabilities: **separately governed; TBD; do not assume defaults.**

Until the above is confirmed, **do not** hardcode endpoints, client IDs, or scopes in code or docs as facts.

## 7. Data domains

Aligned with [`privacy-baseline.md`](privacy-baseline.md):

| Domain | MVP posture | Sensitivity |
|---|---|---|
| Identity pairwise | Not acquired | Extremely high |
| Individual number | Not acquired; never a log/analytics key | Extremely high |
| Health measurements | NotConfigured wellness stubs; intended later scopes `steps`, `heartRate` only after review | High |
| Clinical documents | NotConfigured pipeline stubs; no picker/upload | Very high |
| Expenses | Synthetic eligibility fixtures / local draft UX; export needs confirmation | High |
| Telemetry | Not configured; policy returns false for all categories | Low only if strictly non-PII |

Every acquisition path needs purpose id + consent decision before real kit or network use. Lawful basis and user-facing legal copy: **TODO for product/legal**.

## 8. Compliance notes

- Default posture: collect less, expose less, retain less, log less ([`security.md`](security.md)).
- Cloud certifications and target GCP controls support a program; they do **not** replace Japanese privacy / My Number product-specific legal review.
- No real PII, health payloads, individual numbers, tokens, or unredacted screenshots in Git, fixtures, or examples.
- Fixtures must be obviously synthetic.
- App Store / Play privacy questionnaires, Nutrition labels, and Data safety forms: draft with product/legal before TestFlight / Play internal ([`release-checklist.md`](release-checklist.md)).
- AI outputs (later stage) are advisory: show provenance, timestamps, uncertainty; instruct users to consult qualified professionals; never claim diagnosis.

## 9. MVP success criteria

1. Both native shells build and pass CI unit tests on `main`-equivalent docs+scaffold.
2. Four tabs render; Home / Health / Settings honestly show Identity, wellness, and clinical as **not configured** / blocked.
3. Expenses surface can demonstrate synthetic eligibility fixture behavior without implying tax advice.
4. No CompositionRoot-external construction of `NotConfigured*` / `Unavailable*` / `InMemory*` adapters outside unit tests (CI grep).
5. No secrets, guessed IdP endpoints, or real health/identity samples in the tree.
6. Product, technical, and MVP-screen specs reviewed by owner + CoE; CONTEXT updated for docs-first resume.
7. Feature coding remains gated until specs are reviewed; Identity remains blocked regardless of docs approval.

## 10. Open questions

| ID | Question | Owner | Status |
|---|---|---|---|
| Q1 | Official IdP registration path, issuer, client ID, redirect URIs, and allowed scopes | Owner + legal + IdP | **Blocked / TBD** |
| Q2 | Lawful basis and user-facing consent / retention copy (JA/EN) | Product / legal | TODO |
| Q3 | Whether individual number may ever persist on device | Legal + security | TBD; default no |
| Q4 | Exact clinical allowlist MIME types and max size | Product + security | TBD |
| Q5 | Crash / telemetry vendor after PII review | Engineering + privacy | Not configured |
| Q6 | Apple / Play account ownership and signing custody | Owner | TBD (not in repo) |
| Q7 | Expense export destination (local file vs future tax service) | Product | TBD |
| Q8 | AI model / Vertex grounding and safety review bar | Product + CoE | Post-Expenses stage |
| Q9 | When CoS fully resumes feature eng vs docs-only | CoS | Docs hold lifted 2026-09-29; features still gated on spec review |

## 11. Staging reminder

Hygiene → Security/privacy → **Identity (blocked)** → Wellness → Clinical → Expenses → AI → Release hardening.

Docs/spec work may proceed under the 2026-09-29 docs-first resume. **No feature coding** until these specs are reviewed. Identity stays blocked even after review.

---

*Last updated: 2026-09-29 (Asia/Tokyo).*
