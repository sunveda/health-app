package com.sunveda.healthapp.platform

enum class CapabilityStatus {
    NOT_CONFIGURED,
    UNAVAILABLE,
    READY,
    PERMISSION_REQUIRED,
}

sealed class CapabilityOutcome<out T> {
    data class Success<T>(val value: T) : CapabilityOutcome<T>()
    data class Unavailable(val status: CapabilityStatus) : CapabilityOutcome<Nothing>()
}

fun CapabilityStatus.toDisplayLabel(): String =
    when (this) {
        CapabilityStatus.NOT_CONFIGURED -> "Not configured"
        CapabilityStatus.UNAVAILABLE -> "Unavailable"
        CapabilityStatus.READY -> "Ready"
        CapabilityStatus.PERMISSION_REQUIRED -> "Permission required"
    }
