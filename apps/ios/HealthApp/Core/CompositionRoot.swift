import Foundation

/// Typed dependencies handed to feature modules. Screens must not construct platform adapters.
struct AppDependencies {
    let secureStore: SecureStore
    let biometricUnlock: BiometricUnlock
    let healthDataSource: HealthDataSource
    let nfcCapability: NfcCapability
    let identitySession: IdentitySession
}

/// Single composition root. The only type allowed to construct platform adapter implementations.
enum CompositionRoot {
    static func make() -> AppDependencies {
        AppDependencies(
            secureStore: NotConfiguredSecureStore(),
            biometricUnlock: NotConfiguredBiometricUnlock(),
            healthDataSource: NotConfiguredHealthDataSource(),
            nfcCapability: NotConfiguredNfcCapability(),
            identitySession: NotConfiguredIdentitySession()
        )
    }
}
