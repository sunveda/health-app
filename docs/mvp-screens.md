# MVP screens — My Number Health & Wellness Portal

**Status:** Draft for review (docs-first resume, 2026-09-29)  
**Tab shell (unchanged):** Home · Health · Expenses · Settings  

Aligned with existing Feature modules on iOS (`HealthApp/Features`) and Android (`features`). There is **no** Clinical tab; clinical status appears on Home / Health / Settings. Screens consume **injected** adapter surfaces only — never kits, pickers, or IdP clients directly.

Cross-links: [`product-spec.md`](product-spec.md), [`technical-spec.md`](technical-spec.md), [`architecture.md`](architecture.md), [`privacy-baseline.md`](privacy-baseline.md), [`wellness-baseline.md`](wellness-baseline.md), [`clinical-baseline.md`](clinical-baseline.md), [`security.md`](security.md).

No real PII, My Numbers, tokens, or clinical content in examples. Use labels like `Example User` and synthetic fixture ids only.

---

## Shared rules for all screens

| Rule | Detail |
|---|---|
| Adapter access | Via CompositionRoot-injected interfaces only |
| Identity | Treat as **blocked / not configured** until IdP registration + review |
| Permissions | No system HealthKit / Health Connect / NFC / biometrics / document-picker prompts in MVP stubs |
| Logging | Event names only; use LogRedaction; never interpolate sensitive values |
| Copy | PrivacyCopyPlaceholder / “Not configured” where legal copy or kits are missing — not statute citations |
| Feature flags | Wellness flag **disabled**; do not branch on compile-time kit imports |

---

## 1. Home

### Purpose

Landing overview: session / identity posture, high-level wellness and clinical readiness, and a short path into the other tabs. Orients the user that the app is a portal with staged capabilities — not a live My Number console yet.

### Primary actions (MVP)

| Action | Stub behavior | Future |
|---|---|---|
| View identity status | Show **Blocked / Not configured** — no sign-in sheet | Launch reviewed OIDC/PKCE handoff |
| View wellness summary line | Map HealthDataSource + disabled flag → “Not configured” | Show last sync / granted scopes after Wellness stage |
| View clinical summary line | Map ClinicalDocumentPipeline → `not_configured` | Show in-progress / idle after Clinical stage |
| Navigate to Health / Expenses / Settings | Tab switch | Same |
| Pull to refresh status | Re-read adapter snapshots only | Same + optional backend session check |

### Data shown (MVP)

- Coarse status chips: Identity, Wellness, Clinical, Expenses readiness (all stub-honest).
- App name / build channel if available (non-identifying).
- **No** pairwise subject, individual number, samples, or document titles.

### Gates

- CompositionRoot constructed once at app entry.
- Identity blocked → Home must not imply signed-in My Number session.
- TelemetryPolicy deny → no analytics fire on open.

### Stub vs future

| Aspect | Stub (now) | Future |
|---|---|---|
| Auth CTA | Disabled or “Coming after Identity review” | Official IdP sign-in |
| Health preview | Empty / not configured | Aggregated non-diagnostic counts from granted scopes |
| Clinical preview | Not configured | Count of quarantined / reviewed reports (metadata only) |

---

## 2. Health

### Purpose

Wellness and clinical **capability surfaces** in one tab: explain intended later reads, show adapter availability, and keep permission UX honest. Does not request kit access in MVP.

### Primary actions (MVP)

| Action | Stub behavior | Future |
|---|---|---|
| Inspect wellness status | Availability + permission state + empty scopes + disabled flag → “Not configured” | Purpose explanation → system read prompt for minimized scopes |
| Request wellness access | Fail closed (`not configured` / `unavailable`) | `requestReadAccess` for `steps`, `heartRate` only after review |
| Inspect clinical pipeline status | `not_configured` surface mapping | Selection → validation → quarantine → upload → review → deletion |
| Start document selection | Fail closed; **no** picker | Core/platform picker adapter (native) and browser file-input adapter (**web, in MVP scope**) after security review |
| Open purpose / privacy explainer | Placeholder copy + link to Settings privacy section | Final JA/EN legal notices |

### Data shown (MVP)

- Wellness: feature flag state, mapped contract status (`not_connected` for stubs).
- Clinical: surface status `not_configured` / `unavailable` / later `idle` | `in_progress`.
- Engineering scope labels for education only: `steps`, `heartRate` (not requested).
- **No** quantity samples, workouts, filenames that encode diagnoses, or extracted text.

### Gates

- WellnessFeatureFlag remains `.disabled` in shipping CompositionRoot until kit adapters reviewed.
- Screens must not import HealthKit / Health Connect.
- Clinical adapters must not present `UIDocumentPickerViewController` / `ACTION_OPEN_DOCUMENT` or request photo permissions.
- ConsentStore not configured → do not pretend consent was recorded for kit/upload purposes.

### Stub vs future

| Aspect | Stub (now) | Future |
|---|---|---|
| Samples list | Empty | User-visible measurements with timestamps; never log raw samples |
| Sync | WellnessSyncClient no-op | Backend sync only after API design + consent |
| Upload CTA | Hidden or disabled with explanation | Encrypted upload through the BFF/API into private quarantine storage (**URL TBD — do not invent**); same on web |
| Upload result states | n/a | `quarantined` (scanning) → `accepted` for review, or generic `rejected`; messages for `too_large` / `unsupported_type`; never shows file contents in lists |

---

## 3. Expenses

### Purpose

Medical-expense **eligibility preview** using synthetic fixtures and transparent calculation messaging. Educates that results are not tax advice and that export requires explicit confirmation later.

### Primary actions (MVP)

| Action | Stub behavior | Future |
|---|---|---|
| Browse synthetic fixture scenarios | Load from `packages/domain` fixtures (or mirrored display models) | Import user-confirmed expense lines |
| Run eligibility preview | Pure fixture engine / mapped results | Same + server-side aggregation when Tax service exists |
| Confirm understanding disclaimer | Required checkbox / acknowledgment before showing “export preview” placeholder | Required before any real export file |
| Export | Disabled / “Not available until Expenses stage exit” | Local file or tax-service handoff after review |
| Edit real receipts | Out of MVP | Capture / OCR only after Clinical-adjacent review |

### Data shown (MVP)

- Synthetic scenario names and amounts (obviously fake).
- Eligibility outcome labels from fixtures.
- Disclaimer: not official tax guidance; consult a qualified professional / tax office.
- **No** real clinic names tied to the user, bank accounts, My Numbers, or claim submission IDs.

### Gates

- Fixtures remain synthetic and cannot be mistaken for production records.
- Export path requires explicit review step before generating shareable output ([`security.md`](security.md)).
- Do not log expense line-item narratives.

### Stub vs future

| Aspect | Stub (now) | Future |
|---|---|---|
| Source of truth | `packages/domain` synthetic fixtures | User data + authorized Tax service |
| Calculation | Platform-neutral placeholder engine | Versioned rules with audit trail |
| Export | Placeholder | Confirmed export + retention policy |

---

## 4. Settings

### Purpose

Privacy posture, consent/purpose scaffolding visibility, auth placeholder, and diagnostic policy honesty (crash/telemetry not configured). Primary place for “why is Identity blocked?” education.

### Primary actions (MVP)

| Action | Stub behavior | Future |
|---|---|---|
| View identity / sign-in | Blocked explanation; no OIDC | Start / resume official IdP session; sign out; local biometric unlock |
| View consent / purpose list | PurposeRegistry ids + ConsentStore “not configured” | Grant / deny / withdraw per category with legal copy |
| View privacy placeholders | PrivacyCopyPlaceholder strings | Product/legal notices, retention, deletion |
| Crash reporting | Show **disabled / not configured** | Vendor SDK only after PII review |
| Telemetry | Show deny-all policy | Non-PII events only if approved |
| Open licenses / about | Static | Same |
| Delete local data | Best-effort clear of non-secret prefs; stubs hold nothing sensitive | Full deletion aligned with retention policy |

### Data shown (MVP)

- Purpose identifiers (engineering), not legal notices.
- Adapter configuration states for Consent, Crash, Telemetry, Wellness flag, Clinical pipeline.
- Bundle id `com.sunveda.healthapp` and build metadata if useful.
- **Never** display individual number, tokens, or pairwise subject in MVP.

### Gates

- ConsentStore records (when later enabled) store only category + decision + purpose id — never report contents or individual numbers.
- Settings must not enable crash SDK by flipping a hidden switch without CompositionRoot + review.
- Auth controls remain non-functional until Identity stage exit criteria are met.

### Stub vs future

| Aspect | Stub (now) | Future |
|---|---|---|
| Sign in / out | Placeholder | PKCE OIDC + secure token storage |
| Biometric lock | Not offered | After verified identity |
| Legal documents | Placeholder TODO | Hosted / in-app JA/EN copy |

---

## 5. Consent & auth placeholders (cross-cutting)

These are **not** separate tabs; they appear as sheets or sections from Home / Settings (and gated CTAs on Health).

### Auth placeholder

| Field | MVP |
|---|---|
| Purpose | Communicate that My Number sign-in is blocked pending official provider docs, registration, compliance, and threat-model review |
| Actions | Dismiss; link to Settings explanation |
| Forbidden | Collecting email/password as a fake IdP; hardcoding authorize URLs; storing fake tokens |

### Consent placeholder

| Field | MVP |
|---|---|
| Purpose | Show that each data category needs purpose id + decision before kit/network use |
| Actions | View purpose summaries; acknowledge that lawful basis / notices are TODO for product/legal |
| Forbidden | Recording “granted” for Clinical or Wellness in a way that unlocks real kits while adapters are NotConfigured |

---

## 5b. Web client screen notes (owner scope 2026-10-17; PLANNED)

Same four tabs as responsive layouts. Differences from native:

| Topic | Web behavior |
|---|---|
| Sign-in screen | Shows which method applies: official IdP web flow (only if confirmed free and supported, UNCONFIRMED), else passkey, else email one-time code. **Fallback methods are labelled "account sign-in", never "My Number verified"**; identity-gated actions stay disabled for them |
| Health tab: wellness | No device sync; manual entry or file import only; copy explains why |
| Health tab: clinical upload | **In MVP scope.** File chooser behind adapter; client shows type/size limits as hints (server enforces); states: not configured → uploading → quarantined/scanning → ready for review / rejected → deleted. Disabled with "Not configured" until security review |
| Settings | Shows session method, active sessions and sign-out; data deletion entry |
| Accessibility | Keyboard-operable file input and status announcements (`aria-live`) |
| Never on web | NFC card reading, persistent local storage of identity/health data, third-party scripts |

---

## 6. Screen → stage mapping

| Screen | Hygiene | Sec/privacy | Identity | Wellness | Clinical | Expenses | AI |
|---|---|---|---|---|---|---|---|
| Home | Shell | Status honesty | Blocked CTA | Status line | Status line | Link | Deferred (ADR-0012) |
| Health | Shell | Redaction | — | Stub → real reads | Stub → pipeline (native + web) | — | Deferred |
| Expenses | Shell | Export gate | — | — | — | Fixtures → deepen | — |
| Settings | Shell | Consent/crash/telemetry UI | Auth placeholder (web: method per ADR-0010) | Flag display | Pipeline display | — | Deferred |

---

## 7. Acceptance checklist (MVP screens)

- [ ] Four tabs present and navigable on iOS and Android shells  
- [ ] Home never claims an authenticated My Number session  
- [ ] Health shows wellness + clinical as not configured; no system permission sheets  
- [ ] Expenses uses synthetic fixtures + disclaimer; export disabled or gated  
- [ ] Settings shows crash/telemetry not configured and Identity blocked  
- [ ] No screen imports kits / pickers / IdP SDKs directly  
- [ ] Web: clinical upload shown disabled/not configured until review; no public file URLs; fallback auth never labelled My Number verified
- [ ] Examples and UI previews contain no real PII or health payloads  

---

*Last updated: 2026-10-17 (Asia/Tokyo): web screens and web clinical upload scope added.*
