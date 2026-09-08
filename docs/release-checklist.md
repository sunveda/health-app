# Release-path checklist

Prerequisites for TestFlight and Play internal testing. This is a delivery checklist, not a substitute for legal, privacy, or identity-provider review.

Account emails, Apple Team IDs, Play developer account names, and signing-certificate fingerprints are **TBD with the product owner** and must not be invented or committed here.

Related: [`architecture.md`](architecture.md) (signing placeholders), [`security.md`](security.md), [`privacy-baseline.md`](privacy-baseline.md), [`wellness-baseline.md`](wellness-baseline.md).

## Bundle / application ID freeze

Keep both clients on **`com.sunveda.healthapp`** until a coordinated rename is approved:

| Client | Setting | Current value |
|---|---|---|
| iOS | `PRODUCT_BUNDLE_IDENTIFIER` in `apps/ios/project.yml` | `com.sunveda.healthapp` |
| iOS tests | `PRODUCT_BUNDLE_IDENTIFIER` | `com.sunveda.healthapp.tests` |
| Android | `applicationId` / `namespace` in `apps/android/app/build.gradle.kts` | `com.sunveda.healthapp` |

Changing the identifier after TestFlight or Play internal enrollment orphans the listing. Do not ship a different id “just for staging.”

## Secrets and signing material

Do **not** commit certificates, provisioning profiles, upload keystores, API keys, crash DSNs, or filled `.env` files. Native CI builds iOS with `CODE_SIGNING_ALLOWED=NO`. Inject signing and runtime values from a managed secret store at build time. See `.gitignore` and `.env.example`.

## TestFlight (iOS)

Complete before uploading a build. Ownership of each Apple account is TBD with the product owner.

- [ ] Apple Developer Program membership for the shipping organization (owner TBD)
- [ ] App Store Connect app record whose bundle ID is `com.sunveda.healthapp`
- [ ] Signing: development + distribution certificates and an app App Store / ad hoc profile, held outside this repository
- [ ] Xcode team and signing settings supplied locally or via CI secrets — not in `project.yml`
- [ ] TestFlight internal group created; tester invitations sent by the account owner
- [ ] Privacy Nutrition labels and App Privacy details drafted with product/legal (copy is not in this repo yet)
- [ ] Export compliance, encryption, and health-data questionnaire answers reviewed — do not guess
- [ ] Identity, HealthKit, NFC, and associated-domain entitlements stay **off** until those stages are approved (current stubs are NotConfigured)

CI on `macos-14` runs unit tests on a resolved iPhone simulator (prefers iPhone 15, then any available iPhone). That is not a signed TestFlight build.

## Play internal track (Android)

Complete before promoting an App Bundle. Play Console ownership is TBD with the product owner.

- [ ] Google Play Console developer account for the shipping organization (owner TBD)
- [ ] Application created with application id `com.sunveda.healthapp`
- [ ] Play App Signing enrolled; upload keystore stored in a secret manager, never in Git
- [ ] Internal testing track created; testers added by the account owner
- [ ] `assembleDebug` / unit tests in Native CI are not a substitute for a signed AAB
- [ ] Data safety form drafted with product/legal; Health Connect and identity scopes stay undeclared until those stages
- [ ] Release signing config remains out of `build.gradle.kts` until secrets exist

## Native CI (known iOS simulator risk)

GitHub-hosted `macos-14` images have drifted between Xcode 15 runtimes (`iPhone 15`) and later images that may omit that exact device name, or advertise several OS versions under the same name. A hardcoded `-destination 'platform=iOS Simulator,name=iPhone 15'` can fail destination matching even when simulators exist.

**Mitigation (does not weaken gates):** `scripts/ios_simulator_destination.py` resolves a **concrete** available iPhone (prefer `iPhone 15`, else another available iPhone) and prints `platform=iOS Simulator,id=<udid>`. `xcodebuild test` still runs. There is no fallback to `generic/platform=iOS Simulator`, which cannot execute tests.

If the resolver exits non-zero, the iOS job must fail. Do not skip tests to paper over a missing runtime.

## Still blocked (do not treat this checklist as unblocking)

- Official IdP / My Number registration, OIDC/PKCE, and guessed issuer URLs
- HealthKit / Health Connect / NFC / biometrics / Keychain production wiring
- Backend clients, crash DSNs, and AI
- App Store / Play production submission and legal notices
