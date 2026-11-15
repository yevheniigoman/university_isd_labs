# Task 12 — Stage 7, Ticket 12: Month Comparison Chart data

Coding agent: OpenCode.
Scope: ticket 12 (chart data) from `docs/plan.md`. Ticket 13 (chart rendering) is a frontend concern and is not implemented.

## Decisions

1. **Backend-only chart data: `GET /api/month-comparison`.** The endpoint returns the twelve points required by `docs/openapi.yml`. Drawing the bars is ticket 13 and is outside a backend-only deliverable. Requirements: REQ-F-13.

2. **The range is a pure calendar function, separate from the user's data.** `MonthComparisonRange.endingAt(YearMonth)` returns exactly twelve Months, oldest first, ending with the supplied Month. Extracting it makes the REQ-DP-1 obligations — range calculation, twelve-Month length, and ordering — testable without a database or a user. Requirements: REQ-F-13 (AC-1, AC-2).

3. **The current Month comes from the injected `Clock`.** `MonthComparisonService` uses `YearMonth.now(clock)`, the same clock bean that governs the Training date ceiling. Production therefore uses the server clock, and the range is computed per request rather than fixed at startup. Requirements: REQ-F-13.

4. **The range is independent of the user's Trainings.** The service builds the twelve points from the calendar and only looks up each Month's total. A user with no Trainings, recent Trainings only, or more than twelve Months of history receives the same twelve Month labels. Requirements: REQ-F-13 (AC-1).

5. **Every Month in the range is emitted, including Empty Months.** `getOrDefault(month, 0L)` substitutes zero, so a gap Month appears between its neighbours rather than being omitted. Requirements: REQ-F-13 (AC-3).

6. **Each point is that Month's total alone.** Totals come from `MonthTotalAggregator.totalMetresByMonth`, which sums stored metres per Month; no running total is accumulated and no target value exists in the response. A cumulative chart or goal line therefore cannot be produced from this payload. Requirements: REQ-F-12, REQ-F-13.

7. **Only the authenticated account's Trainings contribute.** The service reads `findAllByOwnerEmail` for the session principal, so another user's Month totals never appear. Requirements: REQ-F-13, REQ-F-14.

8. **The response is ordered oldest first.** `MonthComparisonRange` emits oldest to current and the service maps it in order, so the renderer can draw the list as received and satisfy AC-2 without re-sorting. Requirement: REQ-F-13 (AC-2).

9. **Metre-to-kilometre conversion is centralised.** `MonthTotalAggregator.toKilometres(long)` is now the single conversion used by both the history totals and the chart points, preserving the exact two-decimal conversion invariant. Requirements: REQ-F-6, REQ-F-12, REQ-F-13.

10. **The chart query is read-only.** `@Transactional(readOnly = true)` matches the history read path. Requirement: REQ-F-13.

11. **Ticket 13 is not implemented.** Bar rendering, the 360 px legibility constraint, and abbreviated labels belong to the frontend. This ticket stops at the ordered data contract; REQ-NF-2 has no acceptance criterion of its own and is carried by the renderer. Requirements: REQ-F-13, REQ-NF-2 (out of scope).

## Specification changes

None.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-6, REQ-F-12, REQ-F-13, REQ-F-14, REQ-DP-1, REQ-DP-2.
