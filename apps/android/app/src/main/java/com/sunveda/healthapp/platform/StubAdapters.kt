@file:Suppress("UNUSED_PARAMETER")

package com.sunveda.healthapp.platform

class NotConfiguredSecureStore : SecureStore {
    override val applicationStoreStatus = CapabilityStatus.NOT_CONFIGURED
    override val biometricGatedStoreStatus = CapabilityStatus.NOT_CONFIGURED

    override fun readApplicationValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)

    override fun writeApplicationValue(key: String, value: ByteArray) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)

    override fun deleteApplicationValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)

    override fun readBiometricGatedValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)

    override fun writeBiometricGatedValue(key: String, value: ByteArray) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)

    override fun deleteBiometricGatedValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)
}

class UnavailableSecureStore : SecureStore {
    override val applicationStoreStatus = CapabilityStatus.UNAVAILABLE
    override val biometricGatedStoreStatus = CapabilityStatus.UNAVAILABLE

    override fun readApplicationValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)

    override fun writeApplicationValue(key: String, value: ByteArray) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)

    override fun deleteApplicationValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)

    override fun readBiometricGatedValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)

    override fun writeBiometricGatedValue(key: String, value: ByteArray) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)

    override fun deleteBiometricGatedValue(key: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)
}

class NotConfiguredBiometricUnlock : BiometricUnlock {
    override val status = CapabilityStatus.NOT_CONFIGURED

    override fun unlock() = CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)
}

class UnavailableBiometricUnlock : BiometricUnlock {
    override val status = CapabilityStatus.UNAVAILABLE

    override fun unlock() = CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)
}

class NotConfiguredHealthDataSource : HealthDataSource {
    override val status = CapabilityStatus.NOT_CONFIGURED
}

class UnavailableHealthDataSource : HealthDataSource {
    override val status = CapabilityStatus.UNAVAILABLE
}

class NotConfiguredNfcCapability : NfcCapability {
    override val status = CapabilityStatus.NOT_CONFIGURED
}

class UnavailableNfcCapability : NfcCapability {
    override val status = CapabilityStatus.UNAVAILABLE
}

class NotConfiguredIdentitySession : IdentitySession {
    override val status = CapabilityStatus.NOT_CONFIGURED

    override fun pairwiseSubject(): String? = null
}

class UnavailableIdentitySession : IdentitySession {
    override val status = CapabilityStatus.UNAVAILABLE

    override fun pairwiseSubject(): String? = null
}
