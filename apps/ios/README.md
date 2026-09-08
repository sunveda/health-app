# Native iOS app

This directory contains the SwiftUI source for the native iOS application. The app uses a separate iOS codebase rather than sharing UI with Android.

## Project generation

The `project.yml` file is an XcodeGen manifest. On macOS with XcodeGen installed, generate the Xcode project with:

```bash
xcodegen generate
open HealthApp.xcodeproj
```

The bundle identifier is currently the placeholder `com.sunveda.healthapp`. The Apple Developer Team, signing settings, associated domains, URL schemes, HealthKit entitlements, and production identity-provider redirect configuration must be supplied later and are intentionally not committed.

## Native capability boundaries

HealthKit, Keychain (`import Security` / SecItem), LocalAuthentication, document providers, NFC, and the approved OIDC/PKCE flow must be implemented only under `HealthApp/Core` adapters constructed by `CompositionRoot`. Feature views consume `AppDependencies` and must not import those frameworks. Current stubs are NotConfigured / Unavailable and do not request permissions. `ConsentStore`, `CrashReporter`, and `TelemetryPolicy` follow the same pattern (crash reporting is not configured; no DSN).
