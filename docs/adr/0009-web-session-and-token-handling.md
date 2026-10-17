# ADR-0009: Web session — BFF, httpOnly cookies, no browser tokens

**Status:** Accepted (owner approved the Next.js + BFF design 2026-10-17) · **Date:** 2026-10-17

## Context

Browsers have no hardware-backed secret store comparable to Keychain/Keystore. Any token reachable by JavaScript is exposed to XSS and malicious extensions. The web client handles identity, health and financial data.

## Decision

- A server-side BFF owns the session. If the official IdP web flow is usable, the BFF is the confidential OIDC relying party (design only; IdP details UNCONFIRMED and BLOCKED). Otherwise the fallback sign-in methods in [ADR-0010](0010-web-auth-strategy.md) create the same kind of server session.
- The browser holds only an opaque session identifier in a cookie: `__Host-` prefix, `HttpOnly`, `Secure`, `SameSite`, short lifetime with rotation.
- No access, refresh, or ID tokens, and no identity/health/clinical/expense data, in `localStorage`, `sessionStorage`, IndexedDB, or JS-readable cookies.
- CSRF defense: SameSite, anti-CSRF token, Origin/Fetch-Metadata checks.
- Re-authentication / step-up uses WebAuthn (passkeys); no custom biometrics. NFC / card reading is not supported on web.
- Strict CSP (nonces), no third-party scripts on authenticated pages.

## Consequences

- Requires server sessions and a session store (Postgres per [ADR-0011](0011-hosting-vercel-neon.md); only hashed session ids, no PII, no tokens exposed). Adds BFF operational surface.
- Stateless-SPA simplicity is lost; revocation and logout are server-controlled (a benefit).
- Web clinical upload and wellness are limited (manual/import only for wellness).
- Normative controls and CI gates are listed in [threat-model.md](../security/threat-model.md) §3.

## Alternatives

Bearer tokens in memory with refresh in cookie (still exposes access tokens to XSS); tokens in storage (rejected); third-party auth SDK (rejected: scope/vendor unknown, nothing invented).
