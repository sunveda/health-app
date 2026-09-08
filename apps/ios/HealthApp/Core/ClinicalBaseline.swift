import Foundation

/// Intended later pipeline stages. Stubs never advance these.
enum ClinicalPipelineStage: String, CaseIterable, Hashable {
    case selection
    case validation
    case quarantine
    case upload
    case review
    case deletion
}

/// Engineering file-type labels only. Not a MIME scanner vendor list.
enum ClinicalDeclaredType: String, CaseIterable, Hashable {
    case pdf
    case image
}

/// Metadata-only descriptor. Never carries file bytes or real medical documents.
struct ClinicalFileDescriptor: Equatable {
    let documentId: String
    let declaredType: ClinicalDeclaredType
    let byteSize: Int
}

/// Local surface status for shells. There is no clinical wire contract yet.
enum ClinicalSurfaceStatus: String, Equatable {
    case notConfigured = "not_configured"
    case unavailable = "unavailable"
    case idle = "idle"
    case inProgress = "in_progress"
}

struct ClinicalCapabilitySnapshot: Equatable {
    let availability: CapabilityStatus
    let lastFailure: CapabilityError?
    let activeStage: ClinicalPipelineStage?

    static let notConfigured = ClinicalCapabilitySnapshot(
        availability: .notConfigured,
        lastFailure: .notConfigured,
        activeStage: nil
    )

    static let unavailable = ClinicalCapabilitySnapshot(
        availability: .unavailable,
        lastFailure: .unavailable,
        activeStage: nil
    )
}

enum ClinicalSurfaceMapping {
    static func status(
        availability: CapabilityStatus,
        lastFailure: CapabilityError?,
        activeStage: ClinicalPipelineStage?
    ) -> ClinicalSurfaceStatus {
        if availability == .unavailable || lastFailure == .unavailable {
            return .unavailable
        }
        if availability == .notConfigured || lastFailure == .notConfigured {
            return .notConfigured
        }
        if availability == .ready, lastFailure == nil {
            return activeStage == nil ? .idle : .inProgress
        }
        return .notConfigured
    }

    static func status(from snapshot: ClinicalCapabilitySnapshot) -> ClinicalSurfaceStatus {
        status(
            availability: snapshot.availability,
            lastFailure: snapshot.lastFailure,
            activeStage: snapshot.activeStage
        )
    }
}

func displayLabel(forSurface status: ClinicalSurfaceStatus) -> String {
    switch status {
    case .notConfigured:
        return "Not configured"
    case .unavailable:
        return "Unavailable"
    case .idle:
        return "Idle"
    case .inProgress:
        return "In progress"
    }
}

func displayLabel(forPipelineStage stage: ClinicalPipelineStage) -> String {
    switch stage {
    case .selection:
        return "Selection"
    case .validation:
        return "Validation"
    case .quarantine:
        return "Quarantine"
    case .upload:
        return "Upload"
    case .review:
        return "Review"
    case .deletion:
        return "Deletion"
    }
}

enum ClinicalPresentation {
    static func stagesLabel(_ stages: Set<ClinicalPipelineStage>) -> String {
        let names = ClinicalPipelineStage.allCases.filter { stages.contains($0) }.map {
            displayLabel(forPipelineStage: $0)
        }
        return names.isEmpty ? "None" : names.joined(separator: ", ")
    }

    static func activeStageLabel(_ stage: ClinicalPipelineStage?) -> String {
        guard let stage else { return "None" }
        return displayLabel(forPipelineStage: stage)
    }
}

/// Settings / Health placeholders. Not a claim that Clinical is complete.
enum ClinicalCopyPlaceholder {
    static let pipelineNotConfigured =
        "Clinical document upload and review are not configured. Document pickers and network uploads are deferred."

    static let noUploadEndpoint =
        "No upload endpoint is configured. File validation, quarantine, and deletion remain unavailable."
}
