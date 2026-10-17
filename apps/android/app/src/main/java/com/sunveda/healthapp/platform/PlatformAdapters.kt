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

/** Device-health adapter. Stubs must not call Health Connect; they expose availability, permission, scope, and failure only. */
interface HealthDataSource {
    val status: CapabilityStatus
    val permissionState: HealthPermissionState
    val grantedReadScopes: Set<WellnessReadScope>
    val lastFailure: CapabilityStatus?

    /** Never prompts the system on stubs; returns not configured / unavailable. */
    fun requestReadAccess(scopes: Set<WellnessReadScope>): CapabilityOutcome<Unit>

    fun capabilitySnapshot(): WellnessCapabilitySnapshot =
        WellnessCapabilitySnapshot(
            availability = status,
            permissionState = permissionState,
            grantedReadScopes = grantedReadScopes,
            lastFailure = lastFailure,
        )
}

/** Feature-facing wellness sync. Screens depend on this instead of importing device kits. */
interface WellnessSyncClient {
    val featureFlag: WellnessFeatureFlag
    val status: CapabilityStatus
    val lastSyncedAt: java.time.Instant?

    fun sync(): CapabilityOutcome<Unit>
}

interface NfcCapability {
    val status: CapabilityStatus
}

/** Official identity-provider session. Stub only: no OIDC issuer, client ID, or tokens. */
interface IdentitySession {
    val status: CapabilityStatus
    fun pairwiseSubject(): String?
}

/** Records category + granted/denied + purpose id only. No sensitive payloads. */
interface ConsentStore {
    val status: CapabilityStatus
    fun decision(category: DataCategory): ConsentDecision
    fun record(decision: ConsentDecision, category: DataCategory, purposeId: String): CapabilityOutcome<Unit>
}

/** Crash reporting is not configured; implementations must never send PII or a DSN-backed payload. */
interface CrashReporter {
    val status: CapabilityStatus
    fun captureNonPII(event: String): CapabilityOutcome<Unit>
}

/** Telemetry allow/deny by data category. NotConfigured implementations deny every category. */
interface TelemetryPolicy {
    val status: CapabilityStatus
    fun isAllowed(category: DataCategory): Boolean
}

/** Client-side type/size checks. Stubs do not inspect bytes or call a scanner vendor. */
interface FileValidation {
    val status: CapabilityStatus
    fun validate(descriptor: ClinicalFileDescriptor): CapabilityOutcome<Unit>
}

/** Local holding area before any upload. Stubs store nothing. */
interface QuarantineStore {
    val status: CapabilityStatus
    fun storedDocumentIds(): CapabilityOutcome<List<String>>
    fun quarantine(descriptor: ClinicalFileDescriptor): CapabilityOutcome<Unit>
    fun discard(documentId: String): CapabilityOutcome<Unit>
}

/** Encrypted report upload. Stubs never open a network session or invent a URL. */
interface ReportUploadClient {
    val status: CapabilityStatus
    fun upload(documentId: String): CapabilityOutcome<Unit>
}

/** Upload/review surface. Document selection stays behind this adapter and must not present a picker on stubs. */
interface ClinicalDocumentPipeline {
    val status: CapabilityStatus
    val lastFailure: CapabilityStatus?
    val activeStage: ClinicalPipelineStage?

    /** Never presents a system document picker on stubs; does not request photo or files permissions. */
    fun requestDocumentSelection(): CapabilityOutcome<Unit>
    fun startReview(documentId: String): CapabilityOutcome<Unit>
    fun delete(documentId: String): CapabilityOutcome<Unit>

    fun capabilitySnapshot(): ClinicalCapabilitySnapshot =
        ClinicalCapabilitySnapshot(
            availability = status,
            lastFailure = lastFailure,
            activeStage = activeStage,
        )
}
