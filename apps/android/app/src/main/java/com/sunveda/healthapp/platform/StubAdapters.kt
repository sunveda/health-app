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
    override val permissionState = HealthPermissionState.NOT_CONFIGURED
    override val grantedReadScopes: Set<WellnessReadScope> = emptySet()
    override val lastFailure: CapabilityStatus? = CapabilityStatus.NOT_CONFIGURED

    override fun requestReadAccess(scopes: Set<WellnessReadScope>) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)
}

class UnavailableHealthDataSource : HealthDataSource {
    override val status = CapabilityStatus.UNAVAILABLE
    override val permissionState = HealthPermissionState.UNAVAILABLE
    override val grantedReadScopes: Set<WellnessReadScope> = emptySet()
    override val lastFailure: CapabilityStatus? = CapabilityStatus.UNAVAILABLE

    override fun requestReadAccess(scopes: Set<WellnessReadScope>) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)
}

class NotConfiguredWellnessSyncClient : WellnessSyncClient {
    override val featureFlag = WellnessFeatureFlag.DISABLED
    override val status = CapabilityStatus.NOT_CONFIGURED
    override val lastSyncedAt: java.time.Instant? = null

    override fun sync() = CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)
}

class UnavailableWellnessSyncClient : WellnessSyncClient {
    override val featureFlag = WellnessFeatureFlag.DISABLED
    override val status = CapabilityStatus.UNAVAILABLE
    override val lastSyncedAt: java.time.Instant? = null

    override fun sync() = CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)
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

class NotConfiguredConsentStore : ConsentStore {
    override val status = CapabilityStatus.NOT_CONFIGURED

    override fun decision(category: DataCategory) = ConsentDecision.NOT_RECORDED

    override fun record(decision: ConsentDecision, category: DataCategory, purposeId: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)
}

class UnavailableConsentStore : ConsentStore {
    override val status = CapabilityStatus.UNAVAILABLE

    override fun decision(category: DataCategory) = ConsentDecision.NOT_RECORDED

    override fun record(decision: ConsentDecision, category: DataCategory, purposeId: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)
}

/** Test/scaffolding store: category + decision only, no disk, no sensitive payloads. Not wired by CompositionRoot. */
class InMemoryConsentStore : ConsentStore {
    override val status = CapabilityStatus.READY
    private val decisions = mutableMapOf<DataCategory, ConsentDecision>()

    override fun decision(category: DataCategory) =
        decisions[category] ?: ConsentDecision.NOT_RECORDED

    override fun record(decision: ConsentDecision, category: DataCategory, purposeId: String): CapabilityOutcome<Unit> {
        if (decision == ConsentDecision.NOT_RECORDED) {
            decisions.remove(category)
            return CapabilityOutcome.Success(Unit)
        }
        val purpose = PurposeRegistry.definition(purposeId)
        if (purpose == null || purpose.category != category) {
            return CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)
        }
        decisions[category] = decision
        return CapabilityOutcome.Success(Unit)
    }
}

class NotConfiguredCrashReporter : CrashReporter {
    override val status = CapabilityStatus.NOT_CONFIGURED

    override fun captureNonPII(event: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED)
}

class UnavailableCrashReporter : CrashReporter {
    override val status = CapabilityStatus.UNAVAILABLE

    override fun captureNonPII(event: String) =
        CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE)
}

class NotConfiguredTelemetryPolicy : TelemetryPolicy {
    override val status = CapabilityStatus.NOT_CONFIGURED

    override fun isAllowed(category: DataCategory) = false
}

class UnavailableTelemetryPolicy : TelemetryPolicy {
    override val status = CapabilityStatus.UNAVAILABLE

    override fun isAllowed(category: DataCategory) = false
}
