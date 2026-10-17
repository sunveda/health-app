# ADR-0010: Web auth strategy — official IdP web flow if free and supported, else passkeys, else email OTP

**Status:** Accepted as a decision tree (owner, 2026-10-17). Identity itself stays **BLOCKED** ([ADR-0004](0004-identity-blocked.md)) · **Date:** 2026-10-17

## Context

Browsers cannot read the My Number card (no NFC path, see [system-architecture §8](../architecture/system-architecture.md)). Whether the official My Number IdP offers a web redirect flow to relying parties, at what cost, and with what assurance is **UNCONFIRMED**. The owner wants no paid auth dependency unless unavoidable.

## Decision

Evaluate in this order and stop at the first option that is available, officially supported **and free**:

1. **Official IdP web flow** (OIDC authorization code + PKCE through the BFF, [ADR-0009](0009-web-session-and-token-handling.md)). Only if official documentation confirms web relying-party support, registration is possible, and there is no fee. No endpoint, scope, client ID, or assurance level is assumed here; all remain UNCONFIRMED/TBD.
2. **Passkeys (WebAuthn)**, self-implemented in the BFF with an open-source library (e.g. SimpleWebAuthn; **not yet evaluated**, license/maintenance/security review needed). No per-user fee. Credential public keys live in Postgres; origin and RP ID are pinned to the production domain; user verification required.
3. **Email one-time password (OTP)** only as a recovery/last-resort method. It needs an email provider: **cost and sub-processor are TBD** (price not verified; most providers have free tiers with limits, not confirmed here). Short-lived, single-use, hashed codes, rate limited, no account enumeration, uniform responses.

Any option yields only a server session cookie ([ADR-0009](0009-web-session-and-token-handling.md)). Composition: an `IdentitySession` adapter on web stays `NotConfigured` until review, as on native.

## Legal guardrail

A fallback method (passkey or email OTP) authenticates **an account of this app**. It is **not My Number identity verification** and must never be described, labelled, or used as such: no "verified with My Number" copy, no features that require My Number–verified identity, and the individual number is never collected or stored on web. Whether the product may offer any non-My-Number account for these data categories is a **legal review item**.

## Consequences

- Web may ship with a different (weaker) assurance than native card-based sign-in; UI must say which method signed the session, and identity-gated features stay disabled for fallback sessions.
- Account recovery, passkey loss, device change, and step-up flows need design (spec TBD).
- Email OTP adds a sub-processor and a phishing/enumeration surface; keep it off unless needed.
- Threat model §2.3 and §3 cover the controls; CI gates in `scripts/check-web-patterns.py` confine auth SDKs to `apps/web/src/core/`.

## Alternatives

Hosted auth vendors (Auth0, Clerk, etc.): rejected for now on cost and sub-processor grounds, not evaluated in detail. Password login: rejected (credential storage burden, phishing). Magic links: similar to email OTP, same provider cost.
