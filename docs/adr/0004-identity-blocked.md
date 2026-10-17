# ADR-0004: Identity blocked pending official IdP, legal, and threat model

**Status:** Accepted (Blocked) · **Date:** 2026-10-17

## Context
The My Number sign-in path depends on official provider documentation, relying-party registration, and compliance review. Guessing endpoints, scopes, or card flows is a security and legal risk.

## Decision
No OIDC issuer, endpoint, scope, client ID, certificate/PIN handling, or NFC code is committed. Design only: authorization code + PKCE, state/nonce, claim validation, pairwiseSub as app key, individual number never a client key. Config names only in `.env.example`.

## Consequences
- Identity stage, and every feature that needs an authenticated user against a real backend, waits.
- Web and native share one design; the web BFF is the relying party. Whether a web flow is allowed is UNCONFIRMED.
- Unblock criteria: official docs, registration, legal sign-off, threat-model review, owner approval.

## Alternatives
Interim non-official login: rejected.
