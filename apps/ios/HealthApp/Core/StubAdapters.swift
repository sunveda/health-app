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
}

struct UnavailableHealthDataSource: HealthDataSource {
    let status = CapabilityStatus.unavailable
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
