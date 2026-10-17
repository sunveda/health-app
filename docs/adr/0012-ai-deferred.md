# ADR-0012: Defer AI insights (Vertex AI) to post-MVP; revisit only if free

**Status:** Accepted (owner, 2026-10-17) · **Date:** 2026-10-17

## Context

SPEC open question 3 asked whether Vertex AI is in scope for v1. The owner's answer: include AI in v1 only if it is free, otherwise defer. [ADR-0006](0006-backend-target-gcp.md) (Vertex AI on GCP) is superseded by the Vercel + Neon hosting decision ([ADR-0011](0011-hosting-vercel-neon.md)); Vertex AI would add a second cloud, a new sub-processor, cross-border transfer of health text, and per-call cost (pricing not verified here).

## Decision

**AI is deferred to post-MVP** (S10 status: Deferred). No AI endpoint, SDK, or UI ships in the MVP. Revisit only if a provider/model is **free at the volume needed**, its terms allow health data with no training/retention, and legal approves the data flow. The AI boundary design (grounding, provenance, disclaimers, prompt-injection isolation) stays in the docs as a future constraint, and any future provider is chosen by a new ADR; Vertex AI is not pre-selected.

## Consequences

- Clinical documents in the MVP get validation, quarantine, user review and storage only; no extraction or summarization.
- The `AI jobs service` and AI contract rows in api-contracts are marked deferred; threat-model §2.7 applies only when reopened.
- Fewer sub-processors and less cross-border exposure in the MVP.

## Alternatives

Ship AI with a paid provider (rejected: cost and compliance); on-device models (not evaluated).
