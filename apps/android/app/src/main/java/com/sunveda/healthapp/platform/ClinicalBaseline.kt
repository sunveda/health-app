package com.sunveda.healthapp.platform

/** Intended later pipeline stages. Stubs never advance these. */
enum class ClinicalPipelineStage {
    SELECTION,
    VALIDATION,
    QUARANTINE,
    UPLOAD,
    REVIEW,
    DELETION,
}

/** Engineering file-type labels only. Not a MIME scanner vendor list. */
enum class ClinicalDeclaredType {
    PDF,
    IMAGE,
}

/** Metadata-only descriptor. Never carries file bytes or real medical documents. */
data class ClinicalFileDescriptor(
    val documentId: String,
    val declaredType: ClinicalDeclaredType,
    val byteSize: Int,
)

/** Local surface status for shells. There is no clinical wire contract yet. */
enum class ClinicalSurfaceStatus(val wireValue: String) {
    NOT_CONFIGURED("not_configured"),
    UNAVAILABLE("unavailable"),
    IDLE("idle"),
    IN_PROGRESS("in_progress"),
}

data class ClinicalCapabilitySnapshot(
    val availability: CapabilityStatus,
    val lastFailure: CapabilityStatus?,
    val activeStage: ClinicalPipelineStage?,
) {
    companion object {
        val notConfigured = ClinicalCapabilitySnapshot(
            availability = CapabilityStatus.NOT_CONFIGURED,
            lastFailure = CapabilityStatus.NOT_CONFIGURED,
            activeStage = null,
        )

        val unavailable = ClinicalCapabilitySnapshot(
            availability = CapabilityStatus.UNAVAILABLE,
            lastFailure = CapabilityStatus.UNAVAILABLE,
            activeStage = null,
        )
    }
}

object ClinicalSurfaceMapping {
    fun status(
        availability: CapabilityStatus,
        lastFailure: CapabilityStatus?,
        activeStage: ClinicalPipelineStage?,
    ): ClinicalSurfaceStatus {
        if (availability == CapabilityStatus.UNAVAILABLE || lastFailure == CapabilityStatus.UNAVAILABLE) {
            return ClinicalSurfaceStatus.UNAVAILABLE
        }
        if (availability == CapabilityStatus.NOT_CONFIGURED || lastFailure == CapabilityStatus.NOT_CONFIGURED) {
            return ClinicalSurfaceStatus.NOT_CONFIGURED
        }
        if (availability == CapabilityStatus.READY && lastFailure == null) {
            return if (activeStage == null) ClinicalSurfaceStatus.IDLE else ClinicalSurfaceStatus.IN_PROGRESS
        }
        return ClinicalSurfaceStatus.NOT_CONFIGURED
    }

    fun status(snapshot: ClinicalCapabilitySnapshot): ClinicalSurfaceStatus =
        status(
            availability = snapshot.availability,
            lastFailure = snapshot.lastFailure,
            activeStage = snapshot.activeStage,
        )
}

fun ClinicalSurfaceStatus.toDisplayLabel(): String =
    when (this) {
        ClinicalSurfaceStatus.NOT_CONFIGURED -> "Not configured"
        ClinicalSurfaceStatus.UNAVAILABLE -> "Unavailable"
        ClinicalSurfaceStatus.IDLE -> "Idle"
        ClinicalSurfaceStatus.IN_PROGRESS -> "In progress"
    }

fun ClinicalPipelineStage.toDisplayLabel(): String =
    when (this) {
        ClinicalPipelineStage.SELECTION -> "Selection"
        ClinicalPipelineStage.VALIDATION -> "Validation"
        ClinicalPipelineStage.QUARANTINE -> "Quarantine"
        ClinicalPipelineStage.UPLOAD -> "Upload"
        ClinicalPipelineStage.REVIEW -> "Review"
        ClinicalPipelineStage.DELETION -> "Deletion"
    }

object ClinicalPresentation {
    fun stagesLabel(stages: Set<ClinicalPipelineStage>): String {
        val names = ClinicalPipelineStage.entries.filter { it in stages }.map { it.toDisplayLabel() }
        return if (names.isEmpty()) "None" else names.joinToString(", ")
    }

    fun activeStageLabel(stage: ClinicalPipelineStage?): String =
        stage?.toDisplayLabel() ?: "None"
}

/** Settings / Health placeholders. Not a claim that Clinical is complete. */
object ClinicalCopyPlaceholder {
    const val PIPELINE_NOT_CONFIGURED =
        "Clinical document upload and review are not configured. Document pickers and network uploads are deferred."

    const val NO_UPLOAD_ENDPOINT =
        "No upload endpoint is configured. File validation, quarantine, and deletion remain unavailable."
}
