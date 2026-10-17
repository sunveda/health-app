# ADR-0011: Hosting — Vercel (web + BFF) with Vercel-managed Postgres (Neon)

**Status:** Accepted (owner, 2026-10-17). **Real health data is not permitted on the platform until the legal/compliance gates in [hosting-and-compliance](../architecture/hosting-and-compliance.md) are cleared.** Supersedes the web/BFF hosting, database and storage parts of [ADR-0006](0006-backend-target-gcp.md). · **Date:** 2026-10-17

## Context

Owner decision: host on Vercel with Vercel's managed database (Postgres via the Neon marketplace integration) and keep cost minimal. Health data is special care-required personal information under Japan's APPI (legal to confirm). The verified platform facts, limits and risks are in [hosting-and-compliance](../architecture/hosting-and-compliance.md); the headline facts:

- Vercel Functions default to `iad1` (Washington, D.C.); Tokyo is `hnd1` and must be set explicitly.
- Neon's documented region list has **no Tokyo region**; nearest is Singapore `aws-ap-southeast-1`. Region cannot be changed after creation. Database traffic from `hnd1` functions to Singapore is cross-border.
- Neon Free: 6-hour restore window, scale-to-zero, no SLA; Vercel Hobby is non-commercial only and keeps runtime logs for 1 hour.

## Decision

- **Compute/BFF/API:** Next.js on Vercel Functions, project region pinned to `hnd1` in project config.
- **Database:** Vercel-managed Postgres = Neon via the Vercel integration, region `aws-ap-southeast-1` unless Neon adds Tokyo (check the Neon active-regions API before creating; **UNVERIFIED for the owner's org**). Separate Neon projects for production and for non-production; **preview/dev use synthetic data only** ([ADR-0007](0007-synthetic-fixtures-only.md)), never a branch or copy of production.
- **Clinical files:** private object storage only, accessed through short-lived signed URLs. Candidate: Vercel Blob private stores (documented as supporting private access and signed URLs; region availability for Tokyo and compliance terms **UNVERIFIED**). Choice is provisional pending the storage review in the compliance doc.
- **Encryption:** provider at-rest encryption is not enough for the most sensitive fields; use application-level envelope encryption (design in [data-model §8](../architecture/data-model.md)). Master-key custody is an open item (below).
- **Plan gating:** Free/Hobby tiers for synthetic-data development only. Anything with real user data requires paid plans that meet the SLA, backup, log and contract needs, plus legal sign-off.
- **Native clients** call the same BFF/API tier; they do not talk to the database or storage directly.

## Consequences

- Cloud Run, Firestore, Cloud KMS/CMEK, VPC-SC and Cloud Armor from ADR-0006 are dropped as target; equivalents (WAF, rate limits, secrets, key custody) must be re-specified on Vercel/Neon.
- Cross-border transfer (Tokyo functions, Singapore database, global edge network), sub-processors (Vercel, Neon/Databricks, AWS, storage and email vendors) and data residency are legal review items.
- Vercel serverless functions plus Neon connection pooling need an explicit connection strategy (TBD at implementation).
- Vendor lock-in is moderate: Postgres and Next.js are portable; Blob and Vercel-specific config are not.
- HIPAA BAAs offered by Vercel/Neon are a US regime; they are **not** Japanese law and are not a compliance claim here.

## Alternatives

Stay on GCP (Cloud Run + Cloud SQL/Firestore, Tokyo region available but cost and ops burden higher; owner chose otherwise); AWS Tokyo with RDS (not evaluated); Supabase or other Postgres hosts (not evaluated; a Tokyo-region host would remove the cross-border item and is worth evaluating before real data).
