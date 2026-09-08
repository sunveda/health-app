import Foundation

/// Shared availability state for Core/platform adapters.
/// Screens must render these states; they must not infer capability from device APIs.
enum CapabilityStatus: Equatable {
    case notConfigured
    case unavailable
    case ready
    case permissionRequired
}

enum CapabilityError: Error, Equatable {
    case notConfigured
    case unavailable
}

func displayLabel(for status: CapabilityStatus) -> String {
    switch status {
    case .notConfigured:
        return "Not configured"
    case .unavailable:
        return "Unavailable"
    case .ready:
        return "Ready"
    case .permissionRequired:
        return "Permission required"
    }
}
