# My Number Health & Wellness Portal

A client monorepo for the iOS, Android, and web surfaces of the My Number Health & Wellness Portal. The repository keeps each client separate so platforms can use their own security, health-data, NFC (mobile), and accessibility APIs while sharing product contracts and architecture decisions.

## Agent / handoff context

**Start here:**

1. [docs/STACK.md](docs/STACK.md) — **one-screen stack** (every technology and boundary)
2. [docs/SPEC.md](docs/SPEC.md) — specification index (docs-first writing order)
3. [docs/CONTEXT.md](docs/CONTEXT.md) — living status for any agent or human

Update CONTEXT **daily while this repo is active**, and on every major direction change (SunVeda rule for all projects). Also: [AGENTS.md](AGENTS.md)

## Repository layout

| Path | Responsibility |
|---|---|
| `apps/ios` | Native SwiftUI iOS application and platform adapters |
| `apps/android` | Native Kotlin and Jetpack Compose Android application and platform adapters |
| `apps/web` | React + Vite + TypeScript web shell and platform adapters |
| `packages/contracts` | Platform-neutral JSON Schemas for API and domain boundaries |
| `packages/domain` | Platform-neutral synthetic medical-expense eligibility fixtures and tests |
| `packages/api` | Reserved for versioned backend transport contracts |
| `docs` | Architecture, security, privacy, and delivery decisions |

## Native application strategy

The iOS app is implemented with SwiftUI. Capability access (HealthKit, Keychain, Face ID or Touch ID, NFC) goes through `HealthApp/Core` adapters constructed by a single composition root. Feature screens must not import those frameworks.

The Android app is implemented with Kotlin and Jetpack Compose. Capability access (Health Connect, Android Keystore, BiometricPrompt, NFC) goes through `platform` adapters constructed by a single composition root. Feature screens must not import those APIs.

The clients share **contracts**, not UI code. This avoids a lowest-common-denominator user experience and allows each platform to follow its own permission, lifecycle, security, and accessibility conventions. The web client does not emulate HealthKit, Health Connect, or NFC; see [`docs/web-baseline.md`](docs/web-baseline.md).

## Current status

See [docs/CONTEXT.md](docs/CONTEXT.md) for living handoff detail (blockers, owners).

The repository contains native application shells, a Core/platform composition root with NotConfigured adapters (including consent, crash reporting, telemetry policy, wellness sync, and clinical upload/review stubs), shared contract schemas, synthetic domain eligibility fixtures, and security/privacy baseline documentation. The My Number identity-provider integration, HealthKit and Health Connect permissions, NFC reader support, secure upload pipeline, backend services, and Vertex AI workflows are deliberately not wired to guessed endpoints or credentials. Those integrations require official provider documentation, app registration, compliance approval, and threat-model review.

As of **2026-09-29**, the active track is **docs & specification** ([STACK.md](docs/STACK.md), [SPEC.md](docs/SPEC.md)) — not feature implementation. Specialist bot: **Health App**. Coordinate via Chief of Staff / Chief of Engineering.

## Docs

- [**STACK (one-screen tech map)**](docs/STACK.md)
- [**SPEC (specification index)**](docs/SPEC.md)
- [**CONTEXT (living handoff)**](docs/CONTEXT.md)
- [`docs/web-baseline.md`](docs/web-baseline.md)
- [`docs/architecture.md`](docs/architecture.md)
- [`docs/security.md`](docs/security.md)
- [`docs/privacy-baseline.md`](docs/privacy-baseline.md)
- [`docs/wellness-baseline.md`](docs/wellness-baseline.md)
- [`docs/clinical-baseline.md`](docs/clinical-baseline.md)
- [`docs/release-checklist.md`](docs/release-checklist.md)

## Working locally

### iOS

Open `apps/ios` in Xcode on macOS after adding the project configuration for the target bundle identifier and signing team. The current SwiftUI source is organized under `apps/ios/HealthApp` and is designed to be the first target implementation.

### Android

Open `apps/android` in Android Studio and sync the Gradle project. The current target uses Kotlin, Jetpack Compose, compile SDK 35, and a minimum SDK of 26.

### Web

From the repo root (pnpm workspace):

```bash
pnpm install
pnpm --filter @health-app/web dev
```

See [`apps/web/README.md`](apps/web/README.md).

## Security principles

The application follows data minimization, explicit consent, least privilege, zero-trust service boundaries, encrypted storage, and auditable access. Sensitive identity and health data must be excluded from logs and analytics by default. Any implementation of My Number Card authentication must use the official Japanese identity-provider documentation and a security review; the specification’s example scopes and flows are not treated as proof that a production endpoint or permission is available.
