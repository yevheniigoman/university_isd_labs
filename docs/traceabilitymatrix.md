# Traceability Matrix

**Source specification:** [`spec/SPEC_running_tracker_v1.md`](../spec/SPEC_running_tracker_v1.md), [`spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md`](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md)
**Version:** 1.1 (v1, addendum 1)
**Date:** 2026-10-02, amended 2026-11-15

This matrix traces each requirement to the acceptance criteria that verify it, to the tickets that implement it, and to the REST API endpoint that exposes it. Requirement identifiers are defined in the parent specification's [Section 3](../spec/SPEC_running_tracker_v1.md#3-specific-requirements) or, for `REQ-F-15`, in [addendum 1](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md#3-requirement); acceptance criteria are defined in the parent's [Appendix B](../spec/SPEC_running_tracker_v1.md#appendix-b-acceptance-criteria-index) or the addendum's [Section 4](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md#4-acceptance-criteria); ticket numbers are defined in [`docs/tickets.md`](tickets.md); endpoints are defined in [`docs/openapi.yml`](openapi.yml).

Acceptance criteria are numbered locally within each requirement. Where one block of criteria is shared between two requirements — `REQ-F-9` and `REQ-F-10` are specified together in the appendix — the criteria are listed against both.

## Requirements with acceptance criteria

| Requirement | Requirement name | Acceptance criteria | Tickets | REST API endpoint |
|---|---|---|---|---|
| REQ-F-4 | Record a Training | AC-1, AC-2 | 4, 6 | `POST /api/trainings` |
| REQ-F-5 | Training Date | AC-1, AC-2 | 5, 6 | `POST /api/trainings`, `PUT /api/trainings/{id}` |
| REQ-F-6 | Training Distance | AC-1, AC-2 | 4, 5, 6 | `POST /api/trainings`, `PUT /api/trainings/{id}` |
| REQ-F-8 | View Training history | AC-1, AC-2 | 8 | `GET /api/trainings` |
| REQ-F-9 | View Training details | AC-1, AC-2 | 9 | `GET /api/trainings/{id}` |
| REQ-F-10 | Edit a Training | AC-1, AC-2 | 10 | `PUT /api/trainings/{id}` |
| REQ-F-11 | Delete a Training | AC-1 | 11 | `DELETE /api/trainings/{id}` |
| REQ-F-12 | Month total distance | AC-1, AC-2 | 7 | `GET /api/trainings` (Month group totals) |
| REQ-F-13 | Month Comparison Chart | AC-1, AC-2, AC-3 | 12, 13 | `GET /api/month-comparison` |
| REQ-F-15 | Monthly distance goals | AC-1, AC-2, AC-3, AC-4, AC-5, AC-6 | 14 | `PUT /api/goals/{month}`, `GET /api/goals/{month}`, `DELETE /api/goals/{month}`, `GET /api/goals` |

## Notes

- `REQ-F-9` and `REQ-F-10` share a single block of two criteria in the specification. The same two criteria therefore verify both requirements, and the criteria listed above are not additive across the pair.
- Coverage in this matrix reflects the specification as written. Several guarantees stated in requirement prose are not verified by any criterion — cross-Month recomputation on Date change (`REQ-F-5`), validation parity between creation and update (`REQ-F-10`), and permanence of deletion (`REQ-F-11`). The specification mandates test-first development for this logic under `REQ-DP-1`. The implementation nevertheless covers all three: `TrainingEditTest.reDatesAcrossMonthsAndRecomputesBothMonthTotals`, the three validation-parity tests in `TrainingEditTest`, and `TrainingDeleteTest.deletesTheTrainingPermanently`.
- Ticket 13 (chart rendering) is a frontend concern and is out of scope for this backend-only deliverable. Ticket 12 emits the twelve Month points already ordered oldest first, so the renderer draws the list as received.
- `REQ-F-15` is defined by [addendum 1](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md), not the parent specification. Its six criteria are numbered locally in the addendum's [Section 4](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md#4-acceptance-criteria) and are not part of Appendix B.
- Two guarantees in `REQ-F-15`'s prose are not verified by any criterion, because their criteria were removed on 2026-11-15: progress recomputation after a Training is created, edited, or deleted, and refusal of a goal that is absent or belongs to another account. Both remain mandated by the requirement and by `REQ-F-14`, and fall under `REQ-DP-1`. Both are implemented and tested: `GoalProgressTest.recomputesProgressAfterATrainingIsCreated`, `GoalProgressTest.recomputesProgressAfterATrainingIsDeleted`, and `GoalProgressTest.recomputesProgressAfterATrainingIsReDatedAcrossMonths` cover recomputation, while `GoalCrudTest.returnsNotFoundForAnAbsentGoal`, `GoalCrudTest.returnsNotFoundWhenDeletingAnAbsentGoal`, and `GoalCrudTest.returnsNotFoundForAnotherAccountsGoal` cover refusal. The six criteria are verified by `GoalCrudTest` (AC-1, AC-3, AC-5), `GoalProgressCalculatorTest` and `GoalProgressTest` (AC-2), and `GoalListTest` (AC-4, AC-6).