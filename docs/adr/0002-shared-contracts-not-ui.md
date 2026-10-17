# ADR-0002: Share contracts and domain rules, not UI

**Status:** Accepted · **Date:** 2026-10-17

## Context
Three clients must agree on data shapes and business rules without sharing UI.

## Decision
`packages/contracts` (JSON Schema 2020-12) is the source of truth for wire/state shapes. `packages/domain` holds executable specification and synthetic fixtures (currently Python). Clients port rules and validate against fixtures; no client imports another client's UI.

## Consequences
- Schema changes need classification review and compatibility notes.
- Domain rules are specified once, ported per platform (Swift, Kotlin, TypeScript); fixtures keep ports honest.
- Tooling to generate/validate types is PLANNED.

## Alternatives
Shared Kotlin Multiplatform or TS core for logic: deferred; revisit once rules exist beyond the placeholder engine.
