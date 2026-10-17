#!/usr/bin/env python3
"""Web forbidden-pattern and manifest gates for apps/web (ADR-0008/0009/0010/0011).

Safe to pass when apps/web does not exist yet: prints a skip notice and exits 0.
Stdlib only. Regex based, like scripts/check-forbidden-patterns.py; it is a
tripwire, not a proof of security.
"""

from __future__ import annotations

import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WEB_DIR = "apps/web"

# Only these paths may import auth/storage/email/db SDKs or construct stub adapters.
ADAPTER_ALLOWLIST_PREFIXES = ("apps/web/src/core/",)
CONSTRUCTOR_ALLOWED_PATHS = ("apps/web/src/core/composition-root.ts",)

SOURCE_SUFFIXES = (".ts", ".tsx", ".js", ".jsx", ".mjs", ".cjs", ".html", ".mdx")
SKIP_DIR_PARTS = ("node_modules", ".next", "dist", "build", "coverage", ".turbo", ".vercel")
TEST_PATH = re.compile(r"(?:\.test\.|\.spec\.|/__tests__/|/e2e/)")

# (label, regex) applied to every web source file, tests exempt.
EVERYWHERE = (
    ("localStorage/sessionStorage/IndexedDB use (ADR-0009: no browser-held data)",
     re.compile(r"\b(?:localStorage|sessionStorage|indexedDB)\b|\bwindow\.(?:localStorage|sessionStorage)\b")),
    ("document.cookie access (cookies are httpOnly, set by BFF only)",
     re.compile(r"\bdocument\.cookie\b")),
    ("dangerouslySetInnerHTML / innerHTML / outerHTML / insertAdjacentHTML",
     re.compile(r"dangerouslySetInnerHTML|\.(?:inner|outer)HTML\s*=|insertAdjacentHTML\s*\(")),
    ("eval / new Function",
     re.compile(r"(?<![\w.])eval\s*\(|\bnew\s+Function\s*\(")),
    ("NEXT_PUBLIC_ variable that looks like a secret",
     re.compile(r"NEXT_PUBLIC_[A-Z0-9_]*(?:SECRET|TOKEN|PASSWORD|PRIVATE|API_KEY|DATABASE|DB_URL|SIGNING)")),
    ("PEM private key",
     re.compile(r"-----BEGIN (?:[A-Z0-9]+ )?PRIVATE KEY-----")),
    ("hardcoded secret assignment",
     re.compile(r"(?i)(?:api[_-]?key|auth[_-]?token|access[_-]?token|refresh[_-]?token|"
                r"secret[_-]?key|client[_-]?secret|private[_-]?key|session[_-]?secret)"
                r"\s*[:=]\s*['\"][^'\"]{12,}['\"]")),
    ("obvious token prefix",
     re.compile(r"(?:sk_live_|sk_test_|ghp_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|Bearer eyJ|vercel_blob_rw_)")),
    ("database URL with embedded credentials",
     re.compile(r"postgres(?:ql)?://[^\s:/'\"@]+:[^\s@'\"]+@")),
    ("public blob/object access or hardcoded blob host (clinical files must be private)",
     re.compile(r"access\s*:\s*['\"]public['\"]|public\.blob\.vercel-storage\.com|"
                r"https?://[^\"'\s]*(?:\.blob\.vercel-storage\.com|storage\.googleapis\.com|\.amazonaws\.com)")),
    ("third-party script tag on web pages",
     re.compile(r"<script[^>]+src=['\"]https?://", re.IGNORECASE)),
)

# Auth / storage / DB / email SDKs: allowed only under ADAPTER_ALLOWLIST_PREFIXES.
SDK_MODULES = (
    r"@simplewebauthn/(?:server|browser)", r"openid-client", r"oauth4webapi", r"next-auth",
    r"@auth/[\w.-]+", r"jose", r"@vercel/blob", r"@vercel/postgres", r"@neondatabase/[\w.-]+",
    r"pg", r"postgres", r"drizzle-orm[\w/.-]*", r"firebase[\w/.-]*", r"@aws-sdk/[\w.-]+",
    r"@google-cloud/[\w.-]+", r"resend", r"nodemailer", r"@sendgrid/[\w.-]+", r"postmark",
    r"@vercel/kv", r"@upstash/[\w.-]+",
)
SDK_IMPORT = re.compile(
    r"""(?:from\s+|import\s+|require\(\s*|import\(\s*)['"](?:%s)['"]""" % "|".join(SDK_MODULES)
)
ADAPTER_CONSTRUCTOR = re.compile(r"\bnew\s+(?:NotConfigured|Unavailable|InMemory)[A-Za-z0-9]+\s*\(")

BAD_VERSION = re.compile(r"^(?:\*|latest|next|x|)$|^(?:git|git\+\w+|http|https|github|file|link):|^[\w.-]+/[\w.-]+$")


@dataclass(frozen=True)
class Finding:
    path: str
    line: int
    message: str

    def render(self) -> str:
        return f"{self.path}:{self.line}: {self.message}"


def _skipped(rel: str) -> bool:
    return any(part in SKIP_DIR_PARTS for part in rel.split("/"))


def _line_of(text: str, index: int) -> int:
    return text.count("\n", 0, index) + 1


def scan_source(rel: str, text: str) -> list[Finding]:
    findings: list[Finding] = []
    is_test = bool(TEST_PATH.search(rel))
    if not is_test:
        for label, rx in EVERYWHERE:
            for m in rx.finditer(text):
                findings.append(Finding(rel, _line_of(text, m.start()), label))
        if not rel.startswith(ADAPTER_ALLOWLIST_PREFIXES):
            for m in SDK_IMPORT.finditer(text):
                findings.append(Finding(rel, _line_of(text, m.start()),
                                        "auth/storage/db/email SDK import outside apps/web/src/core/ (CompositionRoot adapters only)"))
        if rel not in CONSTRUCTOR_ALLOWED_PATHS:
            for m in ADAPTER_CONSTRUCTOR.finditer(text):
                findings.append(Finding(rel, _line_of(text, m.start()),
                                        "stub adapter constructed outside composition-root.ts"))
    return findings


def scan_manifest(rel: str, text: str) -> list[Finding]:
    findings: list[Finding] = []
    try:
        data = json.loads(text)
    except json.JSONDecodeError as exc:
        return [Finding(rel, exc.lineno, f"package.json is not valid JSON: {exc.msg}")]
    for section in ("dependencies", "devDependencies", "optionalDependencies"):
        for name, version in (data.get(section) or {}).items():
            if not isinstance(version, str) or BAD_VERSION.match(version.strip()):
                findings.append(Finding(rel, 1, f"{section}.{name} uses unpinned or non-registry version {version!r}"))
    scripts = data.get("scripts") or {}
    for hook in ("preinstall", "install", "postinstall"):
        if hook in scripts:
            findings.append(Finding(rel, 1, f"lifecycle script {hook!r} in apps/web is not allowed"))
    return findings


def scan(root: Path) -> tuple[list[Finding], bool]:
    """Return (findings, web_present)."""
    web = root / WEB_DIR
    if not web.is_dir():
        return [], False
    findings: list[Finding] = []
    for path in sorted(web.rglob("*")):
        if not path.is_file():
            continue
        rel = path.relative_to(root).as_posix()
        if _skipped(rel):
            continue
        name = path.name
        if name.startswith(".env") and name != ".env.example":
            findings.append(Finding(rel, 1, "env file must not be committed (use .env.example names only)"))
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except (UnicodeDecodeError, OSError):
            continue
        if name == "package.json":
            findings.extend(scan_manifest(rel, text))
        elif path.suffix in SOURCE_SUFFIXES:
            findings.extend(scan_source(rel, text))
    if (web / "package.json").is_file() and not (root / "pnpm-lock.yaml").is_file():
        findings.append(Finding("pnpm-lock.yaml", 1, "lockfile missing while apps/web/package.json exists"))
    return findings, True


def main(argv: list[str] | None = None) -> int:
    root = Path(argv[0]).resolve() if argv else ROOT
    findings, present = scan(root)
    if not present:
        print("check-web-patterns: apps/web not present; nothing to check (pass).")
        return 0
    if findings:
        print("Forbidden web patterns found:")
        for f in findings:
            print(f"  {f.render()}")
        return 1
    print("check-web-patterns: apps/web clean.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
