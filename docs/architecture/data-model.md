# Data model

**Status:** Draft. **Last updated:** 2026-10-17.
Derived from `packages/contracts/schemas/portal.json` (live), `packages/domain/eligibility.py`, the privacy baseline categories, and the clinical/wellness baselines. Anything not in those sources is labeled **PLANNED** and is a proposal for review, not a decision. No real data; fixtures are synthetic.

Related: [system-architecture.md](system-architecture.md) · [api-contracts.md](api-contracts.md) · [privacy-baseline.md](../privacy-baseline.md) · [threat-model](../security/threat-model.md)

## 1. Live contract (`portal.json`, JSON Schema 2020-12)

Top-level object, `additionalProperties: false`, all three properties required.

| Property | Field | Type / constraint | Required |
|---|---|---|---|
| `identity` | `pairwiseSub` | string, minLength 1 | yes |
| | `displayName` | string | no |
| | `verifiedAt` | string, date-time | yes |
| `wellnessSync` | `platform` | enum `ios_healthkit`, `android_health_connect` | yes |
| | `status` | enum `not_connected`, `connected`, `permission_denied`, `error` | yes |
| | `lastSyncedAt` | string date-time or null | yes |
| `medicalExpenses` | `taxYear` | integer ≥ 2000 | yes |
| | `eligibleAmountJpy` | integer ≥ 0 | yes |
| | `calculationStatus` | enum `not_available`, `review_required`, `ready_for_export` | yes |

**Gap — web:** `wellnessSync.platform` has no web value. Adding one (for example a manual/import source) is a contract change that needs a data-classification review and compatibility note per `packages/contracts/README.md`. It is **PLANNED**, not decided; do not add a value without that review.

Clients must handle unknown enum values safely (contracts README).

## 2. Entity overview

```mermaid
erDiagram
  USER ||--o| IDENTITY : has
  USER ||--o{ CONSENT_RECORD : records
  USER ||--o{ WELLNESS_SYNC_STATE : has
  USER ||--o{ HEALTH_MEASUREMENT : owns
  USER ||--o{ CLINICAL_DOCUMENT : uploads
  USER ||--o{ EXPENSE_CASE : files
  EXPENSE_CASE ||--|{ EXPENSE_LINE : contains
  CLINICAL_DOCUMENT ||--o{ EXTRACTION : yields
  USER ||--o{ AI_INSIGHT : receives
  USER ||--o{ AUDIT_EVENT : generates
  IDENTITY {
    string pairwiseSub
    string displayName
    datetime verifiedAt
  }
  CONSENT_RECORD {
    string category
    string decision
    string purposeId
  }
  WELLNESS_SYNC_STATE {
    string platform
    string status
    datetime lastSyncedAt
  }
  EXPENSE_CASE {
    int taxYear
    int eligibleAmountJpy
    string calculationStatus
  }
  EXPENSE_LINE {
    string id
    string category
    int amountJpy
    int reimbursedJpy
    bool documented
    string payeeLabel
  }
  CLINICAL_DOCUMENT {
    string documentId
    string declaredType
    int byteSize
  }
```

## 3. Entities

Status column: **CONTRACT** = in `portal.json`; **CODE** = defined in client/domain code; **PLANNED** = proposal only.

| Entity | Fields | Class (privacy-baseline) | Status |
|---|---|---|---|
| Identity | `pairwiseSub`, `displayName?`, `verifiedAt` | identityPairwise — extremely high | CONTRACT |
| Individual number | none modeled; **never** a client field, key, log or analytics value | individualNumber — extremely high | Deliberately absent; any path needs legal review |
| ConsentRecord | category, decision (`granted`/`denied`/`notRecorded`), purposeId; no payloads | per category | CODE (`ConsentStore`; stub) |
| WellnessSyncState | `platform`, `status`, `lastSyncedAt` | healthMeasurements — high | CONTRACT |
| HealthMeasurement | scope (`steps`, `heartRate` first), value, unit, interval, source | healthMeasurements — high | PLANNED; scopes in code, schema not yet |
| ClinicalDocument | `documentId`, `declaredType` (`pdf`, `image`), `byteSize`, stage, status | clinicalDocuments — very high | CODE (descriptor, metadata-only); no contract object yet |
| Extraction | text/fields derived from a document, provenance to document | clinicalDocuments — very high | PLANNED |
| ExpenseCase | `taxYear`, `eligibleAmountJpy`, `calculationStatus`, `reviewReasons`, `contributingLineIds` | expenses — high | CONTRACT (3 fields) + CODE (`EligibilityResult`) |
| ExpenseLine | `id`, `category`, `amountJpy`, `reimbursedJpy`, `documented`, `payeeLabel` | expenses — high | CODE (domain, synthetic) |
| AIInsight | text, source refs, timestamps, uncertainty, disclaimer flag | per source data | PLANNED |
| AuditEvent | actor, action, resource type, time; **no payloads** | telemetry-grade, non-PII | PLANNED |

Enumerations (CODE): `ReviewReason` = `undocumented`, `unknown_category`, `negative_amount`, `over_reimbursed`. Expense categories in the synthetic engine: eligible `consultation`, `prescription`, `inpatient`, `dental_treatment`; ineligible `otc_supplement`, `cosmetic`. These are **engineering placeholders, not Japanese tax-law guidance**; legal thresholds and official category mapping are TODO for product/tax counsel.

## 4. Data classification summary

| Category | Sensitivity | Logged? | Sent to analytics/crash? |
|---|---|---|---|
| identityPairwise | Extremely high | Never | Never |
| individualNumber | Extremely high | Never | Never |
| healthMeasurements | High | Event names only | Never |
| clinicalDocuments | Very high | Never (also filenames) | Never |
| expenses | High | Event names only | Never |
| telemetry | Low only if strictly non-PII | n/a | Policy denies all today |

## 5. Device vs backend partition

| Data | Device (native) | Browser (web) | Backend (PLANNED) |
|---|---|---|---|
| Tokens / session | Keychain / Keystore | **Not in browser JS**; httpOnly cookie only | BFF holds tokens; API validates |
| pairwiseSub | Platform-protected storage | Server session; shown only as needed | Firestore, keyed account id |
| Individual number | Not persisted | Not persisted | Isolated, KMS/CMEK, narrowly authorized, only if reviewed |
| Health measurements | Local cache scoped to purpose (after Wellness) | In-memory / not persisted client-side | Firestore metadata after consent |
| Clinical documents | Local quarantine until upload | Transient in memory during upload; no persistent store | Encrypted object storage (service TBD), quarantine then review |
| Expense drafts | Local drafts | Server-held drafts; no localStorage | Firestore |
| Consent | Local record + server ledger | Server-side ledger | Firestore, append-only audit |
| Telemetry | Not configured | Not configured | Non-PII only, after vendor review |

## 6. Retention and deletion (policy TODO)

Retention periods are **TODO for product/legal** — none are decided and none are invented here. Engineering requirements that hold regardless:

- User-invokable deletion for clinical documents, health metadata and expense data, covered by tests before release (`clinical-baseline.md`).
- Deletion must cover derived data (extractions, AI insights, caches, exports) and backups per a policy to be set.
- Audit events keep metadata only and outlive the data they describe only if legal sets that.
- Quarantined, rejected files are discarded promptly (period TBD).

## 7. Schema evolution rules

1. Any new sensitive field needs a data-classification review and a `docs/security.md` / this-file update before merge (contracts README).
2. Compatibility notes and **synthetic** fixtures accompany every contract change.
3. `additionalProperties: false` stays; add fields explicitly and version the schema `$id` path when breaking (see [api-contracts.md](api-contracts.md)).
4. Planned contract expansion order follows delivery stages: expenses detail → wellness measurements → clinical document status → AI insight. Identity expansion is blocked.
5. TypeScript types for web, and Swift/Kotlin models, are generated or validated against the schema (tooling PLANNED).
