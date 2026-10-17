# Wellness baseline (device-health stubs)

This note is the Wellness capability record for the native clients. It describes intended later read scopes, that real HealthKit / Health Connect wiring is deferred, and the feature-flag approach. It does **not** complete Wellness, Identity, Clinical, or legal review.

Related: [`architecture.md`](architecture.md), [`privacy-baseline.md`](privacy-baseline.md), [`security.md`](security.md).

## Status of this slice

CompositionRoot wires `HealthDataSource` and `WellnessSyncClient` as **NotConfigured** (with matching Unavailable stubs for tests). Feature modules read availability, permission state, read scope, and failure state from those adapters. They must not import or call device health kits.

| Item | This slice | Not in this slice |
|---|---|---|
| Adapter surface | Availability, permission state, granted read scopes, last failure, sync client | Live `HKHealthStore` / `HealthConnectClient` |
| Feature flag | Shipping default **disabled** | Enabling the flag in production |
| Permission prompts | None; `requestReadAccess` fails with not configured / unavailable | System permission sheets |
| Samples | No reads; no synthetic personal health fixtures | Quantity samples, workouts, background delivery |
| Entitlements / manifest | HealthKit and Health Connect declarations stay off | Info.plist usage descriptions, Health Connect permissions |

Identity remains blocked on IdP / legal review. This slice does not unblock OIDC, NFC, biometrics, Keychain/Keystore, backend clients, or Clinical upload.

## Feature flag

`WellnessFeatureFlag` is part of `WellnessSyncClient`. CompositionRoot currently supplies `.disabled`. Screens should treat a disabled flag plus NotConfigured status as the “Not configured” user-visible path.

When kit adapters are reviewed later, only CompositionRoot may construct them, and only then may the flag flip to `.enabled`. Feature modules must not branch on compile-time kit imports.

## Intended later read scopes

Engineering identifiers used by the stubs: `steps`, `heartRate`. These are **not** requested today. Granted scope sets on the stubs are always empty.

If a later slice adds real reads, start with the smallest publicly documented platform read types that match those identifiers, and only after purpose-tied UX and privacy review:

| Engineering scope | Public iOS type (future) | Public Android permission (future) |
|---|---|---|
| `steps` | HealthKit step count (`HKQuantityTypeIdentifierStepCount`) | `android.permission.health.READ_STEPS` |
| `heartRate` | HealthKit heart rate (`HKQuantityTypeIdentifierHeartRate`) | `android.permission.health.READ_HEART_RATE` |

Do not add write scopes, background delivery, or a “full dump” read by default. Additional types (sleep, energy, workouts, and so on) need their own purpose, minimization review, and tests. Do not invent product-specific Apple or Google permission strings.

Future iOS usage descriptions, when entitlements are actually added, are the standard keys `NSHealthShareUsageDescription` and `NSHealthUpdateUsageDescription`. They are not present in the current Info.plist. Future Android Health Connect permission declarations belong in the app manifest only when that slice is approved. Neither is claimed done here.

## Permission minimization (exit criteria for the real Wellness stage)

When kit wiring is implemented, exit criteria are:

1. Request only the read scopes required for a named purpose (`purpose.health.measurements-display` today).
2. Explain the purpose in-app before the system prompt; honor denial, restriction, and not-determined states.
3. Keep adapters behind CompositionRoot; feature screens never import HealthKit or Health Connect.
4. Keep the feature flag off in shipping builds until those adapters pass platform tests.
5. Log event names only; never log raw samples (see privacy baseline).

This stub slice records those criteria. It does not satisfy them by pretending the kits are connected.

## Contract mapping

`packages/contracts` `wellnessSync.status` values remain `not_connected`, `connected`, `permission_denied`, and `error`. Pure mappers translate adapter snapshots to that contract:

- Not configured → `not_connected`
- Permission denied → `permission_denied`
- Unavailable (or an unavailable failure) → `error`
- Ready, authorized, and a last-synced timestamp → `connected`
- Otherwise → `not_connected`

Stubs therefore map to `not_connected`. No backend sync client is implied.

## UI

Health and Settings shells read adapter status (and the disabled feature flag) and show “Not configured”. Tab structure is unchanged. Do not infer kit availability from device APIs.
