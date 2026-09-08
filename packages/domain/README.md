# Domain package

Pure, platform-neutral business rules and synthetic test fixtures. Domain code must not depend on SwiftUI, Jetpack Compose, device APIs, network clients, or production secrets.

Native clients do **not** import this package at runtime. Swift and Kotlin ports of the same rules belong in a later Expenses stage. This package is the shared specification plus executable fixtures.

## Medical-expense eligibility (kickoff)

`eligibility.py` implements a **synthetic product placeholder**, not Japanese tax-law guidance and not a 医療費控除 calculator. Category allowlists, documentation checks, and `not_available` / `review_required` / `ready_for_export` mapping exist so both apps can share cases before a reviewed engine exists.

Legal thresholds, official eligible-expense categories, and user-facing tax copy are **TODO for product/tax counsel**.

Fixtures live in `fixtures/medical-expenses.json`:

- IDs use the `SYNTH-` prefix
- Payees are labeled `Fixture …`
- No patient names, emails, individual numbers, diagnoses, or clinic names that could be mistaken for real records

## Tests

From the repository root (no network):

```bash
python3 -m unittest discover -s packages/domain -t packages/domain -p 'test_*.py'
```
