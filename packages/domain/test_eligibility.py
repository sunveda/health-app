"""Pure unit tests for synthetic medical-expense eligibility fixtures."""

from __future__ import annotations

import re
import unittest

from eligibility import (
    ELIGIBLE_CATEGORIES,
    CalculationStatus,
    ExpenseLine,
    ReviewReason,
    evaluate_case,
)
from fixtures import SYNTHETIC_ID_PREFIX, lines_from_case, load_fixture_document

TWELVE_DIGIT = re.compile(r"\d{12}")
EMAIL_LIKE = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")


class EligibilityEngineTests(unittest.TestCase):
    def test_documented_consultation_nets_reimbursement(self) -> None:
        result = evaluate_case(
            2024,
            [
                ExpenseLine(
                    id="SYNTH-LINE-UNIT-1",
                    category="consultation",
                    amount_jpy=15000,
                    reimbursed_jpy=3000,
                    documented=True,
                    payee_label="Fixture Clinic Alpha",
                )
            ],
        )
        self.assertEqual(result.eligible_amount_jpy, 12000)
        self.assertEqual(result.calculation_status, CalculationStatus.READY_FOR_EXPORT)
        self.assertEqual(result.review_reasons, ())
        self.assertEqual(result.contributing_line_ids, ("SYNTH-LINE-UNIT-1",))

    def test_ineligible_category_does_not_contribute(self) -> None:
        result = evaluate_case(
            2024,
            [
                ExpenseLine(
                    id="SYNTH-LINE-UNIT-2",
                    category="otc_supplement",
                    amount_jpy=4800,
                    reimbursed_jpy=0,
                    documented=True,
                    payee_label="Fixture Pharmacy Beta",
                )
            ],
        )
        self.assertEqual(result.eligible_amount_jpy, 0)
        self.assertEqual(result.calculation_status, CalculationStatus.NOT_AVAILABLE)
        self.assertEqual(result.review_reasons, ())

    def test_undocumented_eligible_category_requires_review(self) -> None:
        result = evaluate_case(
            2024,
            [
                ExpenseLine(
                    id="SYNTH-LINE-UNIT-3",
                    category="prescription",
                    amount_jpy=5000,
                    reimbursed_jpy=0,
                    documented=False,
                    payee_label="Fixture Pharmacy Beta",
                )
            ],
        )
        self.assertEqual(result.eligible_amount_jpy, 0)
        self.assertEqual(result.calculation_status, CalculationStatus.REVIEW_REQUIRED)
        self.assertEqual(result.review_reasons, (ReviewReason.UNDOCUMENTED,))

    def test_allowlist_matches_fixture_contract_categories(self) -> None:
        document = load_fixture_document()
        self.assertEqual(set(document["eligibleCategories"]), set(ELIGIBLE_CATEGORIES))


class FixtureDocumentTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.document = load_fixture_document()
        cls.cases = cls.document["cases"]

    def test_disclaimer_marks_fixtures_as_synthetic_and_non_legal(self) -> None:
        meta = self.document["meta"]
        self.assertTrue(meta["notRealRecords"])
        self.assertEqual(meta["kind"], "synthetic-fixtures")
        disclaimer = meta["disclaimer"].lower()
        self.assertIn("synthetic", disclaimer)
        self.assertIn("not japanese tax-law", disclaimer)
        self.assertIn("todo for product/tax counsel", disclaimer)

    def test_every_fixture_case_matches_engine_output(self) -> None:
        self.assertGreaterEqual(len(self.cases), 8)
        seen_ids: set[str] = set()
        statuses: set[str] = set()

        for case in self.cases:
            case_id = case["id"]
            self.assertTrue(case_id.startswith(SYNTHETIC_ID_PREFIX), case_id)
            self.assertNotIn(case_id, seen_ids)
            seen_ids.add(case_id)

            result = evaluate_case(case["taxYear"], lines_from_case(case))
            expected = case["expected"]
            self.assertEqual(result.tax_year, case["taxYear"], case_id)
            self.assertEqual(result.eligible_amount_jpy, expected["eligibleAmountJpy"], case_id)
            self.assertEqual(result.calculation_status.value, expected["calculationStatus"], case_id)
            self.assertEqual(
                [reason.value for reason in result.review_reasons],
                expected["reviewReasons"],
                case_id,
            )
            self.assertEqual(list(result.contributing_line_ids), expected["contributingLineIds"], case_id)
            statuses.add(result.calculation_status.value)

        self.assertEqual(
            statuses,
            {
                CalculationStatus.NOT_AVAILABLE.value,
                CalculationStatus.REVIEW_REQUIRED.value,
                CalculationStatus.READY_FOR_EXPORT.value,
            },
        )

    def test_fixtures_cannot_be_mistaken_for_real_identity_or_records(self) -> None:
        blob = str(self.document)
        self.assertIsNone(TWELVE_DIGIT.search(blob))
        self.assertIsNone(EMAIL_LIKE.search(blob))
        self.assertNotIn("my number", blob.lower())
        self.assertNotIn("individual number", blob.lower())

        for case in self.cases:
            for line in case["lines"]:
                self.assertTrue(line["id"].startswith(SYNTHETIC_ID_PREFIX), line["id"])
                self.assertTrue(str(line["payeeLabel"]).startswith("Fixture "), line["payeeLabel"])


if __name__ == "__main__":
    unittest.main()
