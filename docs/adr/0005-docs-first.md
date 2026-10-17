# ADR-0005: Docs-first restart

**Status:** Accepted · **Date:** 2026-09-29 (owner direction, #23)

## Context
Scope is large and partly blocked; the owner wants specifications understood before more code.

## Decision
Specification and documentation precede implementation. No feature coding until the matching SPEC.md row is owner-approved. Allowed: docs, CONTEXT updates, trivial link edits, owner-requested security fixes. CONTEXT.md is updated per active workday.

## Consequences
- Slower visible progress, fewer invented APIs.
- Specs in this repo may describe PLANNED items; every doc marks built vs stub vs planned.

## Alternatives
Continue stub-driven feature slices: paused.
