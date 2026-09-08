import Foundation

/// Product data categories for consent/purpose scaffolding. Not a legal determination.
enum DataCategory: String, CaseIterable, Equatable {
    case identityPairwise
    case individualNumber
    case healthMeasurements
    case clinicalDocuments
    case expenses
    case telemetry
}

enum ConsentDecision: Equatable {
    case granted
    case denied
    case notRecorded
}

/// Engineering purpose id plus a non-legal summary. User-facing copy is TODO for product/legal.
struct PurposeDefinition: Equatable {
    let id: String
    let category: DataCategory
    let engineeringSummary: String
}

enum PurposeRegistry {
    static let all: [PurposeDefinition] = [
        PurposeDefinition(
            id: "purpose.identity.pairwise-session",
            category: .identityPairwise,
            engineeringSummary: "Application session keyed by pairwise subject after official IdP verification."
        ),
        PurposeDefinition(
            id: "purpose.identity.individual-number-review",
            category: .individualNumber,
            engineeringSummary: "Narrow, reviewed use of the individual number; not a general client identifier."
        ),
        PurposeDefinition(
            id: "purpose.health.measurements-display",
            category: .healthMeasurements,
            engineeringSummary: "Display wellness measurements from a user-granted platform source."
        ),
        PurposeDefinition(
            id: "purpose.clinical.document-review",
            category: .clinicalDocuments,
            engineeringSummary: "On-device review of an uploaded clinical document before any later pipeline."
        ),
        PurposeDefinition(
            id: "purpose.expenses.export-preview",
            category: .expenses,
            engineeringSummary: "Preview aggregated expenses prior to an explicit export confirmation."
        ),
        PurposeDefinition(
            id: "purpose.telemetry.non-pii-diagnostics",
            category: .telemetry,
            engineeringSummary: "Non-PII diagnostics only; disabled until vendor and PII review."
        ),
    ]

    static func purposes(for category: DataCategory) -> [PurposeDefinition] {
        all.filter { $0.category == category }
    }

    static func definition(id: String) -> PurposeDefinition? {
        all.first { $0.id == id }
    }
}

enum SensitiveField: String, CaseIterable, Equatable {
    case accessToken
    case refreshToken
    case identityToken
    case individualNumber
    case pairwiseSubject
    case diagnosis
    case prescription
    case reportContents
    case clinicalDocument
    case postalAddress
    case financialAccount
    case biometricTemplate
    case healthSample
}

enum LogRedaction {
    static let placeholderPrefix = "[REDACTED]"

    static func replacement(for field: SensitiveField) -> String {
        "\(placeholderPrefix):\(field.rawValue)"
    }

    /// Builds a log line that never interpolates sensitive field values.
    static func sanitize(event: String, fields: [SensitiveField: String]) -> String {
        let redacted = fields.keys.sorted { $0.rawValue < $1.rawValue }
            .map { "\($0.rawValue)=\(replacement(for: $0))" }
            .joined(separator: " ")
        if redacted.isEmpty {
            return "event=\(event)"
        }
        return "event=\(event) \(redacted)"
    }

    static func isForbiddenInLogs(_ field: SensitiveField) -> Bool {
        true
    }
}

/// Settings placeholders tied to ConsentStore stubs. Not legal notices.
enum PrivacyCopyPlaceholder {
    static let legalReviewTodo =
        "TODO(product/legal): privacy notices, consent withdrawal, and retention copy are not approved."

    static let consentStoreNotConfigured =
        "Consent recording is not configured. Engineering purpose summaries are not official notices."

    static let crashAndTelemetryDisabled =
        "Crash reporting and telemetry are not configured and do not send events."
}
