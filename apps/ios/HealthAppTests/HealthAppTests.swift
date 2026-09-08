import XCTest
@testable import HealthApp

final class HealthAppTests: XCTestCase {
    func testDisplayLabelMapsAdapterStatuses() {
        XCTAssertEqual(displayLabel(for: CapabilityStatus.notConfigured), "Not configured")
        XCTAssertEqual(displayLabel(for: CapabilityStatus.unavailable), "Unavailable")
        XCTAssertEqual(displayLabel(for: CapabilityStatus.ready), "Ready")
        XCTAssertEqual(displayLabel(for: CapabilityStatus.permissionRequired), "Permission required")
    }

    func testCompositionRootWiresNotConfiguredAdapters() {
        let dependencies = CompositionRoot.make()

        XCTAssertEqual(dependencies.secureStore.applicationStoreStatus, .notConfigured)
        XCTAssertEqual(dependencies.secureStore.biometricGatedStoreStatus, .notConfigured)
        XCTAssertEqual(dependencies.biometricUnlock.status, .notConfigured)
        XCTAssertEqual(dependencies.healthDataSource.status, .notConfigured)
        XCTAssertEqual(dependencies.healthDataSource.permissionState, .notConfigured)
        XCTAssertTrue(dependencies.healthDataSource.grantedReadScopes.isEmpty)
        XCTAssertEqual(dependencies.healthDataSource.lastFailure, .notConfigured)
        XCTAssertEqual(dependencies.wellnessSync.status, .notConfigured)
        XCTAssertEqual(dependencies.wellnessSync.featureFlag, .disabled)
        XCTAssertFalse(dependencies.wellnessSync.featureFlag.isEnabled)
        XCTAssertEqual(WellnessFeatureFlag.shipping, .disabled)
        XCTAssertNil(dependencies.wellnessSync.lastSyncedAt)
        XCTAssertEqual(dependencies.nfcCapability.status, .notConfigured)
        XCTAssertEqual(dependencies.identitySession.status, .notConfigured)
        XCTAssertNil(dependencies.identitySession.pairwiseSubject())
        XCTAssertEqual(dependencies.consentStore.status, .notConfigured)
        XCTAssertEqual(dependencies.consentStore.decision(for: .healthMeasurements), .notRecorded)
        XCTAssertEqual(dependencies.crashReporter.status, .notConfigured)
        XCTAssertEqual(dependencies.telemetryPolicy.status, .notConfigured)
        XCTAssertFalse(dependencies.telemetryPolicy.isAllowed(.telemetry))
    }

    func testSecureStoreStubsDistinguishLanesAndRejectReads() {
        let store = NotConfiguredSecureStore()
        XCTAssertEqual(store.applicationStoreStatus, .notConfigured)
        XCTAssertEqual(store.biometricGatedStoreStatus, .notConfigured)

        XCTAssertEqual(store.readApplicationValue(forKey: "session"), .failure(.notConfigured))
        XCTAssertEqual(store.readBiometricGatedValue(forKey: "session"), .failure(.notConfigured))

        let unavailable = UnavailableSecureStore()
        XCTAssertEqual(unavailable.applicationStoreStatus, .unavailable)
        XCTAssertEqual(unavailable.biometricGatedStoreStatus, .unavailable)
        XCTAssertEqual(unavailable.readApplicationValue(forKey: "session"), .failure(.unavailable))
        XCTAssertEqual(unavailable.readBiometricGatedValue(forKey: "session"), .failure(.unavailable))
    }

    func testRootViewAcceptsCompositionRootDependencies() {
        let dependencies = CompositionRoot.make()
        _ = RootView(dependencies: dependencies)
        XCTAssertEqual(displayLabel(for: dependencies.healthDataSource.status), "Not configured")
        XCTAssertEqual(displayLabel(for: dependencies.wellnessSync.status), "Not configured")
        XCTAssertEqual(displayLabel(for: dependencies.consentStore.status), "Not configured")
        XCTAssertEqual(displayLabel(for: dependencies.crashReporter.status), "Not configured")
    }

    func testPurposeRegistryCoversEveryDataCategoryOnce() {
        let categories = Set(PurposeRegistry.all.map(\.category))
        XCTAssertEqual(categories, Set(DataCategory.allCases))
        XCTAssertEqual(PurposeRegistry.all.count, DataCategory.allCases.count)

        let ids = PurposeRegistry.all.map(\.id)
        XCTAssertEqual(Set(ids).count, ids.count)

        for category in DataCategory.allCases {
            XCTAssertEqual(PurposeRegistry.purposes(for: category).count, 1)
        }

        XCTAssertEqual(
            PurposeRegistry.definition(id: "purpose.identity.individual-number-review")?.category,
            .individualNumber
        )
        XCTAssertNil(PurposeRegistry.definition(id: "purpose.does-not-exist"))
    }

    func testLogRedactionNeverInterpolatesSensitiveValues() {
        let leakedNumber = "123456789012"
        let leakedToken = "supersecrettokenvalue"
        let message = LogRedaction.sanitize(
            event: "session-start",
            fields: [
                .individualNumber: leakedNumber,
                .accessToken: leakedToken,
                .diagnosis: "synthetic-diagnosis-label",
            ]
        )

        XCTAssertTrue(message.hasPrefix("event=session-start"))
        XCTAssertFalse(message.contains(leakedNumber))
        XCTAssertFalse(message.contains(leakedToken))
        XCTAssertFalse(message.contains("synthetic-diagnosis-label"))
        XCTAssertTrue(message.contains(LogRedaction.replacement(for: .individualNumber)))
        XCTAssertTrue(message.contains(LogRedaction.replacement(for: .accessToken)))
        XCTAssertEqual(LogRedaction.sanitize(event: "noop", fields: [:]), "event=noop")

        for field in SensitiveField.allCases {
            XCTAssertTrue(LogRedaction.isForbiddenInLogs(field))
        }
    }

    func testNotConfiguredPrivacyAdaptersRejectWritesAndDenyTelemetry() {
        let consent = NotConfiguredConsentStore()
        XCTAssertEqual(consent.status, .notConfigured)
        XCTAssertEqual(consent.decision(for: .clinicalDocuments), .notRecorded)
        assertFailure(
            consent.record(decision: .granted, for: .clinicalDocuments, purposeId: "purpose.clinical.document-review"),
            .notConfigured
        )

        let crash = NotConfiguredCrashReporter()
        XCTAssertEqual(crash.status, .notConfigured)
        assertFailure(crash.captureNonPII(event: "launch"), .notConfigured)

        let telemetry = NotConfiguredTelemetryPolicy()
        XCTAssertEqual(telemetry.status, .notConfigured)
        for category in DataCategory.allCases {
            XCTAssertFalse(telemetry.isAllowed(category))
        }
    }

    func testInMemoryConsentStoreRecordsGrantedAndDeniedWithoutPayloads() {
        let store = InMemoryConsentStore()
        XCTAssertEqual(store.status, .ready)
        XCTAssertEqual(store.decision(for: .expenses), .notRecorded)

        assertSuccess(
            store.record(
                decision: .granted,
                for: .expenses,
                purposeId: "purpose.expenses.export-preview"
            )
        )
        XCTAssertEqual(store.decision(for: .expenses), .granted)

        assertSuccess(
            store.record(
                decision: .denied,
                for: .healthMeasurements,
                purposeId: "purpose.health.measurements-display"
            )
        )
        XCTAssertEqual(store.decision(for: .healthMeasurements), .denied)

        assertFailure(
            store.record(
                decision: .granted,
                for: .telemetry,
                purposeId: "purpose.expenses.export-preview"
            ),
            .unavailable
        )
        XCTAssertEqual(store.decision(for: .telemetry), .notRecorded)
    }

    func testPrivacyCopyPlaceholdersStayTodosWithoutLegalClaims() {
        let copy = [
            PrivacyCopyPlaceholder.legalReviewTodo,
            PrivacyCopyPlaceholder.consentStoreNotConfigured,
            PrivacyCopyPlaceholder.crashAndTelemetryDisabled,
        ]
        for line in copy {
            XCTAssertFalse(line.isEmpty)
        }
        XCTAssertTrue(PrivacyCopyPlaceholder.legalReviewTodo.contains("TODO(product/legal)"))
        let joined = copy.joined(separator: " ").lowercased()
        XCTAssertFalse(joined.contains("個人情報保護法"))
        XCTAssertFalse(joined.contains("番号法"))
        XCTAssertFalse(joined.contains("appi"))
    }

    func testWellnessContractMappingCoversStubAndLaterStates() {
        XCTAssertEqual(
            WellnessContractMapping.status(from: .notConfigured, lastSyncedAt: nil),
            .notConnected
        )
        XCTAssertEqual(
            WellnessContractMapping.status(from: .unavailable, lastSyncedAt: nil),
            .error
        )

        let denied = WellnessCapabilitySnapshot(
            availability: .permissionRequired,
            permissionState: .denied,
            grantedReadScopes: [],
            lastFailure: nil
        )
        XCTAssertEqual(WellnessContractMapping.status(from: denied, lastSyncedAt: nil), .permissionDenied)

        let readyUnsynced = WellnessCapabilitySnapshot(
            availability: .ready,
            permissionState: .authorized,
            grantedReadScopes: [.steps],
            lastFailure: nil
        )
        XCTAssertEqual(WellnessContractMapping.status(from: readyUnsynced, lastSyncedAt: nil), .notConnected)
        XCTAssertEqual(
            WellnessContractMapping.status(from: readyUnsynced, lastSyncedAt: Date(timeIntervalSince1970: 1)),
            .connected
        )

        let failedReady = WellnessCapabilitySnapshot(
            availability: .ready,
            permissionState: .authorized,
            grantedReadScopes: [.steps, .heartRate],
            lastFailure: .unavailable
        )
        XCTAssertEqual(
            WellnessContractMapping.status(from: failedReady, lastSyncedAt: Date(timeIntervalSince1970: 1)),
            .error
        )
    }

    func testWellnessPresentationAndPermissionLabels() {
        XCTAssertEqual(WellnessPresentation.scopesLabel([]), "None")
        XCTAssertEqual(WellnessPresentation.scopesLabel([.heartRate]), "Heart rate")
        XCTAssertEqual(WellnessPresentation.scopesLabel([.heartRate, .steps]), "Steps, Heart rate")
        XCTAssertEqual(displayLabel(forPermission: .notConfigured), "Not configured")
        XCTAssertEqual(displayLabel(forPermission: .denied), "Denied")
        XCTAssertEqual(displayLabel(forPermission: .authorized), "Authorized")
        XCTAssertEqual(displayLabel(forFeatureFlag: .disabled), "Disabled")
        XCTAssertEqual(displayLabel(forFeatureFlag: .enabled), "Enabled")
    }

    func testNotConfiguredWellnessAdaptersRejectReadsAndKeepEmptyScopes() {
        let source = NotConfiguredHealthDataSource()
        XCTAssertEqual(source.status, .notConfigured)
        XCTAssertEqual(source.permissionState, .notConfigured)
        XCTAssertTrue(source.grantedReadScopes.isEmpty)
        XCTAssertEqual(source.lastFailure, .notConfigured)
        XCTAssertEqual(source.capabilitySnapshot(), .notConfigured)
        assertFailure(source.requestReadAccess(scopes: []), .notConfigured)
        assertFailure(source.requestReadAccess(scopes: [.steps, .heartRate]), .notConfigured)
        XCTAssertTrue(source.grantedReadScopes.isEmpty)

        let unavailable = UnavailableHealthDataSource()
        XCTAssertEqual(unavailable.capabilitySnapshot(), .unavailable)
        assertFailure(unavailable.requestReadAccess(scopes: [.steps]), .unavailable)

        let sync = NotConfiguredWellnessSyncClient()
        XCTAssertEqual(sync.status, .notConfigured)
        XCTAssertEqual(sync.featureFlag, .disabled)
        XCTAssertNil(sync.lastSyncedAt)
        assertFailure(sync.sync(), .notConfigured)
        XCTAssertEqual(
            WellnessContractMapping.status(from: source.capabilitySnapshot(), lastSyncedAt: sync.lastSyncedAt),
            .notConnected
        )

        let unavailableSync = UnavailableWellnessSyncClient()
        assertFailure(unavailableSync.sync(), .unavailable)
    }

    func testWellnessCopyDoesNotClaimKitWiringOrCompletedStages() {
        let copy = [
            WellnessCopyPlaceholder.kitWiringDeferred,
            WellnessCopyPlaceholder.featureDisabled,
        ]
        for line in copy {
            XCTAssertFalse(line.isEmpty)
        }
        let joined = copy.joined(separator: " ").lowercased()
        XCTAssertTrue(joined.contains("not configured") || joined.contains("disabled"))
        XCTAssertTrue(joined.contains("deferred") || joined.contains("disabled"))
        XCTAssertFalse(joined.contains("connected to healthkit"))
        XCTAssertFalse(joined.contains("identity complete"))
        XCTAssertFalse(joined.contains("clinical complete"))
    }
}

/// `Result<Void, _>` is not `Equatable` on the Swift 5.9 / Xcode 15 CI toolchain.
private func assertSuccess(
    _ result: Result<Void, CapabilityError>,
    file: StaticString = #filePath,
    line: UInt = #line
) {
    if case .success = result {
        return
    }
    XCTFail("expected success, got \(String(describing: result))", file: file, line: line)
}

private func assertFailure(
    _ result: Result<Void, CapabilityError>,
    _ expected: CapabilityError,
    file: StaticString = #filePath,
    line: UInt = #line
) {
    switch result {
    case .failure(let error):
        XCTAssertEqual(error, expected, file: file, line: line)
    case .success:
        XCTFail("expected failure \(expected), got success", file: file, line: line)
    }
}
