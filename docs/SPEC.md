# Product specification index

**Mode:** Docs & specification before implementation.  
**Audience:** Product owner, CoE, Health App bot, any agent picking up the repo.  
**Companion:** [STACK.md](STACK.md) — every technology on one screen.

This file is the **table of contents for what we will specify**. It does not invent My Number endpoints, scopes, credentials, HealthKit/Health Connect permission strings, or production URLs. Where a topic is blocked on official provider docs or legal review, the status is **Blocked**, not TBD-with-a-guess.

---

## How to use this index

1. Open [STACK.md](STACK.md) for the tech map.
2. Use the tables below to see which specs exist, which need drafting, and what blocks coding.
3. New feature code stays **out of scope** until the matching row is **Owner-approved**.
4. After each approved spec slice, update [CONTEXT.md](CONTEXT.md) and link the PR.

---

## Specification catalog

| ID | Spec | Purpose | Status | Source of truth today |
|---|---|---|---|---|
| S0 | Stack & boundaries | One-screen tech + system map | **Draft** | [STACK.md](STACK.md), [architecture.md](architecture.md) |
| S1 | Product vision & non-goals | Who it is for, what we refuse to build | **Needs draft** | CONTEXT one-liner only |
| S2 | Information architecture | Screens, tabs, empty/error/blocked states (iOS/Android/web) | **Needs draft** | Feature tab shells in code (no UX spec) |
| S16 | Web client | Web stack, capability matrix, hosting | **Draft** | [web-baseline.md](web-baseline.md), `apps/web` shell |
| S3 | Shared contracts | JSON Schema for portal payloads | **Scaffold** | `packages/contracts/schemas/portal.json` |
| S4 | Security & threat model | Controls, checklist, fail-closed rules | **Baseline draft** | [security.md](security.md) |
| S5 | Privacy & data classification | Categories, device boundary, logging | **Baseline draft** | [privacy-baseline.md](privacy-baseline.md) |
| S6 | Identity (My Number IdP) | OIDC/PKCE, storage, pairwise sub | **Blocked** | architecture + security notes; no official wiring |
| S7 | Wellness (device health) | Scopes, consent UX, sync flag | **Intent only** | [wellness-baseline.md](wellness-baseline.md) |
| S8 | Clinical documents | Upload → validate → quarantine → review | **Intent only** | [clinical-baseline.md](clinical-baseline.md) |
| S9 | Medical expenses | Eligibility rules, export confirmation | **Fixtures only** | `packages/domain` (synthetic; not tax law) |
| S10 | AI insights | Grounding, provenance, disclaimers | **Needs draft** | architecture AI boundary |
| S11 | Backend services | Cloud Run split, Firestore, KMS, Armor | **Target sketch** | architecture “Backend target” |
| S12 | Release & stores | Signing, TestFlight, Play internal | **Checklist** | [release-checklist.md](release-checklist.md) |
| S13 | Observability & IR | Crash/telemetry policy, incident response | **Partial** | privacy crash decision; IR TBD |
| S14 | Accessibility & i18n | JP-first copy, a11y acceptance | **Needs draft** | — |
| S15 | Testing & CI gates | Required checks per stage | **Partial** | `.github/workflows/native-ci.yml` |

Status legend: **Needs draft** → **Draft** → **Owner-approved** → **Ready to implement**. **Blocked** cannot move to implement without external approval.

---

## Writing order (docs-first restart)

Work top-down. Do not skip S6/S7/S8 into “implementation notes” that invent APIs.

| Order | Deliverable | Exit criteria |
|---|---|---|
| 1 | **S0** Stack freeze | Owner confirms STACK.md matches intended product (incl. web) |
| 2 | **S16** Web baseline | Owner confirms web capability matrix (kits/NFC unavailable) |
| 3 | **S1** Vision & non-goals | Explicit out-of-scope list (no other SunVeda products, no guessed IdP) |
| 4 | **S2** IA / screen inventory | Every tab + blocked Identity/Clinical empty states for all three clients |
| 5 | **S4 + S5** Security/privacy pass | Owner + CoE acknowledge baseline; legal TODOs listed |
| 6 | **S3** Contract expansion plan | Which schemas grow before each stage (no fake fields) |
| 7 | **S11** Backend service sketch | Service list + data partitions; still no live projects |
| 8 | **S6** Identity spec shell | Placeholders for issuer/client/redirect (incl. web); fill only from official docs |
| 9 | **S7 / S8** Wellness & clinical | Purpose-tied scopes and pipeline stages; kits still off on mobile; web Unavailable for kits |
| 10 | **S9 / S10** Expenses & AI | Rules provenance; AI safety copy |
| 11 | **S12–S15** Release, a11y, CI | Checklists ready before any store/web host build |

---

## Hard constraints (every spec must honor)

- Fail closed: Identity, clinical upload, and mobile kit reads stay unavailable until officially approved; web kits/NFC stay Unavailable.
- Composition root only: HealthKit, Health Connect, NFC, Keychain, Keystore, biometrics, WebAuthn, and browser credential APIs never imported from feature screens.
- No invented endpoints, OIDC scopes, certificate flows, or scanner vendors.
- Synthetic fixtures only in git; no real My Number, health payloads, or credentials.
- AI output is advisory; show sources, timestamps, uncertainty; never diagnose.
- Individual number is not a general-purpose client identifier.

---

## Open questions for the owner

Capture answers in CONTEXT when decided; do not encode guesses into schemas.

1. Primary launch language(s) and region assumptions beyond Japan My Number context?
2. Which store tracks first (TestFlight vs Play internal) and account ownership?
3. Web hosting target (Cloud Run static, Firebase Hosting, other) and custom domain?
4. Is Vertex AI in scope for v1, or post-expenses?
5. Must the individual number ever appear on a client, or pairwise-sub only?
6. Expense feature: informational eligibility only, or export toward a specific tax workflow?

---

## Related

- [CONTEXT.md](CONTEXT.md) — living status  
- [STACK.md](STACK.md) — tech one-pager  
- [architecture.md](architecture.md) — staged delivery  
- Root [AGENTS.md](../AGENTS.md) — agent operating rules  

*Last updated: 2026-09-29*
