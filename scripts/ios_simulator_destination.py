#!/usr/bin/env python3
"""Resolve a concrete iOS Simulator destination for xcodebuild test.

macos-14 GitHub-hosted images have drifted: some ship an `iPhone 15` runtime,
later images may omit that exact name or list several OS versions under it.
Matching `name=iPhone 15` alone is therefore flaky.

This script prefers an available device named exactly `iPhone 15`, then any
other available iPhone, and prints `platform=iOS Simulator,id=<udid>` so tests
still run on a real simulator. It never falls back to
`generic/platform=iOS Simulator`, which cannot execute tests.
"""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from pathlib import Path
from typing import Any

PREFERRED_DEVICE_NAME = "iPhone 15"
IPHONE_NAME = re.compile(r"^iPhone")
RUNTIME_VERSION = re.compile(r"(\d+)[^\d]+(\d+)(?:[^\d]+(\d+))?")


def runtime_sort_key(runtime_id: str) -> tuple[int, int, int]:
    match = RUNTIME_VERSION.search(runtime_id)
    if not match:
        return (0, 0, 0)
    major, minor, patch = match.group(1), match.group(2), match.group(3) or "0"
    return (int(major), int(minor), int(patch))


def available_iphones(payload: dict[str, Any]) -> list[tuple[tuple[int, int, int], str, str, str]]:
    """Return (runtime_version, name, udid, runtime_id) for available iPhones."""
    found: list[tuple[tuple[int, int, int], str, str, str]] = []
    devices = payload.get("devices") or {}
    for runtime_id, entries in devices.items():
        version = runtime_sort_key(str(runtime_id))
        for entry in entries or []:
            name = str(entry.get("name") or "")
            udid = str(entry.get("udid") or "")
            if not name or not udid:
                continue
            if not IPHONE_NAME.match(name):
                continue
            if entry.get("isAvailable") is False:
                continue
            found.append((version, name, udid, str(runtime_id)))
    return found


def select_udid(payload: dict[str, Any]) -> tuple[str, str, str]:
    """Return (name, runtime_id, udid)."""
    iphones = available_iphones(payload)
    if not iphones:
        raise SystemExit("No available iPhone simulator was found.")

    preferred = [item for item in iphones if item[1] == PREFERRED_DEVICE_NAME]
    pool = preferred or iphones
    # Highest runtime version, then stable name, then udid.
    _, name, udid, runtime_id = max(pool, key=lambda item: (item[0], item[1], item[2]))
    return name, runtime_id, udid


def destination_specifier(udid: str) -> str:
    return f"platform=iOS Simulator,id={udid}"


def load_simctl_devices() -> dict[str, Any]:
    result = subprocess.run(
        ["xcrun", "simctl", "list", "devices", "available", "-j"],
        check=True,
        capture_output=True,
        text=True,
    )
    return json.loads(result.stdout)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--devices-json",
        help="Path to a simctl-style devices JSON file (for tests). Default: call simctl.",
    )
    args = parser.parse_args(argv)

    if args.devices_json:
        payload = json.loads(Path(args.devices_json).read_text(encoding="utf-8"))
    else:
        payload = load_simctl_devices()

    name, runtime_id, udid = select_udid(payload)
    print(f"Resolved iOS simulator {name} ({runtime_id}) -> {udid}", file=sys.stderr)
    print(destination_specifier(udid))
    return 0


if __name__ == "__main__":
    sys.exit(main())
