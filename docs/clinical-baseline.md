# Clinical baseline (upload/review stubs)

This note is the Clinical capability record for the native clients. It describes intended later pipeline stages, that real upload and document-picker wiring is deferred, and the NotConfigured posture. It does **not** complete Clinical, Identity, Wellness, or legal review.

Related: [`architecture.md`](architecture.md), [`privacy-baseline.md`](privacy-baseline.md), [`security.md`](security.md), [`wellness-baseline.md`](wellness-baseline.md).

## Status of this slice

CompositionRoot wires `ClinicalDocumentPipeline`, `ReportUploadClient`, `FileValidation`, and `QuarantineStore` as **NotConfigured** (with matching Unavailable stubs for tests). Feature modules read availability and failure state from those adapters. They must not import document pickers, request files or photo permissions, open a network session, or persist report bytes.

| Item | This slice | Not in this slice |
|---|---|---|
| Adapter surface | Availability, last failure, rejected select / validate / quarantine / upload / review / delete | Live document picker, resumable transfer, server processing |
| Document picker | Mentioned only behind the pipeline adapter; stubs never present it | `UIDocumentPickerViewController`, `ACTION_OPEN_DOCUMENT`, photo-library sheets |
| Validation | Interface only; no byte inspection | MIME sniffing, vendor malware scanners, content extraction |
| Quarantine | Interface only; stores nothing | On-device holding of real or synthetic report files |
| Upload | Interface only; no URL, bucket, or SDK | AWS / GCS / Firebase Storage clients, resumable multipart transfer |
| Review / deletion | Interface only; no documents to show or erase | Extraction, summarization, retention-policy deletion |
| Fixtures | None required; tests use synthetic ids only | Real medical documents, diagnoses, or filenames that encode them |

Identity remains blocked on IdP / legal review. Wellness remains NotConfigured kit stubs. This slice does not unblock OIDC, NFC, biometrics, Keychain/Keystore, HealthKit, Health Connect, backend clients, or Expenses deepening.

## Intended later pipeline stages

Engineering stage identifiers used by the stubs: `selection`, `validation`, `quarantine`, `upload`, `review`, `deletion`. Stubs never advance these. There is **no** clinical wire contract in `packages/contracts` yet and no invented upload URL.

When a later slice implements the pipeline, the intended order is:

1. **Selection** — user chooses a report through a Core/platform picker adapter after purpose-tied UX. No permission prompt until that adapter is reviewed.
2. **Validation** — client-side type and size checks before anything is stored. Principles only: enforce an allowlist and a maximum size; reject archives and executables; never log contents. Exact byte limits and type lists are TBD with product/security. Do not require a named MIME or malware-scanner vendor here.
3. **Quarantine** — hold metadata (and later, encrypted bytes) locally until upload is approved. Quarantine is not a cache of extracted diagnoses.
4. **Upload** — resumable, encrypted in transit, to an authorized clinical-document service after that service exists. Do not invent Cloud Storage, S3, or Cloud Run URLs in the client.
5. **Review** — user-facing review of what was accepted before extraction or AI. Contents stay unloggable (see privacy baseline).
6. **Deletion** — user-initiated delete plus retention-policy delete; both must be testable before the Clinical stage can exit.

Server-side malware scanning, redaction, and extraction are backend concerns. This note does not pick a scanner product or a bucket name.

## NotConfigured posture

`ClinicalDocumentPipeline.requestDocumentSelection` fails with not configured / unavailable and must not present a system picker or request permissions. `FileValidation.validate`, `QuarantineStore` writes and lists, `ReportUploadClient.upload`, and pipeline review/delete fail the same way.

Screens should treat NotConfigured status as the “Not configured” user-visible path. Do not infer picker or storage availability from device APIs.

When real adapters are reviewed later, only CompositionRoot may construct them. Feature modules must not branch on compile-time picker, upload-SDK, or storage imports.

## Exit criteria for the real Clinical stage

When upload/review is implemented, exit criteria are:

1. Secure upload (encrypted in transit, resumable, authorized) with no hardcoded object-storage URLs in the client.
2. File validation (type/size; plus server-side scanning once a reviewed service exists) before processing.
3. Quarantine of untrusted files until validation completes.
4. User review of accepted reports before extraction or downstream use.
5. Deletion that the user can invoke, covered by tests, aligned with the retention policy once product/legal set it.
6. Keep adapters behind CompositionRoot; feature screens never import document pickers or upload SDKs.
7. Log event names only; never log report bytes, extracted text, diagnoses, or identifying filenames.

This stub slice records those criteria. It does not satisfy them by pretending an upload pipeline exists.

## Surface mapping

There is no `clinicalDocument` object in `packages/contracts` yet. Pure mappers translate adapter snapshots to a local surface status for shells:

- Not configured (or a not-configured failure) → `not_configured`
- Unavailable (or an unavailable failure) → `unavailable`
- Ready, no failure, no active stage → `idle`
- Ready, no failure, an active stage → `in_progress`
- Otherwise → `not_configured`

Stubs therefore map to `not_configured`. No backend upload client is implied.

## UI

Health and Settings shells (and a Home status line) read adapter status and show “Not configured”. Tab structure is unchanged: Home / Health / Expenses / Settings. There is no Clinical tab. Do not infer upload readiness from device APIs.
