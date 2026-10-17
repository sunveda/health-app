package com.sunveda.healthapp.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class CapabilityStatusMappingTest {
    @Test
    fun mapsAdapterStatusesToUserFacingLabels() {
        assertEquals("Not configured", CapabilityStatus.NOT_CONFIGURED.toDisplayLabel())
        assertEquals("Unavailable", CapabilityStatus.UNAVAILABLE.toDisplayLabel())
        assertEquals("Ready", CapabilityStatus.READY.toDisplayLabel())
        assertEquals("Permission required", CapabilityStatus.PERMISSION_REQUIRED.toDisplayLabel())
    }
}

class CompositionRootTest {
    @Test
    fun wiresNotConfiguredAdapters() {
        val dependencies = CompositionRoot.create()

        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.secureStore.applicationStoreStatus)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.secureStore.biometricGatedStoreStatus)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.biometricUnlock.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.healthDataSource.status)
        assertEquals(HealthPermissionState.NOT_CONFIGURED, dependencies.healthDataSource.permissionState)
        assertEquals(true, dependencies.healthDataSource.grantedReadScopes.isEmpty())
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.healthDataSource.lastFailure)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.wellnessSync.status)
        assertEquals(WellnessFeatureFlag.DISABLED, dependencies.wellnessSync.featureFlag)
        assertEquals(false, dependencies.wellnessSync.featureFlag.isEnabled)
        assertEquals(WellnessFeatureFlag.DISABLED, WellnessFeatureFlag.shipping)
        assertNull(dependencies.wellnessSync.lastSyncedAt)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.nfcCapability.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.identitySession.status)
        assertNull(dependencies.identitySession.pairwiseSubject())
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.consentStore.status)
        assertEquals(ConsentDecision.NOT_RECORDED, dependencies.consentStore.decision(DataCategory.HEALTH_MEASUREMENTS))
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.crashReporter.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.telemetryPolicy.status)
        assertEquals(false, dependencies.telemetryPolicy.isAllowed(DataCategory.TELEMETRY))
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.fileValidation.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.quarantineStore.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.reportUpload.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.clinicalPipeline.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.clinicalPipeline.lastFailure)
        assertNull(dependencies.clinicalPipeline.activeStage)
        assertEquals(ClinicalCapabilitySnapshot.notConfigured, dependencies.clinicalPipeline.capabilitySnapshot())
        assertEquals(
            ClinicalSurfaceStatus.NOT_CONFIGURED,
            ClinicalSurfaceMapping.status(dependencies.clinicalPipeline.capabilitySnapshot()),
        )
    }
}

class SecureStoreStubTest {
    @Test
    fun notConfiguredStoreRejectsBothLanes() {
        val store = NotConfiguredSecureStore()

        assertEquals(CapabilityStatus.NOT_CONFIGURED, store.applicationStoreStatus)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, store.biometricGatedStoreStatus)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            store.readApplicationValue("session"),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            store.readBiometricGatedValue("session"),
        )
    }

    @Test
    fun unavailableStoreRejectsBothLanes() {
        val store = UnavailableSecureStore()

        assertEquals(CapabilityStatus.UNAVAILABLE, store.applicationStoreStatus)
        assertEquals(CapabilityStatus.UNAVAILABLE, store.biometricGatedStoreStatus)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            store.readApplicationValue("session"),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            store.readBiometricGatedValue("session"),
        )
    }
}

class PrivacyBaselineTest {
    @Test
    fun purposeRegistryCoversEveryDataCategoryOnce() {
        val categories = PurposeRegistry.all.map { it.category }.toSet()
        assertEquals(DataCategory.entries.toSet(), categories)
        assertEquals(DataCategory.entries.size, PurposeRegistry.all.size)
        assertEquals(PurposeRegistry.all.map { it.id }.toSet().size, PurposeRegistry.all.size)

        DataCategory.entries.forEach { category ->
            assertEquals(1, PurposeRegistry.purposes(category).size)
        }

        assertEquals(
            DataCategory.INDIVIDUAL_NUMBER,
            PurposeRegistry.definition("purpose.identity.individual-number-review")?.category,
        )
        assertNull(PurposeRegistry.definition("purpose.does-not-exist"))
    }

    @Test
    fun logRedactionNeverInterpolatesSensitiveValues() {
        val leakedNumber = "123456789012"
        val leakedToken = "supersecrettokenvalue"
        val message = LogRedaction.sanitize(
            event = "session-start",
            fields = mapOf(
                SensitiveField.INDIVIDUAL_NUMBER to leakedNumber,
                SensitiveField.ACCESS_TOKEN to leakedToken,
                SensitiveField.DIAGNOSIS to "synthetic-diagnosis-label",
            ),
        )

        assertEquals(true, message.startsWith("event=session-start"))
        assertEquals(false, message.contains(leakedNumber))
        assertEquals(false, message.contains(leakedToken))
        assertEquals(false, message.contains("synthetic-diagnosis-label"))
        assertEquals(true, message.contains(LogRedaction.replacement(SensitiveField.INDIVIDUAL_NUMBER)))
        assertEquals(true, message.contains(LogRedaction.replacement(SensitiveField.ACCESS_TOKEN)))
        assertEquals("event=noop", LogRedaction.sanitize("noop", emptyMap()))

        SensitiveField.entries.forEach { field ->
            assertEquals(true, LogRedaction.isForbiddenInLogs(field))
        }
    }

    @Test
    fun notConfiguredPrivacyAdaptersRejectWritesAndDenyTelemetry() {
        val consent = NotConfiguredConsentStore()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, consent.status)
        assertEquals(ConsentDecision.NOT_RECORDED, consent.decision(DataCategory.CLINICAL_DOCUMENTS))
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            consent.record(
                ConsentDecision.GRANTED,
                DataCategory.CLINICAL_DOCUMENTS,
                "purpose.clinical.document-review",
            ),
        )

        val crash = NotConfiguredCrashReporter()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, crash.status)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            crash.captureNonPII("launch"),
        )

        val telemetry = NotConfiguredTelemetryPolicy()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, telemetry.status)
        DataCategory.entries.forEach { category ->
            assertEquals(false, telemetry.isAllowed(category))
        }
    }

    @Test
    fun inMemoryConsentStoreRecordsGrantedAndDeniedWithoutPayloads() {
        val store = InMemoryConsentStore()
        assertEquals(CapabilityStatus.READY, store.status)
        assertEquals(ConsentDecision.NOT_RECORDED, store.decision(DataCategory.EXPENSES))

        assertEquals(
            CapabilityOutcome.Success(Unit),
            store.record(
                ConsentDecision.GRANTED,
                DataCategory.EXPENSES,
                "purpose.expenses.export-preview",
            ),
        )
        assertEquals(ConsentDecision.GRANTED, store.decision(DataCategory.EXPENSES))

        assertEquals(
            CapabilityOutcome.Success(Unit),
            store.record(
                ConsentDecision.DENIED,
                DataCategory.HEALTH_MEASUREMENTS,
                "purpose.health.measurements-display",
            ),
        )
        assertEquals(ConsentDecision.DENIED, store.decision(DataCategory.HEALTH_MEASUREMENTS))

        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            store.record(
                ConsentDecision.GRANTED,
                DataCategory.TELEMETRY,
                "purpose.expenses.export-preview",
            ),
        )
        assertEquals(ConsentDecision.NOT_RECORDED, store.decision(DataCategory.TELEMETRY))
    }

    @Test
    fun privacyCopyPlaceholdersStayTodosWithoutLegalClaims() {
        val copy = listOf(
            PrivacyCopyPlaceholder.LEGAL_REVIEW_TODO,
            PrivacyCopyPlaceholder.CONSENT_STORE_NOT_CONFIGURED,
            PrivacyCopyPlaceholder.CRASH_AND_TELEMETRY_DISABLED,
        )
        copy.forEach { line ->
            assertEquals(true, line.isNotBlank())
        }
        assertEquals(true, PrivacyCopyPlaceholder.LEGAL_REVIEW_TODO.contains("TODO(product/legal)"))
        val joined = copy.joinToString(" ").lowercase()
        assertEquals(false, joined.contains("個人情報保護法"))
        assertEquals(false, joined.contains("番号法"))
        assertEquals(false, joined.contains("appi"))
    }
}

class WellnessBaselineTest {
    @Test
    fun contractMappingCoversStubAndLaterStates() {
        assertEquals(
            WellnessContractStatus.NOT_CONNECTED,
            WellnessContractMapping.status(WellnessCapabilitySnapshot.notConfigured, null),
        )
        assertEquals(
            WellnessContractStatus.ERROR,
            WellnessContractMapping.status(WellnessCapabilitySnapshot.unavailable, null),
        )

        val denied = WellnessCapabilitySnapshot(
            availability = CapabilityStatus.PERMISSION_REQUIRED,
            permissionState = HealthPermissionState.DENIED,
            grantedReadScopes = emptySet(),
            lastFailure = null,
        )
        assertEquals(
            WellnessContractStatus.PERMISSION_DENIED,
            WellnessContractMapping.status(denied, null),
        )

        val readyUnsynced = WellnessCapabilitySnapshot(
            availability = CapabilityStatus.READY,
            permissionState = HealthPermissionState.AUTHORIZED,
            grantedReadScopes = setOf(WellnessReadScope.STEPS),
            lastFailure = null,
        )
        assertEquals(
            WellnessContractStatus.NOT_CONNECTED,
            WellnessContractMapping.status(readyUnsynced, null),
        )
        assertEquals(
            WellnessContractStatus.CONNECTED,
            WellnessContractMapping.status(readyUnsynced, Instant.ofEpochSecond(1)),
        )

        val failedReady = WellnessCapabilitySnapshot(
            availability = CapabilityStatus.READY,
            permissionState = HealthPermissionState.AUTHORIZED,
            grantedReadScopes = setOf(WellnessReadScope.STEPS, WellnessReadScope.HEART_RATE),
            lastFailure = CapabilityStatus.UNAVAILABLE,
        )
        assertEquals(
            WellnessContractStatus.ERROR,
            WellnessContractMapping.status(failedReady, Instant.ofEpochSecond(1)),
        )
    }

    @Test
    fun presentationAndPermissionLabels() {
        assertEquals("None", WellnessPresentation.scopesLabel(emptySet()))
        assertEquals("Heart rate", WellnessPresentation.scopesLabel(setOf(WellnessReadScope.HEART_RATE)))
        assertEquals(
            "Steps, Heart rate",
            WellnessPresentation.scopesLabel(setOf(WellnessReadScope.HEART_RATE, WellnessReadScope.STEPS)),
        )
        assertEquals("Not configured", HealthPermissionState.NOT_CONFIGURED.toDisplayLabel())
        assertEquals("Denied", HealthPermissionState.DENIED.toDisplayLabel())
        assertEquals("Authorized", HealthPermissionState.AUTHORIZED.toDisplayLabel())
        assertEquals("Disabled", WellnessFeatureFlag.DISABLED.toDisplayLabel())
        assertEquals("Enabled", WellnessFeatureFlag.ENABLED.toDisplayLabel())
    }

    @Test
    fun notConfiguredAdaptersRejectReadsAndKeepEmptyScopes() {
        val source = NotConfiguredHealthDataSource()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, source.status)
        assertEquals(HealthPermissionState.NOT_CONFIGURED, source.permissionState)
        assertEquals(true, source.grantedReadScopes.isEmpty())
        assertEquals(CapabilityStatus.NOT_CONFIGURED, source.lastFailure)
        assertEquals(WellnessCapabilitySnapshot.notConfigured, source.capabilitySnapshot())
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            source.requestReadAccess(emptySet()),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            source.requestReadAccess(setOf(WellnessReadScope.STEPS, WellnessReadScope.HEART_RATE)),
        )
        assertEquals(true, source.grantedReadScopes.isEmpty())

        val unavailable = UnavailableHealthDataSource()
        assertEquals(WellnessCapabilitySnapshot.unavailable, unavailable.capabilitySnapshot())
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailable.requestReadAccess(setOf(WellnessReadScope.STEPS)),
        )

        val sync = NotConfiguredWellnessSyncClient()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, sync.status)
        assertEquals(WellnessFeatureFlag.DISABLED, sync.featureFlag)
        assertNull(sync.lastSyncedAt)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            sync.sync(),
        )
        assertEquals(
            WellnessContractStatus.NOT_CONNECTED,
            WellnessContractMapping.status(source.capabilitySnapshot(), sync.lastSyncedAt),
        )

        val unavailableSync = UnavailableWellnessSyncClient()
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailableSync.sync(),
        )
    }

    @Test
    fun copyDoesNotClaimKitWiringOrCompletedStages() {
        val copy = listOf(
            WellnessCopyPlaceholder.KIT_WIRING_DEFERRED,
            WellnessCopyPlaceholder.FEATURE_DISABLED,
        )
        copy.forEach { line ->
            assertEquals(true, line.isNotBlank())
        }
        val joined = copy.joinToString(" ").lowercase()
        assertEquals(true, joined.contains("not configured") || joined.contains("disabled"))
        assertEquals(true, joined.contains("deferred") || joined.contains("disabled"))
        assertEquals(false, joined.contains("connected to healthkit"))
        assertEquals(false, joined.contains("identity complete"))
        assertEquals(false, joined.contains("clinical complete"))
    }
}

class ClinicalBaselineTest {
    @Test
    fun surfaceMappingCoversStubAndLaterStates() {
        assertEquals(
            ClinicalSurfaceStatus.NOT_CONFIGURED,
            ClinicalSurfaceMapping.status(ClinicalCapabilitySnapshot.notConfigured),
        )
        assertEquals(
            ClinicalSurfaceStatus.UNAVAILABLE,
            ClinicalSurfaceMapping.status(ClinicalCapabilitySnapshot.unavailable),
        )

        val idle = ClinicalCapabilitySnapshot(
            availability = CapabilityStatus.READY,
            lastFailure = null,
            activeStage = null,
        )
        assertEquals(ClinicalSurfaceStatus.IDLE, ClinicalSurfaceMapping.status(idle))

        val inProgress = ClinicalCapabilitySnapshot(
            availability = CapabilityStatus.READY,
            lastFailure = null,
            activeStage = ClinicalPipelineStage.VALIDATION,
        )
        assertEquals(ClinicalSurfaceStatus.IN_PROGRESS, ClinicalSurfaceMapping.status(inProgress))

        val failedReady = ClinicalCapabilitySnapshot(
            availability = CapabilityStatus.READY,
            lastFailure = CapabilityStatus.UNAVAILABLE,
            activeStage = ClinicalPipelineStage.UPLOAD,
        )
        assertEquals(ClinicalSurfaceStatus.UNAVAILABLE, ClinicalSurfaceMapping.status(failedReady))

        val permissionRequired = ClinicalCapabilitySnapshot(
            availability = CapabilityStatus.PERMISSION_REQUIRED,
            lastFailure = null,
            activeStage = ClinicalPipelineStage.SELECTION,
        )
        assertEquals(ClinicalSurfaceStatus.NOT_CONFIGURED, ClinicalSurfaceMapping.status(permissionRequired))
    }

    @Test
    fun presentationAndSurfaceLabels() {
        assertEquals("None", ClinicalPresentation.stagesLabel(emptySet()))
        assertEquals("Review", ClinicalPresentation.stagesLabel(setOf(ClinicalPipelineStage.REVIEW)))
        assertEquals(
            "Selection, Validation, Deletion",
            ClinicalPresentation.stagesLabel(
                setOf(
                    ClinicalPipelineStage.DELETION,
                    ClinicalPipelineStage.SELECTION,
                    ClinicalPipelineStage.VALIDATION,
                ),
            ),
        )
        assertEquals("None", ClinicalPresentation.activeStageLabel(null))
        assertEquals("Quarantine", ClinicalPresentation.activeStageLabel(ClinicalPipelineStage.QUARANTINE))
        assertEquals("Not configured", ClinicalSurfaceStatus.NOT_CONFIGURED.toDisplayLabel())
        assertEquals("Unavailable", ClinicalSurfaceStatus.UNAVAILABLE.toDisplayLabel())
        assertEquals("Idle", ClinicalSurfaceStatus.IDLE.toDisplayLabel())
        assertEquals("In progress", ClinicalSurfaceStatus.IN_PROGRESS.toDisplayLabel())
        assertEquals("Upload", ClinicalPipelineStage.UPLOAD.toDisplayLabel())
    }

    @Test
    fun notConfiguredAdaptersRejectPipelineWork() {
        val descriptor = ClinicalFileDescriptor(
            documentId = "SYNTH-CLINICAL-DOC-1",
            declaredType = ClinicalDeclaredType.PDF,
            byteSize = 1024,
        )

        val validation = NotConfiguredFileValidation()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, validation.status)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            validation.validate(descriptor),
        )

        val unavailableValidation = UnavailableFileValidation()
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailableValidation.validate(descriptor),
        )

        val quarantine = NotConfiguredQuarantineStore()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, quarantine.status)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            quarantine.storedDocumentIds(),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            quarantine.quarantine(descriptor),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            quarantine.discard(descriptor.documentId),
        )

        val unavailableQuarantine = UnavailableQuarantineStore()
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailableQuarantine.storedDocumentIds(),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailableQuarantine.discard(descriptor.documentId),
        )

        val upload = NotConfiguredReportUploadClient()
        assertEquals(CapabilityStatus.NOT_CONFIGURED, upload.status)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            upload.upload(descriptor.documentId),
        )

        val unavailableUpload = UnavailableReportUploadClient()
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailableUpload.upload(descriptor.documentId),
        )

        val pipeline = NotConfiguredClinicalDocumentPipeline()
        assertEquals(ClinicalCapabilitySnapshot.notConfigured, pipeline.capabilitySnapshot())
        assertNull(pipeline.activeStage)
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            pipeline.requestDocumentSelection(),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            pipeline.startReview(descriptor.documentId),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.NOT_CONFIGURED),
            pipeline.delete(descriptor.documentId),
        )
        assertEquals(
            ClinicalSurfaceStatus.NOT_CONFIGURED,
            ClinicalSurfaceMapping.status(pipeline.capabilitySnapshot()),
        )

        val unavailablePipeline = UnavailableClinicalDocumentPipeline()
        assertEquals(ClinicalCapabilitySnapshot.unavailable, unavailablePipeline.capabilitySnapshot())
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailablePipeline.requestDocumentSelection(),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailablePipeline.startReview(descriptor.documentId),
        )
        assertEquals(
            CapabilityOutcome.Unavailable(CapabilityStatus.UNAVAILABLE),
            unavailablePipeline.delete(descriptor.documentId),
        )
    }

    @Test
    fun copyDoesNotClaimUploadOrCompletedStages() {
        val copy = listOf(
            ClinicalCopyPlaceholder.PIPELINE_NOT_CONFIGURED,
            ClinicalCopyPlaceholder.NO_UPLOAD_ENDPOINT,
        )
        copy.forEach { line ->
            assertEquals(true, line.isNotBlank())
        }
        val joined = copy.joinToString(" ").lowercase()
        assertEquals(true, joined.contains("not configured"))
        assertEquals(true, joined.contains("deferred") || joined.contains("unavailable"))
        assertEquals(false, joined.contains("upload complete"))
        assertEquals(false, joined.contains("identity complete"))
        assertEquals(false, joined.contains("clinical complete"))
        assertEquals(false, joined.contains("storage.googleapis.com"))
        assertEquals(false, joined.contains("s3.amazonaws.com"))
    }
}
