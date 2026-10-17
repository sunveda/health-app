# Architecture Decision Records

Format: Title, Status, Date, Context, Decision, Consequences, Alternatives. Statuses: **Proposed** (needs owner approval), **Accepted** (reflects existing practice/owner direction), **Blocked**, **Superseded**. New ADRs take the next number; do not rewrite accepted ones, supersede them.

| # | Title | Status |
|---|---|---|
| [0001](0001-native-clients-over-cross-platform-ui.md) | Native iOS and Android; no cross-platform UI | Accepted |
| [0002](0002-shared-contracts-not-ui.md) | Share contracts and domain rules, not UI | Accepted |
| [0003](0003-composition-root-not-configured-fail-closed.md) | CompositionRoot + NotConfigured fail-closed adapters | Accepted |
| [0004](0004-identity-blocked.md) | Identity blocked pending official IdP, legal, threat model | Accepted (Blocked) |
| [0005](0005-docs-first.md) | Docs-first restart | Accepted |
| [0006](0006-backend-target-gcp.md) | Backend target: Cloud Run, Firestore, Vertex AI, KMS | Proposed (target, not deployed) |
| [0007](0007-synthetic-fixtures-only.md) | Synthetic fixtures only | Accepted |
| [0008](0008-web-client-stack.md) | Web client: Next.js + React + TypeScript with BFF | Proposed |
| [0009](0009-web-session-and-token-handling.md) | Web session: BFF, httpOnly cookies, no browser tokens | Proposed |
