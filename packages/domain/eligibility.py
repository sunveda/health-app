"""Synthetic medical-expense eligibility engine.

Platform-neutral product placeholder. This is not Japanese tax-law guidance,
not a 医療費控除 implementation, and not legal advice. Category allowlists and
status mapping exist so clients can share fixtures before a reviewed calculator
exists. Legal thresholds and official category mapping are TODO for product/tax
counsel.

No network. No device APIs. Fixtures must stay synthetic.
"""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Iterable

ELIGIBLE_CATEGORIES = frozenset(
    {
        "consultation",
        "prescription",
        "inpatient",
        "dental_treatment",
    }
)

INELIGIBLE_CATEGORIES = frozenset(
    {
        "otc_supplement",
        "cosmetic",
    }
)

KNOWN_CATEGORIES = ELIGIBLE_CATEGORIES | INELIGIBLE_CATEGORIES


class CalculationStatus(str, Enum):
    NOT_AVAILABLE = "not_available"
    REVIEW_REQUIRED = "review_required"
    READY_FOR_EXPORT = "ready_for_export"


class ReviewReason(str, Enum):
    UNDOCUMENTED = "undocumented"
    UNKNOWN_CATEGORY = "unknown_category"
    NEGATIVE_AMOUNT = "negative_amount"
    OVER_REIMBURSED = "over_reimbursed"


@dataclass(frozen=True)
class ExpenseLine:
    """One synthetic receipt line. No patient identity, no diagnoses."""

    id: str
    category: str
    amount_jpy: int
    reimbursed_jpy: int
    documented: bool
    payee_label: str


@dataclass(frozen=True)
class EligibilityResult:
    tax_year: int
    eligible_amount_jpy: int
    calculation_status: CalculationStatus
    review_reasons: tuple[ReviewReason, ...]
    contributing_line_ids: tuple[str, ...]


def evaluate_case(tax_year: int, lines: Iterable[ExpenseLine]) -> EligibilityResult:
    contributing_ids: list[str] = []
    reasons: list[ReviewReason] = []
    eligible_total = 0

    for line in lines:
        amount, line_reasons = _line_eligible_amount(line)
        for reason in line_reasons:
            if reason not in reasons:
                reasons.append(reason)
        if amount > 0:
            eligible_total += amount
            contributing_ids.append(line.id)

    if reasons:
        status = CalculationStatus.REVIEW_REQUIRED
    elif eligible_total > 0:
        status = CalculationStatus.READY_FOR_EXPORT
    else:
        status = CalculationStatus.NOT_AVAILABLE

    return EligibilityResult(
        tax_year=tax_year,
        eligible_amount_jpy=eligible_total,
        calculation_status=status,
        review_reasons=tuple(reasons),
        contributing_line_ids=tuple(contributing_ids),
    )


def _line_eligible_amount(line: ExpenseLine) -> tuple[int, list[ReviewReason]]:
    reasons: list[ReviewReason] = []

    if line.amount_jpy < 0 or line.reimbursed_jpy < 0:
        reasons.append(ReviewReason.NEGATIVE_AMOUNT)
        return 0, reasons

    if line.reimbursed_jpy > line.amount_jpy:
        reasons.append(ReviewReason.OVER_REIMBURSED)
        return 0, reasons

    if line.category not in KNOWN_CATEGORIES:
        reasons.append(ReviewReason.UNKNOWN_CATEGORY)
        return 0, reasons

    if line.category in INELIGIBLE_CATEGORIES:
        return 0, reasons

    if not line.documented:
        reasons.append(ReviewReason.UNDOCUMENTED)
        return 0, reasons

    return line.amount_jpy - line.reimbursed_jpy, reasons
