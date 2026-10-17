# ADR-0006: Backend target — Cloud Run, Firestore, Vertex AI, KMS

**Status:** Superseded in part by [ADR-0011](0011-hosting-vercel-neon.md) (web/BFF hosting, database, object storage) and [ADR-0012](0012-ai-deferred.md) (Vertex AI deferred) · **Date:** 2026-10-17

> **Amended 2026-10-17.** Owner chose Vercel + Vercel-managed Postgres (Neon) for the web/BFF/data tier. Cloud Run, Firestore, Cloud KMS/CMEK, VPC Service Controls and Cloud Armor are **no longer the target** and nothing was deployed. Retained from this ADR: split services by purpose and per-service authorization, separate environments, no project IDs/regions/URLs in client code. Native clients will call the same BFF/API tier. The text below is kept for history.

## Context
Services needed for identity, health data, clinical documents, expenses, AI jobs, exports. The supplied specification proposes Google Cloud.

## Decision (proposed)
Cloud Run services split by purpose; Firestore for metadata; Vertex AI for AI jobs; Cloud KMS with CMEK; VPC Service Controls and Cloud Armor; managed secrets; separate projects and service accounts per environment. No project IDs, regions or URLs are defined.

## Consequences
- Per-service authorization and least privilege.
- Object storage for clinical files and the scanner vendor are open items.
- Region/data-residency choice needs legal input (Japan context).
- Vertex AI in v1 vs later is an owner question (SPEC open question 3).

## Alternatives
AWS/Azure, a single monolith, or a BaaS: not evaluated; revisit if owner prefers.
