"""Unit tests for iOS simulator destination resolution (no simctl required)."""

from __future__ import annotations

import unittest

from ios_simulator_destination import destination_specifier, select_udid


def payload(devices: dict) -> dict:
    return {"devices": devices}


class SimulatorDestinationTests(unittest.TestCase):
    def test_prefers_exact_iphone_15_over_other_iphones(self) -> None:
        data = payload(
            {
                "com.apple.CoreSimulator.SimRuntime.iOS-17-5": [
                    {
                        "name": "iPhone 14",
                        "udid": "UDID-14",
                        "isAvailable": True,
                    },
                    {
                        "name": "iPhone 15",
                        "udid": "UDID-15",
                        "isAvailable": True,
                    },
                    {
                        "name": "iPad (10th generation)",
                        "udid": "UDID-IPAD",
                        "isAvailable": True,
                    },
                ]
            }
        )
        self.assertEqual(select_udid(data)[2], "UDID-15")
        self.assertEqual(destination_specifier("UDID-15"), "platform=iOS Simulator,id=UDID-15")

    def test_skips_unavailable_iphone_15_and_uses_another_iphone(self) -> None:
        data = payload(
            {
                "com.apple.CoreSimulator.SimRuntime.iOS-17-5": [
                    {
                        "name": "iPhone 15",
                        "udid": "UDID-15-UNAVAIL",
                        "isAvailable": False,
                    },
                    {
                        "name": "iPhone 16",
                        "udid": "UDID-16",
                        "isAvailable": True,
                    },
                ]
            }
        )
        self.assertEqual(select_udid(data)[2], "UDID-16")

    def test_picks_highest_runtime_when_name_is_ambiguous(self) -> None:
        data = payload(
            {
                "com.apple.CoreSimulator.SimRuntime.iOS-17-4": [
                    {
                        "name": "iPhone 15",
                        "udid": "UDID-15-17-4",
                        "isAvailable": True,
                    }
                ],
                "com.apple.CoreSimulator.SimRuntime.iOS-17-5": [
                    {
                        "name": "iPhone 15",
                        "udid": "UDID-15-17-5",
                        "isAvailable": True,
                    }
                ],
            }
        )
        self.assertEqual(select_udid(data)[2], "UDID-15-17-5")

    def test_fails_when_only_ipads_are_available(self) -> None:
        data = payload(
            {
                "com.apple.CoreSimulator.SimRuntime.iOS-17-5": [
                    {
                        "name": "iPad Air 11-inch (M2)",
                        "udid": "UDID-IPAD",
                        "isAvailable": True,
                    }
                ]
            }
        )
        with self.assertRaises(SystemExit):
            select_udid(data)


if __name__ == "__main__":
    unittest.main()
