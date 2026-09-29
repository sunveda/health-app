#!/usr/bin/env python3
"""Fail CI when Expo leftovers, secrets, or device-kit APIs leak outside Core/platform adapters."""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

ADAPTER_ALLOWLIST_PREFIXES = (
    "apps/ios/HealthApp/Core/",
    "apps/android/app/src/main/java/com/sunveda/healthapp/platform/",
    "apps/web/src/core/",
)

CONSTRUCTOR_ALLOWED_PATHS = (
    "apps/ios/HealthApp/Core/CompositionRoot.swift",
    "apps/android/app/src/main/java/com/sunveda/healthapp/platform/CompositionRoot.kt",
    "apps/web/src/core/CompositionRoot.ts",
)

CODE_SUFFIXES = (
    ".swift",
    ".kt",
    ".kts",
    ".java",
    ".m",
    ".mm",
    ".h",
    ".gradle",
    ".plist",
    ".entitlements",
    ".xml",
    ".yml",
    ".yaml",
    ".ts",
    ".tsx",
    ".js",
    ".jsx",
)

SKIP_PATH_PREFIXES = (
    ".git/",
)

CHECKER_RELATIVE = "scripts/check-forbidden-patterns.py"

EXPO_PUBLIC = re.compile(r"EXPO_PUBLIC_")
PEM_HEADER = re.compile(r"-----BEGIN (?:[A-Z0-9]+ )?PRIVATE KEY-----")
TOKEN_ASSIGNMENT = re.compile(
    r"(?i)(?:api[_-]?key|auth[_-]?token|access[_-]?token|refresh[_-]?token|"
    r"secret[_-]?key|client[_-]?secret|private[_-]?key)\s*[:=]\s*['\"][^'\"]{12,}['\"]"
)
OBVIOUS_TOKEN = re.compile(
    r"(?:sk_live_|sk_test_|ghp_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|Bearer eyJ)"
)

KIT_PATTERNS = (
    (
        "HealthKit import/usage",
        re.compile(
            r"(?m)^\s*import HealthKit\b|"
            r"\bHKHealthStore\b|"
            r"\bHKSampleQuery\b|"
            r"\bHKAnchoredObjectQuery\b|"
            r"\bHKStatisticsQuery\b|"
            r"\bHKObserverQuery\b|"
            r"\bHKWorkoutSession\b|"
            r"HealthKit\.framework|"
            r"com\.apple\.developer\.healthkit|"
            r"\bNSHealthShareUsageDescription\b|"
            r"\bNSHealthUpdateUsageDescription\b"
        ),
    ),
    ("CoreNFC import/usage", re.compile(r"(?m)^\s*import CoreNFC\b|\bNFCTagReaderSession\b|\bNFCNDEFReaderSession\b")),
    (
        "LocalAuthentication import/usage",
        re.compile(r"(?m)^\s*import LocalAuthentication\b|\bLAContext\b"),
    ),
    (
        "Security/Keychain import/usage",
        re.compile(
            r"(?m)^\s*import Security\b|"
            r"\bSecItem(?:Add|CopyMatching|Update|Delete)?\b|"
            r"\bSecAccessControl\w*|"
            r"\bkSec(?:Class|ValueData|AttrAccessible)\w*"
        ),
    ),
    (
        "Health Connect import/usage",
        re.compile(
            r"androidx\.health\.connect\b|"
            r"android\.health\.connect\b|"
            r"\bHealthConnectClient\b|"
            r"\bHealthConnectManager\b|"
            r"\bReadRecordsRequest\b|"
            r"\bHealthPermission\b|"
            r"android\.permission\.health\."
        ),
    ),
    (
        "BiometricPrompt import/usage",
        re.compile(r"androidx\.biometric\b|\bBiometricPrompt\b"),
    ),
    (
        "Android Keystore API usage",
        re.compile(r"AndroidKeyStore\b|android\.security\.keystore\b"),
    ),
    (
        "Android NFC API usage",
        re.compile(r"(?m)^\s*import android\.nfc\b|\bNfcAdapter\b"),
    ),
    (
        "iOS document/photo picker",
        re.compile(
            r"\bUIDocumentPickerViewController\b|"
            r"\bUIDocumentPickerDelegate\b|"
            r"\bUIDocumentBrowserViewController\b|"
            r"\bPHPickerViewController\b|"
            r"\bUIImagePickerController\b|"
            r"\bNSPhotoLibraryUsageDescription\b|"
            r"\bNSPhotoLibraryAddUsageDescription\b"
        ),
    ),
    (
        "Android document/photo picker",
        re.compile(
            r"ActivityResultContracts\.(?:GetContent|OpenDocument|OpenMultipleDocuments|PickVisualMedia)\b|"
            r"\bACTION_(?:GET_CONTENT|OPEN_DOCUMENT|OPEN_DOCUMENT_TREE)\b|"
            r"android\.permission\.READ_MEDIA_IMAGES\b|"
            r"android\.permission\.READ_MEDIA_VISUAL_USER_SELECTED\b"
        ),
    ),
    (
        "AWS / GCP / Firebase upload SDK",
        re.compile(
            r"(?m)^\s*import AWSS3\b|"
            r"\bAWSS3TransferUtility\b|"
            r"\bAmazonS3Client\b|"
            r"\bAWSS3StoragePlugin\b|"
            r"com\.amazonaws\.(?:mobileconnectors|services)\.s3\b|"
            r"software\.amazon\.awssdk\.services\.s3\b|"
            r"com\.amazonaws:aws-android-sdk-s3\b|"
            r"software\.amazon\.awssdk:s3\b|"
            r"amplifyframework:aws-storage|"
            r"com\.google\.cloud\.storage\b|"
            r"com\.google\.cloud:google-cloud-storage\b|"
            r"com\.google\.firebase\.storage\b|"
            r"com\.google\.firebase:firebase-storage\b|"
            r"^\s*import FirebaseStorage\b|"
            r"\bFirebaseStorage\b"
        ),
    ),
    (
        "Hardcoded object-storage upload URL",
        re.compile(
            r"https?://[^\"'\s]*storage\.googleapis\.com|"
            r"https?://[^\"'\s]*storage\.cloud\.google\.com|"
            r"https?://s3\.amazonaws\.com|"
            r"https?://s3[.-][a-z0-9-]+\.amazonaws\.com|"
            r"https?://[^\"'\s]*\.s3[.-][a-z0-9-]*\.amazonaws\.com|"
            r"https?://[^\"'\s]*firebasestorage\.googleapis\.com|"
            r"https?://[^\"'\s]*\.blob\.core\.windows\.net"
        ),
    ),
)

ADAPTER_CONSTRUCTOR = re.compile(
    r"\b(?:NotConfigured|Unavailable|InMemory)[A-Za-z0-9]+\s*\("
)


def tracked_files() -> list[str]:
    result = subprocess.run(
        ["git", "ls-files", "-z"],
        cwd=ROOT,
        check=True,
        capture_output=True,
    )
    return [path for path in result.stdout.decode("utf-8").split("\0") if path]


def is_skipped(path: str) -> bool:
    return path.startswith(SKIP_PATH_PREFIXES)


def is_adapter_path(path: str) -> bool:
    return path.startswith(ADAPTER_ALLOWLIST_PREFIXES)


def is_unit_test_path(path: str) -> bool:
    return (
        path.startswith("apps/ios/HealthAppTests/")
        or "/src/test/" in path
        or path.endswith("Tests.swift")
        or path.endswith("Test.kt")
        or path.endswith("Tests.kt")
        or path.endswith(".test.ts")
        or path.endswith(".test.tsx")
    )


def is_constructor_allowed(path: str) -> bool:
    return path in CONSTRUCTOR_ALLOWED_PATHS or is_unit_test_path(path)


def read_text(path: str) -> str | None:
    full = ROOT / path
    try:
        return full.read_text(encoding="utf-8")
    except (UnicodeDecodeError, FileNotFoundError, IsADirectoryError):
        return None


def main() -> int:
    violations: list[str] = []

    for path in tracked_files():
        if is_skipped(path):
            continue
        text = read_text(path)
        if text is None:
            continue

        if path != CHECKER_RELATIVE:
            for match in EXPO_PUBLIC.finditer(text):
                line = text.count("\n", 0, match.start()) + 1
                violations.append(f"{path}:{line}: forbidden Expo public env prefix EXPO_PUBLIC_")

        for match in PEM_HEADER.finditer(text):
            line = text.count("\n", 0, match.start()) + 1
            violations.append(f"{path}:{line}: private key PEM header")

        if path != CHECKER_RELATIVE:
            for match in TOKEN_ASSIGNMENT.finditer(text):
                line = text.count("\n", 0, match.start()) + 1
                violations.append(f"{path}:{line}: obvious secret assignment")
            for match in OBVIOUS_TOKEN.finditer(text):
                line = text.count("\n", 0, match.start()) + 1
                violations.append(f"{path}:{line}: obvious token material")

        if path.endswith(CODE_SUFFIXES) and path != CHECKER_RELATIVE:
            if not is_adapter_path(path):
                for label, pattern in KIT_PATTERNS:
                    for match in pattern.finditer(text):
                        line = text.count("\n", 0, match.start()) + 1
                        violations.append(
                            f"{path}:{line}: {label} outside Core/platform adapter allowlist"
                        )
            if not is_constructor_allowed(path):
                for match in ADAPTER_CONSTRUCTOR.finditer(text):
                    line = text.count("\n", 0, match.start()) + 1
                    violations.append(
                        f"{path}:{line}: concrete adapter constructed outside CompositionRoot"
                    )

    if violations:
        print("Forbidden-pattern gate failed:", file=sys.stderr)
        for item in violations:
            print(f"  {item}", file=sys.stderr)
        return 1

    print("Forbidden-pattern gate passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
