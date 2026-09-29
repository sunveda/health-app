import type {
  ClinicalDocumentPipeline,
  ConsentStore,
  CrashReporter,
  FileValidation,
  HealthDataSource,
  IdentitySession,
  LocalUnlock,
  NfcCapability,
  QuarantineStore,
  ReportUploadClient,
  SecureStore,
  TelemetryPolicy,
  WellnessSyncClient,
} from "./adapters";

/** Browser secure storage is not wired; never put tokens in localStorage. */
export class NotConfiguredSecureStore implements SecureStore {
  readonly status = "not_configured" as const;
}

/** WebAuthn / passkeys only after Identity is approved. */
export class NotConfiguredLocalUnlock implements LocalUnlock {
  readonly status = "not_configured" as const;
}

/** HealthKit / Health Connect are mobile-only. */
export class UnavailableHealthDataSource implements HealthDataSource {
  capabilitySnapshot() {
    return {
      availability: "unavailable" as const,
      permissionState: "not_determined" as const,
      grantedReadScopes: [] as string[],
    };
  }
}

export class NotConfiguredWellnessSyncClient implements WellnessSyncClient {
  readonly status = "not_configured" as const;
  readonly featureFlag = "disabled" as const;
}

/** NFC card reading is not a web portal capability. */
export class UnavailableNfcCapability implements NfcCapability {
  readonly status = "unavailable" as const;
}

export class NotConfiguredIdentitySession implements IdentitySession {
  readonly status = "not_configured" as const;
}

export class NotConfiguredConsentStore implements ConsentStore {
  readonly status = "not_configured" as const;
}

export class NotConfiguredCrashReporter implements CrashReporter {
  readonly status = "not_configured" as const;
}

export class NotConfiguredTelemetryPolicy implements TelemetryPolicy {
  readonly status = "not_configured" as const;
}

export class NotConfiguredFileValidation implements FileValidation {
  readonly status = "not_configured" as const;
}

export class NotConfiguredQuarantineStore implements QuarantineStore {
  readonly status = "not_configured" as const;
}

export class NotConfiguredReportUploadClient implements ReportUploadClient {
  readonly status = "not_configured" as const;
}

export class NotConfiguredClinicalDocumentPipeline
  implements ClinicalDocumentPipeline
{
  readonly status = "not_configured" as const;
}
