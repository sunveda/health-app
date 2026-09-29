export type CapabilityAvailability =
  | "not_configured"
  | "unavailable"
  | "available";

export type PermissionState = "not_determined" | "denied" | "granted";

export type FeatureFlagState = "disabled" | "enabled";

export type SessionStatus =
  | "not_configured"
  | "signed_out"
  | "signed_in";

export type SyncStatus =
  | "not_configured"
  | "not_connected"
  | "connected"
  | "permission_denied"
  | "error";

export type PipelineStatus =
  | "not_configured"
  | "idle"
  | "processing"
  | "ready_for_review"
  | "error";

export function displayAvailability(value: CapabilityAvailability): string {
  switch (value) {
    case "not_configured":
      return "Not configured";
    case "unavailable":
      return "Unavailable on web";
    case "available":
      return "Available";
  }
}

export function displaySession(value: SessionStatus): string {
  switch (value) {
    case "not_configured":
      return "Not configured";
    case "signed_out":
      return "Signed out";
    case "signed_in":
      return "Signed in";
  }
}

export function displaySync(value: SyncStatus): string {
  switch (value) {
    case "not_configured":
      return "Not configured";
    case "not_connected":
      return "Not connected";
    case "connected":
      return "Connected";
    case "permission_denied":
      return "Permission denied";
    case "error":
      return "Error";
  }
}

export function displayPipeline(value: PipelineStatus): string {
  switch (value) {
    case "not_configured":
      return "Not configured";
    case "idle":
      return "Idle";
    case "processing":
      return "Processing";
    case "ready_for_review":
      return "Ready for review";
    case "error":
      return "Error";
  }
}

export function displayFeatureFlag(value: FeatureFlagState): string {
  return value === "enabled" ? "Enabled" : "Disabled";
}
