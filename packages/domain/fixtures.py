"""Load synthetic medical-expense fixtures. No network. No real PII."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from eligibility import ExpenseLine

FIXTURES_PATH = Path(__file__).resolve().parent / "fixtures" / "medical-expenses.json"

SYNTHETIC_ID_PREFIX = "SYNTH-"


def load_fixture_document(path: Path | None = None) -> dict[str, Any]:
    target = path or FIXTURES_PATH
    return json.loads(target.read_text(encoding="utf-8"))


def lines_from_case(case: dict[str, Any]) -> list[ExpenseLine]:
    return [
        ExpenseLine(
            id=line["id"],
            category=line["category"],
            amount_jpy=line["amountJpy"],
            reimbursed_jpy=line["reimbursedJpy"],
            documented=line["documented"],
            payee_label=line["payeeLabel"],
        )
        for line in case["lines"]
    ]
