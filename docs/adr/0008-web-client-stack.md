# ADR-0008: Web client — Next.js + React + TypeScript with a BFF

**Status:** Accepted (owner decision 2026-10-17: Next.js + BFF approved) · **Date:** 2026-10-17

## Context

The owner requires the product to ship as **web + iOS + Android**. The web client must share `packages/contracts` and `packages/domain` rules, use the same CompositionRoot + NotConfigured adapter pattern, and meet a stricter token-handling bar than native (no secure local store in browsers). Identity remains blocked, so the web client cannot sign in against a real IdP yet. `apps/web` does not exist today.

Requirements that drive the choice: server-side session boundary (BFF with httpOnly cookies), strict CSP with nonces, TypeScript sharing with the existing pnpm workspace (`apps/*`, `packages/*`), hosting per ADR-0011 (Vercel), and mature ecosystem with active security maintenance.

## Decision

**Next.js (App Router) + React + TypeScript (strict)**, deployed on Vercel Functions ([ADR-0011](0011-hosting-vercel-neon.md); this supersedes the earlier Cloud Run assumption), with route handlers acting as the **BFF** (OIDC relying party design, session cookies, same-origin API proxy). Package manager: pnpm (already pinned). Render authenticated pages dynamically with `no-store`; CSP via middleware with per-request nonces. Tests: Vitest + Testing Library for units, Playwright for E2E (to be confirmed at implementation). `apps/web` mirrors the native layering: `core/` interfaces + `composition-root.ts` + `NotConfigured*` adapters, features consume dependencies only.

Why this over alternatives: the BFF and the UI live in one deployable with server-rendered, nonce-able CSP and cookie handling built in; one language across web UI, BFF, and the TS domain port; no separate gateway needed to keep tokens out of the browser.

## Consequences

- Tokens never reach browser JS (ADR-0009). A server runtime is required — pure static hosting is not enough.
- Larger framework surface and supply chain than a Vite SPA; mitigated by lockfile, Dependabot, pinned versions, minimal deps.
- React/TypeScript here does **not** reverse ADR-0001: web is a web app, not a mobile UI-sharing layer; no React Native/Expo.
- CI needs new jobs (install, typecheck, lint, test, build) and extended forbidden-pattern gates before any code merges.
- TS types generated from JSON Schema; TS port of eligibility rules validated against `packages/domain/fixtures`.
- Sequencing: web implementation starts after contracts/domain hardening and owner-approved specs; web Identity stays blocked with the rest.

## Alternatives considered

| Option | Pros | Cons / why not recommended |
|---|---|---|
| **Vite + React SPA** + separate BFF service | Smaller framework; simple static hosting | Needs a second service for BFF/cookies/CSP nonces; more moving parts to secure; SPA tempts client-side token handling |
| **Remix / React Router framework mode** | Good server-first model, web standards | Smaller ecosystem for our needs; comparable to Next.js with less team familiarity (assumed) |
| **SvelteKit / Nuxt** | Lean, server-capable | Different language ecosystem from the React default; less shared tooling; no strong benefit here |
| **Kotlin/Wasm or Flutter Web** | Code share with native | Rejected: contradicts "share contracts not UI", poor a11y/CSP story |
| **PWA only, no server** | Cheap | Cannot keep tokens out of the browser; violates ADR-0009 |

The owner approved Next.js + BFF on 2026-10-17. The BFF + cookie requirement (ADR-0009) is the constraint that must hold under any option.
