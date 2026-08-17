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

HealthKit, Keychain, LocalAuthentication, document providers, NFC, and the approved OIDC/PKCE flow should be implemented under `HealthApp/Core` or feature-specific adapters. The UI should consume typed capability results and must not assume that a permission exists or that a device supports a capability.
