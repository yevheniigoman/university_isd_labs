# Task 8 — Stage 3, Ticket 7: Month total distance aggregation

Coding agent: OpenCode.
Scope: ticket 7 (Month total distance aggregation) from `docs/plan.md`.

## Decisions

1. **A stateless `MonthTotalAggregator` component owns the aggregation.** It takes the current Trainings as an argument on every call and stores no totals, satisfying REQ-F-12's "computed at the time it is requested, not persisted or cached". Requirements: REQ-F-12.

2. **Totals are summed in whole metres and converted once at the boundary.** `totalMetres` accumulates the stored `distanceMetres` in a `long`; `totalKilometres` divides the finished total by 1000. No per-Training conversion occurs inside the aggregation, as REQ-F-12 requires a single consistent unit. Requirement: REQ-F-12.

3. **The Month is derived from the Training's Date with `YearMonth.from`.** `monthOf(Training)` is public and static so ticket 12 can reuse the same partition rule instead of reimplementing it. Requirement: REQ-F-12.

4. **`totalMetresByMonth` returns a `TreeMap`, oldest Month first.** A deterministic order gives the chart its required oldest-to-newest sequence and lets the history view reverse it for newest-first display. Requirements: REQ-F-12, REQ-F-13.

5. **A Month with no Trainings returns zero.** `totalMetres` returns `0` and `totalKilometres` returns `0.00` without special-casing the caller. Requirements: REQ-F-12 (AC-2).

6. **Kilometres are exact to two decimal places.** `setScale(2, RoundingMode.UNNECESSARY)` relies on the REQ-F-6 invariant that every stored distance is a whole multiple of 10 m, so a metre total is always an exact two-decimal kilometre value. It fails loudly rather than silently rounding if that invariant is ever violated. Requirements: REQ-F-6, REQ-F-12.

7. **No cache means recomputation is inherent, not tested separately.** Because the aggregator holds no state and takes the Trainings on every call, an added, removed, or re-dated Training is reflected on the next request by construction. The initial mutation-based tests were removed as self-evident; the remaining tests cover totals, partitioning, empty Months, and year separation. Requirement: REQ-F-12.

## Specification changes

None.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-12, REQ-F-6, REQ-F-13, REQ-DP-1, REQ-DP-2.
