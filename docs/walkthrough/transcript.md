# Health App walkthrough transcript

Narration is synthesized with the installed macOS Samantha voice. All records are synthetic. Native illustrations are source-based, not simulator captures.

## 00:00 — Health App

**CURRENT + PLANNED · CLEARLY SEPARATED**

Health App is a planned health and wellness portal for web, iOS and Android. This walkthrough separates working synthetic screens from illustrative native layouts and future flows. It does not demonstrate a clinical-ready product.

## 00:14 — The specification

**APPROVED DIRECTION · NOT DEPLOYED**

The approved architecture uses Next.js and a server-side backend for frontend on the web, SwiftUI on iOS, and Kotlin Compose on Android. Clients share contracts and fixtures. Vercel and Neon are hosting targets, not a deployed service. AI is deferred.

## 00:31 — Home

**ILLUSTRATIVE NATIVE LAYOUT · NOT A SIMULATOR CAPTURE**

The native Home screen is a readiness overview. This illustration follows the current source, but is not a simulator capture. Identity, wellness sync and the clinical pipeline remain not configured. There is no working My Number sign-in or personal health dashboard.

## 00:48 — Health

**ILLUSTRATIVE NATIVE LAYOUT · INTEGRATIONS DISABLED**

The Health tab shows source availability, permission state, read scopes and clinical status. This is an illustrative layout. Apple Health and Health Connect are not connected. Clinical validation, quarantine and uploads are also not configured, and no device permissions are requested.

## 01:06 — Expenses on web

**ACTUAL WEB CAPTURE · LOCAL SYNTHETIC BUILD**

Here is the actual web implementation, captured from the local production build. It opens directly to a synthetic expense demonstration. The notice explains that these are sample records and engineering rules, not tax guidance. There is no sign-in or file import.

## 01:23 — Review the numbers

**ACTUAL WEB CAPTURE · SYNTHETIC RECORDS**

For the documented consultation sample, fifteen thousand yen paid minus three thousand yen reimbursed gives an illustrative included amount of twelve thousand yen. The screen shows the category, documentation flag, arithmetic and outcome. These are fixture rules, not a completed Japanese tax calculator.

## 01:43 — Review exceptions

**ACTUAL WEB CAPTURE · REVIEW REQUIRED**

Changing scenarios reveals review conditions. This mixed example includes six thousand yen and flags an undocumented line. Other samples cover unknown categories, negative amounts, over-reimbursement and empty records. Changing the scenario clears the acknowledgment and any export placeholder.

## 02:02 — Acknowledge, then inspect

**ACTUAL WEB CAPTURE · NOTHING CREATED OR SHARED**

The export placeholder button begins disabled. After acknowledging the synthetic-data limitation, the user can view an explanation that export is not configured. No file is created or shared. Revoking acknowledgment hides the message. Real exports remain disabled.

## 02:20 — Phone-size experience

**ACTUAL MOBILE-WIDTH WEB · NOT iOS / ANDROID RUNTIME**

This capture shows the same web flow at phone width. It is responsive web, not an iOS or Android simulator. Native Expense screens have also been implemented using the same generated fixtures, but their full builds and device user interfaces have not been verified here.

## 02:37 — Settings

**ILLUSTRATIVE NATIVE LAYOUT · NOT A SIMULATOR CAPTURE**

The Settings tab exposes privacy and platform capability status. This source-based illustration shows consent, crash reporting and telemetry as not configured. Identity, biometrics, secure storage and NFC are also stubs. The expense acknowledgment is not consent to collect health data.

## 02:57 — The intended journey

**PLANNED FLOW · NOT IMPLEMENTED OR VERIFIED**

The future journey would connect reviewed identity and consent, approved health sources or secure clinical uploads, record review, reviewed expense calculations, and separately confirmed exports. These steps are planned, not functioning integrations. Official provider, security, privacy and legal decisions are still required.

## 03:18 — What is verified

**LOCAL VERIFICATION · NO DEPLOYMENT**

The web production build, lint, type checks and four desktop and mobile browser tests passed. Nine fixture scenarios match the generated models. Swift syntax and model parity were checked. Native builds, device testing, localization and real integrations remain outstanding. Nothing has been deployed.

