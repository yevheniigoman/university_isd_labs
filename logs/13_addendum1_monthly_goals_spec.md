# Task 13 — Addendum 1: Monthly distance goals (specification only)

Coding agent: OpenCode.
Scope: elicit and document a new middle-sized feature before any test or implementation. No tests or production code were written.

## Decisions

1. **The requirement lives in a separate addendum, not in the parent specification.** `spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md` is a new document; `spec/SPEC_running_tracker_v1.md` is unmodified. The parent remains the authority for everything the addendum does not mention. Requirement: REQ-DP-2.

2. **The new requirement is `REQ-F-15`, continuing the parent's functional series.** No existing identifier is reused or renumbered. Requirement: REQ-F-15.

3. **Target distance is bounded to `[0.01, 1000] km`.** The lower bound matches Training Distance so that a target is a real distance; the upper bound is ten times the Training maximum because a monthly target aggregates many runs. Requirement: REQ-F-15 (AC-1, AC-2).

4. **Target distance is normalised like Training Distance.** Range is checked first, then any component beyond two decimal places is truncated toward zero, and the result is stored in whole metres. Truncation rather than rounding matches the accepted Distance and Duration precedent and guarantees storage never exceeds the submitted target. Requirement: REQ-F-15 (AC-1).

5. **One Monthly Goal per account per Goal Month, enforced by a unique `(user_id, goal_month)` constraint.** Setting a second target for the same Month replaces the first, so there is no way to create a duplicate or to accumulate competing targets. Requirement: REQ-F-15 (AC-3).

6. **Progress is derived, never stored.** Completed distance is the `REQ-F-12` Month total distance; Remaining distance is target minus completed floored at zero; Percent complete is completed over target, rounded half-up to one decimal, uncapped. Deriving the figures means Training creation, edit (including re-dating across Months), and deletion need no correction step, exactly as Month totals already behave. Requirements: REQ-F-12, REQ-F-15 (AC-4, AC-5).

7. **The zero floor and the uncapped percentage are stated separately.** They are opposite failure modes of the same formula and each gets its own criterion, so a future implementation cannot satisfy one by breaking the other. Requirement: REQ-F-15 (AC-4, AC-5).

8. **Listing covers the Month Comparison Range and omits Months without a goal.** Reusing the twelve-Month range keeps goal listing aligned with the chart's notion of "recent". Unlike the chart, which zero-fills Empty Months under `REQ-F-13`, a goal-less Month has no target to report and is omitted. Requirements: REQ-F-13, REQ-F-15 (AC-6).

9. **Goals may be set for any valid Month, including Months outside the listing range.** Restricting the Goal Month would add a rule the feature does not need; the listing range is a presentation bound, not a validity bound. Requirement: REQ-F-15.

10. **An absent goal and another account's goal are both "not found".** The two cases are indistinguishable, matching the Training identifier precedent under `REQ-F-14`, so the response cannot confirm that a goal exists elsewhere. Requirements: REQ-F-14, REQ-F-15.

11. **Goals do not appear on the Month Comparison Chart.** `REQ-F-13` forbids a target or goal line, so the addendum forbids extending the chart payload with goal values and exposes progress only through the goal views. Requirement: REQ-F-13.

12. **`REQ-NF-3`'s table count is amended to three.** The parent says the database comprises two tables; `monthly_goals` makes three. The addendum supersedes only the count and leaves the description of the users and trainings tables intact. Requirements: REQ-NF-3, REQ-F-15.

13. **The addendum carries six acceptance criteria.** Validation, replacement, arithmetic, overshoot, and listing are each covered; the listing criterion (AC-6) exists because the originally sketched set did not cover the twelve-Month, oldest-first, goal-less-Month-omitting list. Requirement: REQ-F-15.

14. **Ticket 14 is added to `docs/tickets.md` with its own acceptance-criteria table and explicit test-first obligations.** The overview now records fifteen covered functional requirements and names the addendum as the source of `REQ-F-15`. Requirement: REQ-DP-2.

15. **No tests and no production code were written.** This task stops at the requirement, its criteria, and the ticket, as instructed. Implementation will follow the project's test-first rule when it is requested. Requirement: REQ-DP-1.

16. **AC-5 and AC-7 were removed on request.** The criteria covering progress after Training mutation (previously AC-5) and refusal of an absent or another account's goal (previously AC-7) were deleted, and the remaining criteria were renumbered to AC-1–AC-6. The requirement prose is unchanged: progress derivation remains mandated by `REQ-F-15` and isolation by `REQ-F-14` and `REQ-F-15`, so the `REQ-DP-1` obligations in addendum section 5.6 still apply to both. Requirement: REQ-DP-2.

## Specification changes

- Added `spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md` defining `REQ-F-15`, its definitions, its acceptance criteria, and its compatibility clauses.
- Amended the table count in `REQ-NF-3` from two to three, recorded in addendum section 5.1 rather than edited into the parent.
- `spec/SPEC_running_tracker_v1.md` itself is unchanged.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-12, REQ-F-13, REQ-F-14, REQ-F-15, REQ-NF-3, REQ-NF-4, REQ-DP-1, REQ-DP-2.
