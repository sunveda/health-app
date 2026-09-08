import Foundation

/// Application-protected storage versus storage that must only be readable after a biometric challenge.
/// Real Keychain access is not performed here; feature code must use this protocol only.
protocol SecureStore {
    var applicationStoreStatus: CapabilityStatus { get }
    var biometricGatedStoreStatus: CapabilityStatus { get }

    func readApplicationValue(forKey key: String) -> Result<Data?, CapabilityError>
    func writeApplicationValue(_ value: Data, forKey key: String) -> Result<Void, CapabilityError>
    func deleteApplicationValue(forKey key: String) -> Result<Void, CapabilityError>

    func readBiometricGatedValue(forKey key: String) -> Result<Data?, CapabilityError>
    func writeBiometricGatedValue(_ value: Data, forKey key: String) -> Result<Void, CapabilityError>
    func deleteBiometricGatedValue(forKey key: String) -> Result<Void, CapabilityError>
}

protocol BiometricUnlock {
    var status: CapabilityStatus { get }
    func unlock() -> Result<Void, CapabilityError>
}

protocol HealthDataSource {
    var status: CapabilityStatus { get }
}

protocol NfcCapability {
    var status: CapabilityStatus { get }
}

/// Official identity-provider session. Stub only: no OIDC issuer, client ID, or tokens.
protocol IdentitySession {
    var status: CapabilityStatus { get }
    func pairwiseSubject() -> String?
}
