# Web app

Vite + React + TypeScript shell for the My Number Health & Wellness Portal web client.

Platform adapters (secure store, WebAuthn, clinical upload, identity session) live under `src/core` and are constructed only by `CompositionRoot`. Feature screens under `src/features` must not call credential, WebAuthn, or file-upload APIs directly.

Current adapters are **NotConfigured** or **Unavailable** (device health kits and NFC are unavailable on web). No OIDC issuer, upload URL, or analytics SDK is wired. See [`docs/web-baseline.md`](../../docs/web-baseline.md) and [`docs/STACK.md`](../../docs/STACK.md).

## Scripts

```bash
# from repo root
pnpm install
pnpm --filter @health-app/web dev
pnpm --filter @health-app/web typecheck
pnpm --filter @health-app/web test
pnpm --filter @health-app/web build
```

## Screenshots

Documented tab captures: [`docs/web-screens.md`](../../docs/web-screens.md).

Regenerate (preview must be running on port 4173):

```bash
pnpm --filter @health-app/web build
pnpm --filter @health-app/web preview --host 127.0.0.1 --port 4173
node scripts/capture-web-screenshots.mjs
```

## Identity & privacy

Do not invent My Number IdP endpoints or put tokens in `localStorage`. Env var *names* match the root `.env.example` when wiring is approved.
