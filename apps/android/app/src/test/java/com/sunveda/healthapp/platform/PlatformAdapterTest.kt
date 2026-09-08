package com.sunveda.healthapp.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.nfcCapability.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.identitySession.status)
        assertNull(dependencies.identitySession.pairwiseSubject())
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.consentStore.status)
        assertEquals(ConsentDecision.NOT_RECORDED, dependencies.consentStore.decision(DataCategory.HEALTH_MEASUREMENTS))
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.crashReporter.status)
        assertEquals(CapabilityStatus.NOT_CONFIGURED, dependencies.telemetryPolicy.status)
        assertEquals(false, dependencies.telemetryPolicy.isAllowed(DataCategory.TELEMETRY))
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
}
