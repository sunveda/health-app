import Foundation

/// Intended later platform read scopes. Stubs never request these from HealthKit or Health Connect.
enum WellnessReadScope: String, CaseIterable, Hashable {
    case steps
    case heartRate
}

/// Permission lane distinct from adapter availability (`CapabilityStatus`).
enum HealthPermissionState: Equatable {
    case notConfigured
    case unavailable
    case notDetermined
    case denied
    case authorized
}

/// Shipping default is disabled until kit adapters are reviewed and wired by CompositionRoot.
enum WellnessFeatureFlag: Equatable {
    case disabled
    case enabled

    var isEnabled: Bool { self == .enabled }

    static let shipping: WellnessFeatureFlag = .disabled
}

/// Contract `wellnessSync.status` values from `packages/contracts`.
enum WellnessContractStatus: String, Equatable {
    case notConnected = "not_connected"
    case connected = "connected"
    case permissionDenied = "permission_denied"
    case error = "error"
}

struct WellnessCapabilitySnapshot: Equatable {
    let availability: CapabilityStatus
    let permissionState: HealthPermissionState
    let grantedReadScopes: Set<WellnessReadScope>
    let lastFailure: CapabilityError?

    static let notConfigured = WellnessCapabilitySnapshot(
        availability: .notConfigured,
        permissionState: .notConfigured,
        grantedReadScopes: [],
        lastFailure: .notConfigured
    )

    static let unavailable = WellnessCapabilitySnapshot(
        availability: .unavailable,
        permissionState: .unavailable,
        grantedReadScopes: [],
        lastFailure: .unavailable
    )
}

enum WellnessContractMapping {
    static func status(
        availability: CapabilityStatus,
        permissionState: HealthPermissionState,
        lastSyncedAt: Date?,
        lastFailure: CapabilityError?
    ) -> WellnessContractStatus {
        if permissionState == .denied {
            return .permissionDenied
        }
        if availability == .unavailable || lastFailure == .unavailable {
            return .error
        }
        if availability == .ready,
           permissionState == .authorized,
           lastSyncedAt != nil,
           lastFailure == nil
        {
            return .connected
        }
        return .notConnected
    }

    static func status(from snapshot: WellnessCapabilitySnapshot, lastSyncedAt: Date?) -> WellnessContractStatus {
        status(
            availability: snapshot.availability,
            permissionState: snapshot.permissionState,
            lastSyncedAt: lastSyncedAt,
            lastFailure: snapshot.lastFailure
        )
    }
}

func displayLabel(forPermission permission: HealthPermissionState) -> String {
    switch permission {
    case .notConfigured:
        return "Not configured"
    case .unavailable:
        return "Unavailable"
    case .notDetermined:
        return "Not determined"
    case .denied:
        return "Denied"
    case .authorized:
        return "Authorized"
    }
}

func displayLabel(forFeatureFlag flag: WellnessFeatureFlag) -> String {
    flag.isEnabled ? "Enabled" : "Disabled"
}

enum WellnessPresentation {
    static func scopesLabel(_ scopes: Set<WellnessReadScope>) -> String {
        let names = WellnessReadScope.allCases.filter { scopes.contains($0) }.map(displayName(for:))
        return names.isEmpty ? "None" : names.joined(separator: ", ")
    }

    static func displayName(for scope: WellnessReadScope) -> String {
        switch scope {
        case .steps:
            return "Steps"
        case .heartRate:
            return "Heart rate"
        }
    }
}

/// Settings / Health placeholders. Not a claim that Wellness is complete.
enum WellnessCopyPlaceholder {
    static let kitWiringDeferred =
        "HealthKit and Health Connect are not configured. Permission requests and reads are deferred."

    static let featureDisabled =
        "Wellness sync is disabled until adapters are reviewed and the feature flag is enabled."
}
