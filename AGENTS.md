# AGENTS.md — health-app

Operating notes for any coding agent working in this repository.

## Read first

1. **[docs/CONTEXT.md](docs/CONTEXT.md)** — living product status, blockers, who owns what. **Update it** on every active workday and on every major direction change (SunVeda house rule for all projects).
2. [README.md](README.md)
3. [docs/architecture.md](docs/architecture.md), [docs/security.md](docs/security.md), [docs/privacy-baseline.md](docs/privacy-baseline.md)

## Product in one line

Native iOS + Android My Number Health & Wellness Portal — share contracts, not UI; fail closed on Identity and clinical uploads until officially approved.

## Engineering rules

- Keep platform adapters behind composition roots; feature screens must not import HealthKit / Health Connect / NFC / Keychain / Keystore APIs directly
- Do not invent My Number endpoints, scopes, or credentials
- No real clinical uploads or Identity wiring without owner + compliance path
- Prefer small PRs; coordinate standards with **Chief of Engineering**; product ownership with bot **Health App**

## Context hygiene (required)

After meaningful work (feature merge, blocker change, scope pivot):

1. Update the dated **Current status** section in `docs/CONTEXT.md`
2. Keep bullets short; link PRs by number
3. If you only ship code and skip CONTEXT, the next agent starts blind — treat that as a bug

## Hold note (2026-09-15)

Feature work is **paused**. Dual active SunVeda focus is jkk-watch + Learn AI Now. Until CoS resumes health-app, only CONTEXT/docs hygiene (and owner-requested security fixes).
