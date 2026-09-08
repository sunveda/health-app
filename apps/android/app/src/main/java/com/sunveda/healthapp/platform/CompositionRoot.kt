package com.sunveda.healthapp.platform

/** Typed dependencies handed to feature modules. Screens must not construct platform adapters. */
data class PlatformDependencies(
    val secureStore: SecureStore,
    val biometricUnlock: BiometricUnlock,
    val healthDataSource: HealthDataSource,
    val nfcCapability: NfcCapability,
    val identitySession: IdentitySession,
)

/** Single composition root. The only type allowed to construct platform adapter implementations. */
object CompositionRoot {
    fun create(): PlatformDependencies =
        PlatformDependencies(
            secureStore = NotConfiguredSecureStore(),
            biometricUnlock = NotConfiguredBiometricUnlock(),
            healthDataSource = NotConfiguredHealthDataSource(),
            nfcCapability = NotConfiguredNfcCapability(),
            identitySession = NotConfiguredIdentitySession(),
        )
}
