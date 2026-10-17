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

/// Device-health adapter. Stubs must not call HealthKit; they expose availability, permission, scope, and failure only.
protocol HealthDataSource {
    var status: CapabilityStatus { get }
    var permissionState: HealthPermissionState { get }
    var grantedReadScopes: Set<WellnessReadScope> { get }
    var lastFailure: CapabilityError? { get }

    /// Never prompts the system on stubs; returns not configured / unavailable.
    func requestReadAccess(scopes: Set<WellnessReadScope>) -> Result<Void, CapabilityError>
}

extension HealthDataSource {
    func capabilitySnapshot() -> WellnessCapabilitySnapshot {
        WellnessCapabilitySnapshot(
            availability: status,
            permissionState: permissionState,
            grantedReadScopes: grantedReadScopes,
            lastFailure: lastFailure
        )
    }
}

/// Feature-facing wellness sync. Screens depend on this instead of importing device kits.
protocol WellnessSyncClient {
    var featureFlag: WellnessFeatureFlag { get }
    var status: CapabilityStatus { get }
    var lastSyncedAt: Date? { get }

    func sync() -> Result<Void, CapabilityError>
}

protocol NfcCapability {
    var status: CapabilityStatus { get }
}

/// Official identity-provider session. Stub only: no OIDC issuer, client ID, or tokens.
protocol IdentitySession {
    var status: CapabilityStatus { get }
    func pairwiseSubject() -> String?
}

/// Records category + granted/denied + purpose id only. No sensitive payloads.
protocol ConsentStore {
    var status: CapabilityStatus { get }
    func decision(for category: DataCategory) -> ConsentDecision
    func record(decision: ConsentDecision, for category: DataCategory, purposeId: String) -> Result<Void, CapabilityError>
}

/// Crash reporting is not configured; implementations must never send PII or a DSN-backed payload.
protocol CrashReporter {
    var status: CapabilityStatus { get }
    func captureNonPII(event: String) -> Result<Void, CapabilityError>
}

/// Telemetry allow/deny by data category. NotConfigured implementations deny every category.
protocol TelemetryPolicy {
    var status: CapabilityStatus { get }
    func isAllowed(_ category: DataCategory) -> Bool
}

/// Client-side type/size checks. Stubs do not inspect bytes or call a scanner vendor.
protocol FileValidation {
    var status: CapabilityStatus { get }
    func validate(descriptor: ClinicalFileDescriptor) -> Result<Void, CapabilityError>
}

/// Local holding area before any upload. Stubs store nothing.
protocol QuarantineStore {
    var status: CapabilityStatus { get }
    func storedDocumentIds() -> Result<[String], CapabilityError>
    func quarantine(descriptor: ClinicalFileDescriptor) -> Result<Void, CapabilityError>
    func discard(documentId: String) -> Result<Void, CapabilityError>
}

/// Encrypted report upload. Stubs never open a network session or invent a URL.
protocol ReportUploadClient {
    var status: CapabilityStatus { get }
    func upload(documentId: String) -> Result<Void, CapabilityError>
}

/// Upload/review surface. Document selection stays behind this adapter and must not present a picker on stubs.
protocol ClinicalDocumentPipeline {
    var status: CapabilityStatus { get }
    var lastFailure: CapabilityError? { get }
    var activeStage: ClinicalPipelineStage? { get }

    /// Never presents a system document picker on stubs; does not request photo or files permissions.
    func requestDocumentSelection() -> Result<Void, CapabilityError>
    func startReview(documentId: String) -> Result<Void, CapabilityError>
    func delete(documentId: String) -> Result<Void, CapabilityError>
}

extension ClinicalDocumentPipeline {
    func capabilitySnapshot() -> ClinicalCapabilitySnapshot {
        ClinicalCapabilitySnapshot(
            availability: status,
            lastFailure: lastFailure,
            activeStage: activeStage
        )
    }
}
