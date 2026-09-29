import type { AppDependencies } from "./adapters";
import {
  NotConfiguredClinicalDocumentPipeline,
  NotConfiguredConsentStore,
  NotConfiguredCrashReporter,
  NotConfiguredFileValidation,
  NotConfiguredIdentitySession,
  NotConfiguredLocalUnlock,
  NotConfiguredQuarantineStore,
  NotConfiguredReportUploadClient,
  NotConfiguredSecureStore,
  NotConfiguredTelemetryPolicy,
  NotConfiguredWellnessSyncClient,
  UnavailableHealthDataSource,
  UnavailableNfcCapability,
} from "./StubAdapters";

/** Single composition root. Feature screens must not construct adapters. */
export function createCompositionRoot(): AppDependencies {
  return {
    secureStore: new NotConfiguredSecureStore(),
    localUnlock: new NotConfiguredLocalUnlock(),
    healthDataSource: new UnavailableHealthDataSource(),
    wellnessSync: new NotConfiguredWellnessSyncClient(),
    nfcCapability: new UnavailableNfcCapability(),
    identitySession: new NotConfiguredIdentitySession(),
    consentStore: new NotConfiguredConsentStore(),
    crashReporter: new NotConfiguredCrashReporter(),
    telemetryPolicy: new NotConfiguredTelemetryPolicy(),
    fileValidation: new NotConfiguredFileValidation(),
    quarantineStore: new NotConfiguredQuarantineStore(),
    reportUpload: new NotConfiguredReportUploadClient(),
    clinicalPipeline: new NotConfiguredClinicalDocumentPipeline(),
  };
}
