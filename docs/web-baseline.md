# Web client baseline

This note records the **web** client posture for the My Number Health & Wellness Portal. It complements [STACK.md](STACK.md), [architecture.md](architecture.md), and the native wellness/clinical baselines. It does **not** complete Identity, invent IdP endpoints, or enable clinical upload.

Related code: `apps/web` (Vite + React + TypeScript shell).

## Why a third client

The product shares **contracts**, not UI. Web is a first-class portal surface for review, expenses preview, and (later) authenticated account flows on a desktop or laptop browser. It does **not** replace native apps for device health kits or NFC card flows.

## Capability matrix (web)

| Capability | Web posture | Notes |
|---|---|---|
| Identity (OIDC + PKCE) | **Not wired** | Same blocked path as mobile; browser redirect URI must come from official registration |
| Secure token storage | **NotConfigured** | Target later: httpOnly secure cookies and/or Web Crypto; never `localStorage` for tokens |
| Local re-entry | **NotConfigured** | Target later: WebAuthn / passkeys after Identity exists |
| Device wellness (HealthKit / Health Connect) | **Unavailable** | Mobile-only. Web may later *display* server-synced summaries; it must not pretend to read device kits |
| NFC / Digital Auth App reader | **Unavailable** | Not a browser capability in this product |
| Clinical upload pipeline | **NotConfigured** | Same stages as native (select → validate → quarantine → upload → review); no upload URL |
| Consent / crash / telemetry | **NotConfigured** | No analytics SDK; PII-safe logging rules still apply |
| Expenses / AI views | Shell only | Same contracts; calculators and Vertex jobs remain later stages |

## Architecture rules

- Feature screens under `apps/web/src/features` must not call browser credential, WebAuthn, or file-upload APIs directly.
- Only `CompositionRoot` may construct `NotConfigured*` / `Unavailable*` adapters (`apps/web/src/core`).
- No Expo, no React Native Web bridge, no shared UI package with native.
- Do not invent `HEALTH_APP_*` values; use `.env.example` names only when wiring is approved.

## Delivery

| Slice | Exit criteria |
|---|---|
| Foundation shell | Tab shells + CompositionRoot stubs; `pnpm` typecheck/build in CI |
| Spec S16 owner-approved | Web IA + capability matrix accepted |
| Identity (later) | Official web redirect + threat model; still fail closed until then |
| Clinical (later) | Same pipeline review as native; no guessed storage URLs |

## Non-goals

- Emulating HealthKit or Health Connect in the browser
- Shipping a marketing landing page as the product shell
- Guessing My Number web SSO endpoints or scopes
