import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path

SPEC = importlib.util.spec_from_file_location(
    "check_web_patterns", Path(__file__).with_name("check-web-patterns.py")
)
cwp = importlib.util.module_from_spec(SPEC)
sys.modules["check_web_patterns"] = cwp
SPEC.loader.exec_module(cwp)


class WebPatternTests(unittest.TestCase):
    def run_tree(self, files, lock=True):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            for rel, content in files.items():
                p = root / rel
                p.parent.mkdir(parents=True, exist_ok=True)
                p.write_text(content, encoding="utf-8")
            if lock:
                (root / "pnpm-lock.yaml").write_text("lockfileVersion: '9.0'\n")
            findings, present = cwp.scan(root)
            return [f.message for f in findings], present

    def test_absent_web_dir_passes(self):
        msgs, present = self.run_tree({})
        self.assertFalse(present)
        self.assertEqual(msgs, [])
        with tempfile.TemporaryDirectory() as tmp:
            self.assertEqual(cwp.main([tmp]), 0)

    def test_clean_app_passes(self):
        msgs, present = self.run_tree({
            "apps/web/src/app/page.tsx": "export default function P(){return <p>hi</p>}\n",
            "apps/web/package.json": json.dumps({"dependencies": {"next": "15.0.0"}}),
        })
        self.assertTrue(present)
        self.assertEqual(msgs, [])

    def test_storage_flagged_but_tests_exempt(self):
        msgs, _ = self.run_tree({
            "apps/web/src/a.ts": "localStorage.setItem('t','x')\n",
            "apps/web/src/b.test.ts": "expect(window.localStorage).toBeUndefined()\n",
        })
        self.assertEqual(len(msgs), 1)
        self.assertIn("localStorage", msgs[0])

    def test_xss_sinks_and_cookie(self):
        msgs, _ = self.run_tree({
            "apps/web/src/a.tsx": "<div dangerouslySetInnerHTML={{__html:x}}/>\n",
            "apps/web/src/b.ts": "document.cookie = 'a=b'; el.innerHTML = x; eval('1')\n",
        })
        self.assertEqual(len(msgs), 4)

    def test_sdk_import_outside_core(self):
        bad = "import { generateRegistrationOptions } from '@simplewebauthn/server'\n"
        msgs, _ = self.run_tree({
            "apps/web/src/features/login.ts": bad,
            "apps/web/src/core/webauthn-adapter.ts": bad,
        })
        self.assertEqual(len(msgs), 1)
        self.assertIn("outside apps/web/src/core", msgs[0])

    def test_constructor_only_in_composition_root(self):
        msgs, _ = self.run_tree({
            "apps/web/src/core/composition-root.ts": "new NotConfiguredConsentStore()\n",
            "apps/web/src/features/x.ts": "const s = new NotConfiguredConsentStore()\n",
            "apps/web/src/features/x.test.ts": "new NotConfiguredConsentStore()\n",
        })
        self.assertEqual(len(msgs), 1)

    def test_secrets_urls_and_public_blob(self):
        msgs, _ = self.run_tree({
            "apps/web/src/a.ts": "const url = 'postgres://user:hunter2@host/db'\n",
            "apps/web/src/b.ts": "put('f', d, { access: 'public' })\n",
            "apps/web/src/c.ts": "const NEXT_PUBLIC_API_KEY_X = 1\n",
            "apps/web/.env.local": "A=1\n",
            "apps/web/.env.example": "HEALTH_APP_API_BASE_URL=\n",
        })
        self.assertEqual(len(msgs), 4)

    def test_manifest_and_lockfile(self):
        msgs, _ = self.run_tree({
            "apps/web/package.json": json.dumps({
                "dependencies": {"a": "latest", "b": "github:x/y", "c": "1.2.3"},
                "scripts": {"postinstall": "node x.js"},
            }),
        }, lock=False)
        self.assertEqual(len(msgs), 4)

    def test_node_modules_skipped(self):
        msgs, _ = self.run_tree({"apps/web/node_modules/x/index.js": "localStorage.x=1\n"})
        self.assertEqual(msgs, [])


if __name__ == "__main__":
    unittest.main()
