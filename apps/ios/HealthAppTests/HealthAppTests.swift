import XCTest
@testable import HealthApp

final class HealthAppTests: XCTestCase {
    func testDisplayLabelMapsAdapterStatuses() {
        XCTAssertEqual(displayLabel(for: .notConfigured), "Not configured")
        XCTAssertEqual(displayLabel(for: .unavailable), "Unavailable")
        XCTAssertEqual(displayLabel(for: .ready), "Ready")
        XCTAssertEqual(displayLabel(for: .permissionRequired), "Permission required")
    }

    func testCompositionRootWiresNotConfiguredAdapters() {
        let dependencies = CompositionRoot.make()

        XCTAssertEqual(dependencies.secureStore.applicationStoreStatus, .notConfigured)
        XCTAssertEqual(dependencies.secureStore.biometricGatedStoreStatus, .notConfigured)
        XCTAssertEqual(dependencies.biometricUnlock.status, .notConfigured)
        XCTAssertEqual(dependencies.healthDataSource.status, .notConfigured)
        XCTAssertEqual(dependencies.nfcCapability.status, .notConfigured)
        XCTAssertEqual(dependencies.identitySession.status, .notConfigured)
        XCTAssertNil(dependencies.identitySession.pairwiseSubject())
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
    }
}
