# ADR-0001: Native iOS and Android; no cross-platform UI

**Status:** Accepted · **Date:** 2026-10-17 (records existing decision)

## Context
The portal needs HealthKit / Health Connect, Keychain / Keystore, biometrics, NFC, and per-platform accessibility. An earlier Expo scaffold was removed; CI still rejects the Expo public-env prefix.

## Decision
SwiftUI (iOS 17+, Swift 5.9) and Kotlin + Jetpack Compose (minSdk 26) as separate apps in one monorepo. No React Native, Expo, or Flutter for the mobile clients.

## Consequences
- Best access to native security and health APIs; platform-native UX.
- Two implementations of each feature; parity enforced by shared contracts and tests.
- Web is a third client (ADR-0008); it is a web app, not a mobile UI-sharing layer.

## Alternatives
React Native/Expo, Flutter: rejected due to health/NFC/secure-storage bridging risk and lowest-common-denominator UX.
