# Security and Privacy Boundaries

## Non-negotiable rules

The application handles identity, health, and financial information. The default posture is to **collect less, expose less, retain less, and log less**. No production personal data, individual numbers, access tokens, private keys, certificates, medical documents, or unredacted screenshots may be committed to Git.

| Area | Required control |
|---|---|
| Secrets | Use environment injection or a managed secret store; commit only `.env.example` with non-secret names |
| Local storage | Use iOS Keychain and Android Keystore-backed storage for tokens and credentials; do not use ordinary preferences for secrets |
| Identity | Validate OIDC issuer, audience, nonce, state, PKCE verifier, redirect URI, token expiry, and claims |
| Authorization | Enforce server-side tenant, user, consent, and purpose checks for every sensitive operation |
| Health data | Request the smallest platform scope, explain its purpose, and support revocation and deletion |
| Logs | Redact identifiers, tokens, report contents, diagnoses, prescriptions, addresses, and financial details |
| Uploads | Enforce MIME/type limits, size limits, malware scanning, quarantine, encryption, and explicit retention |
| AI | Ground output in user-approved records, show provenance and timestamps, avoid diagnosis claims, and provide safe fallback states |
| Exports | Require an explicit review and confirmation step before generating or sharing expense or health exports |
| Infrastructure | Use separate projects and service accounts, least privilege, CMEK where required, perimeter controls, monitoring, and tested incident response |

## My Number handling

The individual number is a specially sensitive identifier. It must not be used as a general-purpose client identifier, placed in analytics events, displayed unnecessarily, or included in error messages. The pairwise subject identifier should be used for application identity where permitted. Any persistence, retrieval, display, export, or deletion path involving the individual number requires an explicit data-flow review and legal/compliance approval.

The specification references the Digital Authentication App, OIDC scopes, certificate PINs, NFC, and reader hardware. These details must be validated against current official documentation and the approved relying-party registration before implementation. The repository intentionally does not contain guessed endpoints, certificate commands, PIN-handling code, or claims that an unverified scope is available.

## Threat-model checklist

Before production integration, review at minimum: authorization-code interception, malicious redirect handling, token substitution, replay, state/nonce failure, rooted or jailbroken device behavior, clipboard and screenshot leakage, debug logging, backup extraction, offline-cache access, emulator abuse, compromised third-party libraries, malicious uploads, prompt injection in clinical documents, model overreach, insider access, data exfiltration, denial of service, and incomplete deletion.

## Compliance posture

The product team must document the lawful basis and purpose for each data category, user notices and consent language, retention periods, deletion/export procedures, processor and subprocessor responsibilities, access review, incident notification, and the exact requirements of applicable Japanese privacy and My Number regulations. Cloud certifications and security controls support a compliance program but do not replace product-specific legal review.

## Stage 1.5 baseline

Classification, on-device vs later-sync principles, logging redaction, the crash-reporting **not configured** decision, and consent/purpose scaffolding are in [`privacy-baseline.md`](privacy-baseline.md). Lawful basis and user-facing legal copy remain TODO for product/legal.

## Development safeguards

Pull requests should include a data-classification note for new fields, tests for authorization and redaction, dependency review, and a statement about whether a new native permission is introduced. CI should scan for secrets and prohibited sample data. Fixtures must use synthetic values that cannot be mistaken for real identity or medical records.
