# Shared contracts

This package stores platform-neutral schemas and examples for the iOS and Android apps. The schemas describe API boundaries and user-visible states; they are not a replacement for server-side authorization or validation.

The current contract intentionally uses a pairwise subject identifier and avoids exposing the individual number. Native clients may generate platform-specific models from these schemas, but each client must still validate server responses and handle unknown enum values safely.

Contract changes should include compatibility notes and fixtures using synthetic values only. Changes that add a sensitive field require a data-classification review and an update to `docs/security.md` before they are merged.
