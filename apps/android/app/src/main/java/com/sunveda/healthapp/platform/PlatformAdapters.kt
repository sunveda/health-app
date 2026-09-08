package com.sunveda.healthapp.platform

/**
 * Application-protected storage versus storage that must only be readable after a biometric
 * challenge. Real Keystore access is not performed here; feature code must use this interface only.
 */
interface SecureStore {
    val applicationStoreStatus: CapabilityStatus
    val biometricGatedStoreStatus: CapabilityStatus

    fun readApplicationValue(key: String): CapabilityOutcome<ByteArray?>
    fun writeApplicationValue(key: String, value: ByteArray): CapabilityOutcome<Unit>
    fun deleteApplicationValue(key: String): CapabilityOutcome<Unit>

    fun readBiometricGatedValue(key: String): CapabilityOutcome<ByteArray?>
    fun writeBiometricGatedValue(key: String, value: ByteArray): CapabilityOutcome<Unit>
    fun deleteBiometricGatedValue(key: String): CapabilityOutcome<Unit>
}

interface BiometricUnlock {
    val status: CapabilityStatus
    fun unlock(): CapabilityOutcome<Unit>
}

interface HealthDataSource {
    val status: CapabilityStatus
}

interface NfcCapability {
    val status: CapabilityStatus
}

/** Official identity-provider session. Stub only: no OIDC issuer, client ID, or tokens. */
interface IdentitySession {
    val status: CapabilityStatus
    fun pairwiseSubject(): String?
}
