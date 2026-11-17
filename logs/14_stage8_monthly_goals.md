# Task 14 — Stage 8, Ticket 14: Monthly distance goals

Coding agent: OpenCode.
Scope: implement `REQ-F-15` from `spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md` test-first. Ticket 13 (chart rendering) remains a frontend concern and is not implemented.

## Decisions

1. **Tests were written before any production class.** Five test classes were added first — `GoalTargetValidatorTest`, `GoalProgressCalculatorTest`, `GoalCrudTest`, `GoalProgressTest`, `GoalListTest` — and the suite was run while the production classes did not exist. It failed on `cannot find symbol` for every goal type, confirming red for the intended reason. Implementation followed only after that run. Requirement: REQ-DP-1.

2. **The Goal Month is stored as `varchar(7)` `YYYY-MM`, not as a date.** A Goal Month carries no day, so a date column would store a meaningless day and invite off-by-one conversions around month ends. `YearMonthConverter` maps `YearMonth` to its `toString()` form, which a plain string sort also orders chronologically. Requirement: REQ-F-15.

3. **One goal per account per Month is enforced by the database, not only by the service.** `monthly_goals` carries a unique constraint on `(user_id, goal_month)`. The service still performs find-then-update so a replacement reuses the existing row, but the constraint means a bug in that path cannot silently create a duplicate. Requirement: REQ-F-15 (AC-3).

4. **A rejected Target leaves the stored goal untouched.** `GoalTargetValidator` runs before the row is fetched or mutated, so a `PUT` with an out-of-range target returns `400` and the previous target survives. `GoalCrudTest.rejectsAnOutOfRangeTargetAndLeavesTheGoalUnchanged` pins this ordering. Requirement: REQ-F-15 (AC-1).

5. **The Target validator is separate from the Training Distance validator.** Both check range first and then truncate toward zero beyond two decimal places, but the bounds differ: `[0.01, 1000]` km versus `[0.01, 100]` km. Sharing one class would have meant a parameterised bound with no second caller; the two small classes keep each requirement's rule readable. Requirement: REQ-F-15 (AC-1, AC-2).

6. **Only the Target distance is persisted.** Completed distance comes from `MonthTotalAggregator` over the account's current Trainings, Remaining distance is derived, and Percent complete is derived. No progress figure is stored, so creating, editing (including re-dating across Months), or deleting a Training needs no correction step — the same property Month totals already have. Requirement: REQ-F-12, REQ-F-15 (AC-4).

7. **Remaining distance is floored at zero and Percent complete is uncapped.** Both are computed in `GoalProgressCalculator` and both directions are tested: `25 km` against a `20 km` target reports `remaining = 0.00` and `percent = 125.0`. Rounding is `HALF_UP` to one decimal place, matching the addendum. Requirement: REQ-F-15 (AC-4, AC-5).

8. **Progress arithmetic stays in whole metres until the boundary.** `MonthTotalAggregator.toKilometres` performs the only metre-to-kilometre conversion, so targets and completed totals share one exact two-decimal conversion and no binary floating-point error enters the percentage. Requirement: REQ-F-6, REQ-F-15.

9. **Listing reuses the Month Comparison Range.** `GET /api/goals` filters the account's goals to `MonthComparisonRange.endingAt(YearMonth.now(clock))`, oldest first, and omits Months without a goal. Goals outside the range remain stored and addressable by `GET /api/goals/{month}`; the range is a presentation bound, not a validity bound. Requirements: REQ-F-13, REQ-F-15 (AC-6).

10. **The listing reads the Trainings once.** `GoalService.list` builds a Month-to-metres map with `MonthTotalAggregator.totalMetresByMonth` and looks each goal up in it, rather than re-scanning the account's Trainings per goal. Requirement: REQ-F-15 (AC-6).

11. **An absent goal and another account's goal are the same `404 "Goal not found."`.** Every query is owner-scoped at the repository (`findByOwnerEmailAndGoalMonth`), and `GoalNotFoundException` carries one fixed message, so the response cannot confirm that a goal exists under another account. Requirement: REQ-F-14, REQ-F-15.

12. **A malformed `{month}` is a `400`, not a `404`.** `GoalController` parses the path with `YearMonth.parse` and converts `DateTimeParseException` into `GoalValidationException("Month must be a valid year and month (YYYY-MM).")`. A syntactically invalid Month never reaches the repository. Requirement: REQ-F-15.

13. **The Month Comparison Chart was not touched.** `MonthComparisonService` and its payload are unchanged, so no target or goal line can appear on the chart, as `REQ-F-13` requires. Goals are exposed only through the four goal endpoints. Requirement: REQ-F-13.

14. **Two new exception handlers, mirroring the Training precedent.** `ApiExceptionHandler` maps `GoalValidationException` to `400` and `GoalNotFoundException` to `404`, each returning the documented `Error` body. Requirement: REQ-F-15.

15. **Existing test fixtures now clear `monthly_goals` before `users`.** The new foreign key made the full suite fail with a referential-integrity violation: goal tests left rows behind, then later classes' `users.deleteAll()` could not delete the owners. The eight affected test classes now delete goals first. Production code was not changed to cascade deletes, because no account-deletion path exists and an unused cascade would only mask fixture problems. Requirement: REQ-DP-1.

16. **`docs/openapi.yml` was extended rather than regenerated.** Version `1.1.0`, a `Goals` tag, the four goal paths, and the `GoalInput`, `GoalProgress`, and `GoalListResponse` schemas were added, and the description now states that goals never appear on the chart. Requirement: REQ-F-15.

## Verification

- Goal suite: `mvn -B test "-Dtest=GoalTargetValidatorTest,GoalProgressCalculatorTest,GoalCrudTest,GoalProgressTest,GoalListTest"` — 36 tests, 0 failures, 0 errors.
- Full suite: `mvn -B test` — 122 tests, 0 failures, 0 errors (the previous 86 plus the 36 new).
- `docs/openapi.yml` parses as YAML: 8 paths, 13 schemas.

## Specification changes

- None. `spec/SPEC_running_tracker_v1.md` and the addendum are unchanged by this task; the requirement and its six criteria were fixed in task 13.

## Risks observed

- Two concurrent `PUT /api/goals/{month}` requests for the same account and Month can both take the insert path and one will violate the unique constraint, surfacing as a `500` rather than a replacement. The window is small and no criterion covers concurrency; a retry-on-conflict or an upsert would close it if the feature ever needs it.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-6, REQ-F-12, REQ-F-13, REQ-F-14, REQ-F-15, REQ-NF-3, REQ-DP-1, REQ-DP-2.
