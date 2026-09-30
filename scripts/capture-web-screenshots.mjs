#!/usr/bin/env node
/**
 * Capture fail-closed web portal tab screenshots for docs.
 * Usage: node scripts/capture-web-screenshots.mjs [baseUrl]
 */
import { mkdir, copyFile } from "node:fs/promises";
import path from "node:path";
import { chromium } from "playwright";

const baseUrl = process.argv[2] ?? "http://127.0.0.1:4173/";
const docsDir = path.resolve("docs/images/web");
const artifactsDir = path.resolve("/opt/cursor/artifacts/screenshots");

const tabs = [
  { id: "home", label: "Home", file: "web-home.png" },
  { id: "health", label: "Health", file: "web-health.png" },
  { id: "expenses", label: "Expenses", file: "web-expenses.png" },
  { id: "settings", label: "Settings", file: "web-settings.png" },
];

await mkdir(docsDir, { recursive: true });
await mkdir(artifactsDir, { recursive: true });

const browser = await chromium.launch({
  channel: "chrome",
  headless: true,
});
const page = await browser.newPage({
  viewport: { width: 1280, height: 900 },
  deviceScaleFactor: 2,
});

await page.goto(baseUrl, { waitUntil: "networkidle" });
await page.waitForSelector(".portal__brand");

for (const tab of tabs) {
  await page.getByRole("button", { name: tab.label, exact: true }).click();
  await page.waitForSelector(".panel h1");
  // Brief settle for nav active state / paint
  await page.waitForTimeout(200);
  const docsPath = path.join(docsDir, tab.file);
  const artifactPath = path.join(artifactsDir, tab.file);
  await page.screenshot({ path: docsPath, fullPage: true });
  await copyFile(docsPath, artifactPath);
  console.log(`captured ${tab.id}: ${docsPath}`);
}

await browser.close();
console.log("done");
