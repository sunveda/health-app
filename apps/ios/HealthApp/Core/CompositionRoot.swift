import Foundation

/// Typed dependencies handed to feature modules. Screens must not construct platform adapters.
struct AppDependencies {
    let secureStore: SecureStore
    let biometricUnlock: BiometricUnlock
    let healthDataSource: HealthDataSource
    let wellnessSync: WellnessSyncClient
    let nfcCapability: NfcCapability
    let identitySession: IdentitySession
    let consentStore: ConsentStore
    let crashReporter: CrashReporter
    let telemetryPolicy: TelemetryPolicy
    let fileValidation: FileValidation
    let quarantineStore: QuarantineStore
    let reportUpload: ReportUploadClient
    let clinicalPipeline: ClinicalDocumentPipeline
}

/// Single composition root. The only type allowed to construct platform adapter implementations.
enum CompositionRoot {
    static func make() -> AppDependencies {
        AppDependencies(
            secureStore: NotConfiguredSecureStore(),
            biometricUnlock: NotConfiguredBiometricUnlock(),
            healthDataSource: NotConfiguredHealthDataSource(),
            wellnessSync: NotConfiguredWellnessSyncClient(),
            nfcCapability: NotConfiguredNfcCapability(),
            identitySession: NotConfiguredIdentitySession(),
            consentStore: NotConfiguredConsentStore(),
            crashReporter: NotConfiguredCrashReporter(),
            telemetryPolicy: NotConfiguredTelemetryPolicy(),
            fileValidation: NotConfiguredFileValidation(),
            quarantineStore: NotConfiguredQuarantineStore(),
            reportUpload: NotConfiguredReportUploadClient(),
            clinicalPipeline: NotConfiguredClinicalDocumentPipeline()
        )
    }
}
