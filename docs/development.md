# Development and test guide

**Last updated:** 2026-10-17. Commands below are the ones used by `.github/workflows/native-ci.yml` or documented in the repo READMEs. Web items are **PLANNED** (`apps/web` does not exist yet).

Docs-first mode applies ([ADR-0005](adr/0005-docs-first.md)): no feature coding until the SPEC.md row is owner-approved.

## 1. Prerequisites (from the actual toolchain)

| Area | Requirement |
|---|---|
| Python | Python 3 (CI uses `python3` on ubuntu-latest); stdlib only, no installs |
| iOS | macOS, Xcode 15+, [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`); iOS 17.0 deployment target, Swift 5.9 |
| Android | JDK 17 (Temurin in CI), Android SDK (compile/target 35, min 26), Gradle 8.10, AGP 8.7.3, Kotlin 2.0.21 |
| Workspace | pnpm 10.12.1 (`packageManager`), Node for workspace tooling (Prettier ^3.6, TypeScript ^5.9 dev deps) |
| Web (PLANNED) | Node LTS + pnpm; framework per [ADR-0008](adr/0008-web-client-stack.md) |

Do not commit secrets, signing material, or filled `.env`. Use `.env.example` names only (`HEALTH_APP_API_BASE_URL`, `HEALTH_APP_OIDC_ISSUER`, `HEALTH_APP_OIDC_CLIENT_ID`, `HEALTH_APP_OIDC_REDIRECT_URI`).

## 2. Build and test commands

### Repository checks (no network, runs anywhere with Python 3)

```bash
# contracts/domain JSON parse + whitespace check (as in CI)
python3 - <<'PY'
import json
from pathlib import Path
for d in ("packages/contracts", "packages/domain"):
    for p in Path(d).rglob("*.json"):
        json.loads(p.read_text(encoding="utf-8")); print("Validated", p)
PY
git diff --check

# forbidden-pattern gate
python3 scripts/check-forbidden-patterns.py

# domain eligibility tests
python3 -m unittest discover -s packages/domain -t packages/domain -p 'test_*.py'

# iOS simulator destination resolver tests
python3 -m unittest discover -s scripts -t scripts -p 'test_ios_simulator_destination.py'
```

### Android

```bash
cd apps/android
gradle --no-daemon test assembleDebug
```

(CI uses Gradle 8.10; no wrapper is checked in. Android Studio sync also works.)

### iOS (macOS)

```bash
cd apps/ios
xcodegen generate --spec project.yml
# CI rewrites objectVersion 77 -> 56 on macos-14 / Xcode 15:
#   sed -i '' 's/objectVersion = 77;/objectVersion = 56;/' HealthApp.xcodeproj/project.pbxproj
DESTINATION="$(python3 ../../scripts/ios_simulator_destination.py)"
xcodebuild -project HealthApp.xcodeproj -scheme HealthApp -sdk iphonesimulator \
  -destination "${DESTINATION}" CODE_SIGNING_ALLOWED=NO test
```

Never use `generic/platform=iOS Simulator` for tests; it cannot run them.

### Workspace scripts

`pnpm lint`, `pnpm typecheck`, `pnpm test`, `pnpm format:check` exist in the root `package.json` and run `pnpm -r` across packages. No workspace package currently defines these scripts, so they are effectively no-ops; they are not part of CI.

### Web (PLANNED)

Install, typecheck, lint, unit test, build, and E2E jobs to be added to CI with `apps/web`. Until then none exist; do not document commands that do not exist.

## 3. CI jobs

`Native CI` runs on pull requests and pushes to `main`: **contracts** job (JSON validate, whitespace, forbidden patterns, domain tests, simulator resolver tests) → **android** and **ios** jobs (both `needs: contracts`). Merge gate used so far: all three green.

## 4. Forbidden-pattern gates

`scripts/check-forbidden-patterns.py` scans tracked files and fails on:

- `EXPO_PUBLIC_`, PEM private-key headers, obvious secret assignments and token prefixes (all files)
- HealthKit, CoreNFC, LocalAuthentication, Security/Keychain, Health Connect, BiometricPrompt, Android Keystore, Android NFC, iOS/Android document/photo pickers, AWS/GCP/Firebase storage SDKs, hardcoded object-storage URLs — **outside** `apps/ios/HealthApp/Core/` and `apps/android/.../platform/`
- `NotConfigured*` / `Unavailable*` / `InMemory*` constructors outside the CompositionRoot files (unit tests exempt)

Gaps: nothing covers web yet (see [threat-model §3](security/threat-model.md)); the checks are regex based.

## 5. How to add a feature behind the CompositionRoot

1. Confirm the SPEC.md row is **Owner-approved**; Identity-related work is **Blocked**.
2. Write/extend the spec and data classification first (new sensitive field → classification review + `docs/security.md` update).
3. Define or extend the adapter **interface** in `Core/PlatformAdapters.swift` and `platform/PlatformAdapters.kt` (and the web `core/` once it exists). Expose status via `CapabilityStatus`; return `CapabilityError` / `CapabilityOutcome` instead of throwing past the boundary.
4. Add `NotConfigured` and `Unavailable` implementations in `StubAdapters` and keep them fail-closed.
5. Add the field to `AppDependencies` and construct it **only** in `CompositionRoot`.
6. Consume it from the feature layer via dependencies; never import kits, pickers or SDKs there.
7. Add unit tests (not-configured, unavailable, and for real adapters: denied and ready) on both platforms, mirroring `PlatformAdapterTest.kt` / `HealthAppTests.swift`.
8. Real adapters (kit access) live only in adapter paths, behind a feature flag defaulting off, and need CoE review before the flag flips.
9. Update contracts + synthetic fixtures if the wire shape changes; update the baseline doc and `CONTEXT.md`.

## 6. Fixtures and data

Synthetic only (`SYNTH-` ids, `Fixture …` labels). Never paste real documents, identifiers, or screenshots into the repo, PRs, or issues ([ADR-0007](adr/0007-synthetic-fixtures-only.md)).

## 7. PR workflow

- Branch from `main`, small PRs, descriptive titles; draft until CI is green.
- Required: CI green (contracts, Android, iOS), no secrets, data-classification note for new fields, statement on whether a new native permission is introduced, dependency review.
- Standards review by Chief of Engineering; product ownership by the Health App bot; owner approves specs.
- Do not merge or close PRs unless instructed; Dependabot PRs are handled separately.
- After meaningful work, add a dated entry to `docs/CONTEXT.md`.

## 8. Docs conventions

Mark every statement **BUILT / STUB / PLANNED / BLOCKED**. Mermaid diagrams must render on GitHub; validate with `npx @mermaid-js/mermaid-cli` when changed. Never invent endpoints, scopes, client IDs, or vendors.
