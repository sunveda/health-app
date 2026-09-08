package com.sunveda.healthapp.platform

import java.time.Instant

/** Intended later platform read scopes. Stubs never request these from Health Connect. */
enum class WellnessReadScope {
    STEPS,
    HEART_RATE,
}

/** Permission lane distinct from adapter availability (`CapabilityStatus`). */
enum class HealthPermissionState {
    NOT_CONFIGURED,
    UNAVAILABLE,
    NOT_DETERMINED,
    DENIED,
    AUTHORIZED,
}

/** Shipping default is disabled until kit adapters are reviewed and wired by CompositionRoot. */
enum class WellnessFeatureFlag {
    DISABLED,
    ENABLED,
    ;

    val isEnabled: Boolean get() = this == ENABLED

    companion object {
        val shipping: WellnessFeatureFlag = DISABLED
    }
}

/** Contract `wellnessSync.status` values from `packages/contracts`. */
enum class WellnessContractStatus(val wireValue: String) {
    NOT_CONNECTED("not_connected"),
    CONNECTED("connected"),
    PERMISSION_DENIED("permission_denied"),
    ERROR("error"),
}

data class WellnessCapabilitySnapshot(
    val availability: CapabilityStatus,
    val permissionState: HealthPermissionState,
    val grantedReadScopes: Set<WellnessReadScope>,
    val lastFailure: CapabilityStatus?,
) {
    companion object {
        val notConfigured = WellnessCapabilitySnapshot(
            availability = CapabilityStatus.NOT_CONFIGURED,
            permissionState = HealthPermissionState.NOT_CONFIGURED,
            grantedReadScopes = emptySet(),
            lastFailure = CapabilityStatus.NOT_CONFIGURED,
        )

        val unavailable = WellnessCapabilitySnapshot(
            availability = CapabilityStatus.UNAVAILABLE,
            permissionState = HealthPermissionState.UNAVAILABLE,
            grantedReadScopes = emptySet(),
            lastFailure = CapabilityStatus.UNAVAILABLE,
        )
    }
}

object WellnessContractMapping {
    fun status(
        availability: CapabilityStatus,
        permissionState: HealthPermissionState,
        lastSyncedAt: Instant?,
        lastFailure: CapabilityStatus?,
    ): WellnessContractStatus {
        if (permissionState == HealthPermissionState.DENIED) {
            return WellnessContractStatus.PERMISSION_DENIED
        }
        if (availability == CapabilityStatus.UNAVAILABLE || lastFailure == CapabilityStatus.UNAVAILABLE) {
            return WellnessContractStatus.ERROR
        }
        if (
            availability == CapabilityStatus.READY &&
            permissionState == HealthPermissionState.AUTHORIZED &&
            lastSyncedAt != null &&
            lastFailure == null
        ) {
            return WellnessContractStatus.CONNECTED
        }
        return WellnessContractStatus.NOT_CONNECTED
    }

    fun status(snapshot: WellnessCapabilitySnapshot, lastSyncedAt: Instant?): WellnessContractStatus =
        status(
            availability = snapshot.availability,
            permissionState = snapshot.permissionState,
            lastSyncedAt = lastSyncedAt,
            lastFailure = snapshot.lastFailure,
        )
}

fun HealthPermissionState.toDisplayLabel(): String =
    when (this) {
        HealthPermissionState.NOT_CONFIGURED -> "Not configured"
        HealthPermissionState.UNAVAILABLE -> "Unavailable"
        HealthPermissionState.NOT_DETERMINED -> "Not determined"
        HealthPermissionState.DENIED -> "Denied"
        HealthPermissionState.AUTHORIZED -> "Authorized"
    }

fun WellnessFeatureFlag.toDisplayLabel(): String =
    if (isEnabled) "Enabled" else "Disabled"

object WellnessPresentation {
    fun scopesLabel(scopes: Set<WellnessReadScope>): String {
        val names = WellnessReadScope.entries.filter { it in scopes }.map { displayName(it) }
        return if (names.isEmpty()) "None" else names.joinToString(", ")
    }

    fun displayName(scope: WellnessReadScope): String =
        when (scope) {
            WellnessReadScope.STEPS -> "Steps"
            WellnessReadScope.HEART_RATE -> "Heart rate"
        }
}

/** Settings / Health placeholders. Not a claim that Wellness is complete. */
object WellnessCopyPlaceholder {
    const val KIT_WIRING_DEFERRED =
        "Health Connect is not configured. Permission requests and reads are deferred."

    const val FEATURE_DISABLED =
        "Wellness sync is disabled until adapters are reviewed and the feature flag is enabled."
}
