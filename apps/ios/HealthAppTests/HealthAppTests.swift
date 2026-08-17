import XCTest
@testable import HealthApp

final class HealthAppTests: XCTestCase {
    func testRootViewCanBeConstructed() {
        _ = RootView()
        XCTAssertTrue(true)
    }
}
