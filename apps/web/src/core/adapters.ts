import type {
  CapabilityAvailability,
  FeatureFlagState,
  PermissionState,
  PipelineStatus,
  SessionStatus,
  SyncStatus,
} from "./CapabilityStatus";

export interface HealthCapabilitySnapshot {
  availability: CapabilityAvailability;
  permissionState: PermissionState;
  grantedReadScopes: string[];
}

export interface SecureStore {
  readonly status: CapabilityAvailability;
}

export interface LocalUnlock {
  readonly status: CapabilityAvailability;
}

export interface HealthDataSource {
  capabilitySnapshot(): HealthCapabilitySnapshot;
}

export interface WellnessSyncClient {
  readonly status: SyncStatus;
  readonly featureFlag: FeatureFlagState;
}

export interface NfcCapability {
  readonly status: CapabilityAvailability;
}

export interface IdentitySession {
  readonly status: SessionStatus;
}

export interface ConsentStore {
  readonly status: CapabilityAvailability;
}

export interface CrashReporter {
  readonly status: CapabilityAvailability;
}

export interface TelemetryPolicy {
  readonly status: CapabilityAvailability;
}

export interface FileValidation {
  readonly status: CapabilityAvailability;
}

export interface QuarantineStore {
  readonly status: CapabilityAvailability;
}

export interface ReportUploadClient {
  readonly status: CapabilityAvailability;
}

export interface ClinicalDocumentPipeline {
  readonly status: PipelineStatus;
}

export interface AppDependencies {
  secureStore: SecureStore;
  localUnlock: LocalUnlock;
  healthDataSource: HealthDataSource;
  wellnessSync: WellnessSyncClient;
  nfcCapability: NfcCapability;
  identitySession: IdentitySession;
  consentStore: ConsentStore;
  crashReporter: CrashReporter;
  telemetryPolicy: TelemetryPolicy;
  fileValidation: FileValidation;
  quarantineStore: QuarantineStore;
  reportUpload: ReportUploadClient;
  clinicalPipeline: ClinicalDocumentPipeline;
}
