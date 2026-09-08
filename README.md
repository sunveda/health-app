# My Number Health & Wellness Portal

A native mobile monorepo for the iOS and Android clients of the My Number Health & Wellness Portal. The repository keeps the two applications separate so each platform can use its native security, health-data, NFC, and accessibility APIs while sharing product contracts and architecture decisions.

## Repository layout

| Path | Responsibility |
|---|---|
| `apps/ios` | Native SwiftUI iOS application and platform adapters |
| `apps/android` | Native Kotlin and Jetpack Compose Android application and platform adapters |
| `packages/contracts` | Platform-neutral JSON Schemas for API and domain boundaries |
| `packages/domain` | Platform-neutral synthetic medical-expense eligibility fixtures and tests |
| `packages/api` | Reserved for versioned backend transport contracts |
| `docs` | Architecture, security, privacy, and delivery decisions |

## Native application strategy

The iOS app is implemented with SwiftUI. Capability access (HealthKit, Keychain, Face ID or Touch ID, NFC) goes through `HealthApp/Core` adapters constructed by a single composition root. Feature screens must not import those frameworks.

The Android app is implemented with Kotlin and Jetpack Compose. Capability access (Health Connect, Android Keystore, BiometricPrompt, NFC) goes through `platform` adapters constructed by a single composition root. Feature screens must not import those APIs.

The applications share **contracts**, not UI code. This avoids a lowest-common-denominator user experience and allows each platform to follow its own permission, lifecycle, security, and accessibility conventions.

## Current status

The repository contains native application shells, a Core/platform composition root with NotConfigured adapters (including consent, crash reporting, and telemetry policy stubs), shared contract schemas, synthetic domain eligibility fixtures, and security/privacy baseline documentation. The My Number identity-provider integration, HealthKit and Health Connect permissions, NFC reader support, secure upload pipeline, backend services, and Vertex AI workflows are deliberately not wired to guessed endpoints or credentials. Those integrations require official provider documentation, app registration, compliance approval, and threat-model review.

## Working locally

### iOS

Open `apps/ios` in Xcode on macOS after adding the project configuration for the target bundle identifier and signing team. The current SwiftUI source is organized under `apps/ios/HealthApp` and is designed to be the first target implementation.

### Android

Open `apps/android` in Android Studio and sync the Gradle project. The current target uses Kotlin, Jetpack Compose, compile SDK 35, and a minimum SDK of 26.

## Security principles

The application follows data minimization, explicit consent, least privilege, zero-trust service boundaries, encrypted storage, and auditable access. Sensitive identity and health data must be excluded from logs and analytics by default. Any implementation of My Number Card authentication must use the official Japanese identity-provider documentation and a security review; the specification’s example scopes and flows are not treated as proof that a production endpoint or permission is available.

See [`docs/architecture.md`](docs/architecture.md), [`docs/security.md`](docs/security.md), [`docs/privacy-baseline.md`](docs/privacy-baseline.md), and [`docs/release-checklist.md`](docs/release-checklist.md) for the current design boundaries, open decisions, and TestFlight / Play internal-track prerequisites.
