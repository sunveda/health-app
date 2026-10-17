# ADR-0007: Synthetic fixtures only

**Status:** Accepted · **Date:** 2026-10-17

## Context
Identity and health data are extremely sensitive; git history is permanent.

## Decision
All fixtures, examples, and tests use synthetic values (`SYNTH-` ids, `Fixture …` payees). No real My Number, patient data, clinic names, documents, tokens, or keys in git. CI rejects PEM headers, token-like assignments, and known token prefixes.

## Consequences
- Examples in docs use obviously fake values.
- Real-data testing, if ever needed, happens outside the repo under a separate approved process.

## Alternatives
Anonymized real data: rejected (re-identification risk).
