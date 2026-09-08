package com.sunveda.healthapp.platform

/** Typed dependencies handed to feature modules. Screens must not construct platform adapters. */
data class PlatformDependencies(
    val secureStore: SecureStore,
    val biometricUnlock: BiometricUnlock,
    val healthDataSource: HealthDataSource,
    val wellnessSync: WellnessSyncClient,
    val nfcCapability: NfcCapability,
    val identitySession: IdentitySession,
    val consentStore: ConsentStore,
    val crashReporter: CrashReporter,
    val telemetryPolicy: TelemetryPolicy,
    val fileValidation: FileValidation,
    val quarantineStore: QuarantineStore,
    val reportUpload: ReportUploadClient,
    val clinicalPipeline: ClinicalDocumentPipeline,
)

/** Single composition root. The only type allowed to construct platform adapter implementations. */
object CompositionRoot {
    fun create(): PlatformDependencies =
        PlatformDependencies(
            secureStore = NotConfiguredSecureStore(),
            biometricUnlock = NotConfiguredBiometricUnlock(),
            healthDataSource = NotConfiguredHealthDataSource(),
            wellnessSync = NotConfiguredWellnessSyncClient(),
            nfcCapability = NotConfiguredNfcCapability(),
            identitySession = NotConfiguredIdentitySession(),
            consentStore = NotConfiguredConsentStore(),
            crashReporter = NotConfiguredCrashReporter(),
            telemetryPolicy = NotConfiguredTelemetryPolicy(),
            fileValidation = NotConfiguredFileValidation(),
            quarantineStore = NotConfiguredQuarantineStore(),
            reportUpload = NotConfiguredReportUploadClient(),
            clinicalPipeline = NotConfiguredClinicalDocumentPipeline(),
        )
}
