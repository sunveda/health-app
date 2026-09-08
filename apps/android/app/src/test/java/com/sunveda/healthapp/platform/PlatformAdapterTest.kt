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
