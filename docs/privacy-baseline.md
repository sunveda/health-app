# Security / privacy baseline (stage 1.5)

This note is the Security/privacy baseline for the native clients. It records classification, device-boundary principles, logging rules, a crash-reporting decision, and consent/purpose scaffolding. It does **not** complete Identity, Wellness, Clinical, or legal review.

Lawful basis, user-facing notices, retention periods, and the exact requirements of applicable Japanese privacy and My Number regulations are **TODO for product/legal**. This document does not invent statute citations, IdP endpoints, or backend URLs.

Related: [`architecture.md`](architecture.md), [`security.md`](security.md).

## Data classification

Categories below are the product vocabulary used by the consent/purpose stubs. Sensitivity is a working engineering label, not a legal determination.

| Category | What it includes (engineering) | Sensitivity | Notes |
|---|---|---|---|
| Identity pairwise | Application-facing subject from the official IdP, once Identity is approved | Extremely high | Preferred application identity key. Not a substitute for the individual number. |
| Individual number | My Number / individual number and equivalent direct national identifiers | Extremely high | Must not be a general-purpose client key, analytics id, or log field. Any persist/display/export path needs data-flow and legal/compliance review. |
| Health measurements | Wellness samples (steps, heart rate, and similar platform reads) | High | Acquired only after Wellness adapters and minimized platform scopes. |
| Clinical documents | Uploaded reports, extracted text, diagnoses, prescriptions, clinician notes | Very high | Treat contents as unloggable. Upload/extraction is a later Clinical stage. |
| Expenses | Medical-expense amounts, eligibility inputs, export payloads | High | Export requires an explicit review step (see security policy). |
| Telemetry | Non-PII diagnostics, coarse feature flags, crash breadcrumbs | Low only if strictly non-PII | Default is **do not send**. PII, health, or identity payload in telemetry is forbidden. |

New fields in pull requests should map to one of these categories (or add a category here with a classification note).

## Device boundary

These are **state principles**, not a network map. No service URLs, bucket names, or IdP issuers are defined here.

| Category | On-device only (current baseline) | May sync later | Must not |
|---|---|---|---|
| Identity pairwise | Hold only in platform-protected storage after Identity is reviewed | Session/authorization with the official IdP and authorized APIs | Put in ordinary preferences, logs, or crash reports |
| Individual number | Do not persist unless a reviewed product requirement makes it unavoidable | Only through a narrowly authorized, reviewed service path | Use as a primary key, analytics id, or log/crash field |
| Health measurements | Local adapter cache after Wellness, scoped to granted purpose | Backend health metadata only after consent + API design | Upload the full platform dump “just in case” |
| Clinical documents | Capture/review locally until the Clinical pipeline exists | Encrypted upload to the clinical document service after that stage | Log, screenshot, or attach file contents to telemetry |
| Expenses | Local draft lists until the Expenses stage | Aggregated export after user confirmation | Include account numbers or unreviewed exports in logs |
| Telemetry | Client-side policy decision only (currently not configured) | Non-PII events only, after vendor + PII review | Include tokens, identifiers, diagnoses, or report text |

Sync is not enabled in this stage. Adapters remain NotConfigured and do not open network or device-kit sessions.

## Logging redaction

Default posture: **collect less, expose less, retain less, and log less**. Clients expose `LogRedaction` helpers so event names can be recorded without interpolating sensitive values.

**Never log, crash-report, or attach:**

- Access, refresh, or identity tokens; authorization codes; PKCE verifiers
- Individual number or other direct national identifiers
- Pairwise subject (treat as an identity key, not a log field)
- Diagnoses, prescriptions, report contents, clinical document text or filenames that encode those
- Postal addresses, financial account details, expense line-item narratives
- Biometric templates or secure-storage payloads
- Raw HealthKit / Health Connect samples

Allowed in logs: coarse, non-identifying event names (for example `consent.record.failed`) and redaction placeholders such as `[REDACTED]:individualNumber`. Fixtures must stay synthetic.

## Crash reporting decision

**Decision (placeholder until vendor + PII review):** crash reporting is **disabled / not configured**. No crash SDK, DSN, API key, or upload endpoint is included in this repository.

| Option | Outcome |
|---|---|
| Chosen | `CrashReporter` and `TelemetryPolicy` interfaces exist; CompositionRoot wires `NotConfigured` implementations that never send events and never attach user data |
| Rejected for this stage | Wiring Sentry, Firebase Crashlytics, Bugsnag, or any vendor SDK with a real DSN/key |
| Revisit when | A vendor is selected, a PII review of breadcrumbs/minidumps is complete, and Identity/legal have not flagged additional blockers |

`TelemetryPolicy` returns `false` for every data category while not configured. There is no analytics SDK.

## Consent / purpose scaffolding

Each data category needs:

1. A **purpose** identifier (engineering id in `PurposeRegistry`)
2. A **lawful basis** — TODO for product/legal; not claimed here
3. An in-app **consent record** (granted / denied / not recorded) before Clinical and before any real kit or network use

`ConsentStore` records only category + decision + purpose id. It must not persist report contents, individual numbers, or other sensitive payloads. CompositionRoot currently wires `NotConfiguredConsentStore` (operations fail; status is not configured). An in-memory store exists for unit tests and is not a production consent ledger (no disk, no legal copy).

User-facing consent language, withdrawal copy, and retention notices are **TODO for product/legal**. Do not treat engineering purpose summaries as official notices. Settings screens show `PrivacyCopyPlaceholder` strings that repeat that TODO; they are not statute citations.

Identity remains blocked on IdP registration, official scopes, threat-model, and legal review. This baseline does not unblock OIDC, biometrics, HealthKit, Health Connect, NFC, or backend clients.
