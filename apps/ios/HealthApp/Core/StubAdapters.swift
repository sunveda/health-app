import Foundation

struct NotConfiguredSecureStore: SecureStore {
    let applicationStoreStatus = CapabilityStatus.notConfigured
    let biometricGatedStoreStatus = CapabilityStatus.notConfigured

    func readApplicationValue(forKey _: String) -> Result<Data?, CapabilityError> {
        .failure(.notConfigured)
    }

    func writeApplicationValue(_: Data, forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }

    func deleteApplicationValue(forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }

    func readBiometricGatedValue(forKey _: String) -> Result<Data?, CapabilityError> {
        .failure(.notConfigured)
    }

    func writeBiometricGatedValue(_: Data, forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }

    func deleteBiometricGatedValue(forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }
}

struct UnavailableSecureStore: SecureStore {
    let applicationStoreStatus = CapabilityStatus.unavailable
    let biometricGatedStoreStatus = CapabilityStatus.unavailable

    func readApplicationValue(forKey _: String) -> Result<Data?, CapabilityError> {
        .failure(.unavailable)
    }

    func writeApplicationValue(_: Data, forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }

    func deleteApplicationValue(forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }

    func readBiometricGatedValue(forKey _: String) -> Result<Data?, CapabilityError> {
        .failure(.unavailable)
    }

    func writeBiometricGatedValue(_: Data, forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }

    func deleteBiometricGatedValue(forKey _: String) -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }
}

struct NotConfiguredBiometricUnlock: BiometricUnlock {
    let status = CapabilityStatus.notConfigured

    func unlock() -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }
}

struct UnavailableBiometricUnlock: BiometricUnlock {
    let status = CapabilityStatus.unavailable

    func unlock() -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }
}

struct NotConfiguredHealthDataSource: HealthDataSource {
    let status = CapabilityStatus.notConfigured
    let permissionState = HealthPermissionState.notConfigured
    let grantedReadScopes: Set<WellnessReadScope> = []
    let lastFailure: CapabilityError? = .notConfigured

    func requestReadAccess(scopes _: Set<WellnessReadScope>) -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }
}

struct UnavailableHealthDataSource: HealthDataSource {
    let status = CapabilityStatus.unavailable
    let permissionState = HealthPermissionState.unavailable
    let grantedReadScopes: Set<WellnessReadScope> = []
    let lastFailure: CapabilityError? = .unavailable

    func requestReadAccess(scopes _: Set<WellnessReadScope>) -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }
}

struct NotConfiguredWellnessSyncClient: WellnessSyncClient {
    let featureFlag = WellnessFeatureFlag.disabled
    let status = CapabilityStatus.notConfigured
    let lastSyncedAt: Date? = nil

    func sync() -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }
}

struct UnavailableWellnessSyncClient: WellnessSyncClient {
    let featureFlag = WellnessFeatureFlag.disabled
    let status = CapabilityStatus.unavailable
    let lastSyncedAt: Date? = nil

    func sync() -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }
}

struct NotConfiguredNfcCapability: NfcCapability {
    let status = CapabilityStatus.notConfigured
}

struct UnavailableNfcCapability: NfcCapability {
    let status = CapabilityStatus.unavailable
}

struct NotConfiguredIdentitySession: IdentitySession {
    let status = CapabilityStatus.notConfigured

    func pairwiseSubject() -> String? {
        nil
    }
}

struct UnavailableIdentitySession: IdentitySession {
    let status = CapabilityStatus.unavailable

    func pairwiseSubject() -> String? {
        nil
    }
}

struct NotConfiguredConsentStore: ConsentStore {
    let status = CapabilityStatus.notConfigured

    func decision(for _: DataCategory) -> ConsentDecision {
        .notRecorded
    }

    func record(decision _: ConsentDecision, for _: DataCategory, purposeId _: String) -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }
}

struct UnavailableConsentStore: ConsentStore {
    let status = CapabilityStatus.unavailable

    func decision(for _: DataCategory) -> ConsentDecision {
        .notRecorded
    }

    func record(decision _: ConsentDecision, for _: DataCategory, purposeId _: String) -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }
}

/// Test/scaffolding store: category + decision only, no disk, no sensitive payloads.
/// Not wired by CompositionRoot.
final class InMemoryConsentStore: ConsentStore {
    let status = CapabilityStatus.ready
    private var decisions: [DataCategory: ConsentDecision] = [:]

    func decision(for category: DataCategory) -> ConsentDecision {
        decisions[category] ?? .notRecorded
    }

    func record(decision: ConsentDecision, for category: DataCategory, purposeId: String) -> Result<Void, CapabilityError> {
        guard decision != .notRecorded else {
            decisions.removeValue(forKey: category)
            return .success(())
        }
        guard PurposeRegistry.definition(id: purposeId)?.category == category else {
            return .failure(.unavailable)
        }
        decisions[category] = decision
        return .success(())
    }
}

struct NotConfiguredCrashReporter: CrashReporter {
    let status = CapabilityStatus.notConfigured

    func captureNonPII(event _: String) -> Result<Void, CapabilityError> {
        .failure(.notConfigured)
    }
}

struct UnavailableCrashReporter: CrashReporter {
    let status = CapabilityStatus.unavailable

    func captureNonPII(event _: String) -> Result<Void, CapabilityError> {
        .failure(.unavailable)
    }
}

struct NotConfiguredTelemetryPolicy: TelemetryPolicy {
    let status = CapabilityStatus.notConfigured

    func isAllowed(_: DataCategory) -> Bool {
        false
    }
}

struct UnavailableTelemetryPolicy: TelemetryPolicy {
    let status = CapabilityStatus.unavailable

    func isAllowed(_: DataCategory) -> Bool {
        false
    }
}
