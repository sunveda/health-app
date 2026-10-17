# ADR-0003: CompositionRoot + NotConfigured fail-closed adapters

**Status:** Accepted · **Date:** 2026-10-17

## Context
Sensitive capabilities (health kits, secure storage, biometrics, NFC, upload) must not leak into feature code or silently succeed before review.

## Decision
Every capability is an interface in `Core/` (iOS) or `platform/` (Android) with `NotConfigured` and `Unavailable` implementations. A single `CompositionRoot` constructs them and hands typed `AppDependencies` to screens. `CapabilityStatus` is rendered by the UI. `scripts/check-forbidden-patterns.py` fails CI on kit/picker/upload SDK use outside adapter paths and on adapter constructors outside the root (unit tests exempt). The web client adopts the same pattern (ADR-0008).

## Consequences
- Real adapters are added one at a time, reviewed, only inside adapter paths.
- Shipping default is visible "Not configured".
- The gate is regex-based; it is a backstop, not a substitute for review. Web patterns must be added before web code lands.

## Alternatives
DI frameworks or ad-hoc singletons: rejected for weaker enforcement.
