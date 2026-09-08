package com.sunveda.healthapp.platform

/** Product data categories for consent/purpose scaffolding. Not a legal determination. */
enum class DataCategory {
    IDENTITY_PAIRWISE,
    INDIVIDUAL_NUMBER,
    HEALTH_MEASUREMENTS,
    CLINICAL_DOCUMENTS,
    EXPENSES,
    TELEMETRY,
}

enum class ConsentDecision {
    GRANTED,
    DENIED,
    NOT_RECORDED,
}

/** Engineering purpose id plus a non-legal summary. User-facing copy is TODO for product/legal. */
data class PurposeDefinition(
    val id: String,
    val category: DataCategory,
    val engineeringSummary: String,
)

object PurposeRegistry {
    val all: List<PurposeDefinition> = listOf(
        PurposeDefinition(
            id = "purpose.identity.pairwise-session",
            category = DataCategory.IDENTITY_PAIRWISE,
            engineeringSummary = "Application session keyed by pairwise subject after official IdP verification.",
        ),
        PurposeDefinition(
            id = "purpose.identity.individual-number-review",
            category = DataCategory.INDIVIDUAL_NUMBER,
            engineeringSummary = "Narrow, reviewed use of the individual number; not a general client identifier.",
        ),
        PurposeDefinition(
            id = "purpose.health.measurements-display",
            category = DataCategory.HEALTH_MEASUREMENTS,
            engineeringSummary = "Display wellness measurements from a user-granted platform source.",
        ),
        PurposeDefinition(
            id = "purpose.clinical.document-review",
            category = DataCategory.CLINICAL_DOCUMENTS,
            engineeringSummary = "On-device review of an uploaded clinical document before any later pipeline.",
        ),
        PurposeDefinition(
            id = "purpose.expenses.export-preview",
            category = DataCategory.EXPENSES,
            engineeringSummary = "Preview aggregated expenses prior to an explicit export confirmation.",
        ),
        PurposeDefinition(
            id = "purpose.telemetry.non-pii-diagnostics",
            category = DataCategory.TELEMETRY,
            engineeringSummary = "Non-PII diagnostics only; disabled until vendor and PII review.",
        ),
    )

    fun purposes(category: DataCategory): List<PurposeDefinition> =
        all.filter { it.category == category }

    fun definition(id: String): PurposeDefinition? =
        all.firstOrNull { it.id == id }
}

enum class SensitiveField(val logKey: String) {
    ACCESS_TOKEN("accessToken"),
    REFRESH_TOKEN("refreshToken"),
    IDENTITY_TOKEN("identityToken"),
    INDIVIDUAL_NUMBER("individualNumber"),
    PAIRWISE_SUBJECT("pairwiseSubject"),
    DIAGNOSIS("diagnosis"),
    PRESCRIPTION("prescription"),
    REPORT_CONTENTS("reportContents"),
    CLINICAL_DOCUMENT("clinicalDocument"),
    POSTAL_ADDRESS("postalAddress"),
    FINANCIAL_ACCOUNT("financialAccount"),
    BIOMETRIC_TEMPLATE("biometricTemplate"),
}

object LogRedaction {
    const val PLACEHOLDER_PREFIX = "[REDACTED]"

    fun replacement(field: SensitiveField): String = "$PLACEHOLDER_PREFIX:${field.logKey}"

    /** Builds a log line that never interpolates sensitive field values. */
    fun sanitize(event: String, fields: Map<SensitiveField, String>): String {
        val redacted = fields.keys
            .sortedBy { it.logKey }
            .joinToString(" ") { "${it.logKey}=${replacement(it)}" }
        return if (redacted.isEmpty()) "event=$event" else "event=$event $redacted"
    }

    fun isForbiddenInLogs(field: SensitiveField): Boolean = true
}

/** Settings placeholders tied to ConsentStore stubs. Not legal notices. */
object PrivacyCopyPlaceholder {
    const val LEGAL_REVIEW_TODO =
        "TODO(product/legal): privacy notices, consent withdrawal, and retention copy are not approved."

    const val CONSENT_STORE_NOT_CONFIGURED =
        "Consent recording is not configured. Engineering purpose summaries are not official notices."

    const val CRASH_AND_TELEMETRY_DISABLED =
        "Crash reporting and telemetry are not configured and do not send events."
}
