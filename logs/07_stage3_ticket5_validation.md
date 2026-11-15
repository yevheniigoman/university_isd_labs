# Task 7 — Stage 3, Ticket 5: Training field validation

Coding agent: OpenCode.
Scope: ticket 5 (Training field validation) from `docs/plan.md`.

## Decisions

1. **Three focused validators, one per field.** `TrainingDateValidator`, `TrainingDistanceValidator`, and `TrainingDurationValidator` each own one rule and are independently testable. This matches the ticket's decomposition and keeps Stage 4's create/update handlers free of validation logic. Requirements: REQ-F-5, REQ-F-6, REQ-F-7.

2. **Date uses an injected `Clock`.** The ceiling is `LocalDate.now(clock).plusDays(1)`. A `Clock` bean supplies the server clock in production, while tests use `Clock.fixed`, so the one-day boundary is deterministic rather than dependent on the wall clock. Requirements: REQ-F-5.

3. **Date has no lower bound.** Only dates after the ceiling are rejected; any past date is accepted, as the specification explicitly forbids an earliest date. Requirement: REQ-F-5.

4. **Distance is validated and normalised with `BigDecimal`.** Binary floating point would make "at most two decimal places" and "lossless metres" unreliable. `movePointRight(3)` converts km to m exactly for accepted input (`5` → `5000`, `5.2` → `5200`, `0.01` → `10`). Requirements: REQ-F-6.

5. **Precision beyond two decimal places is truncated toward zero, not rejected.** The range is checked against the submitted value first, so `100.009` remains out of range, while an in-range `5.239` becomes `5230` m and `5.999` becomes `5990` m. Truncation never increases the distance, so the stored value is never longer than the submitted one. This revises an earlier reject-on-extra-precision rule at the user's request. Requirement: REQ-F-6.

6. **Duration truncates toward zero and must remain positive after truncation.** `RoundingMode.DOWN` guarantees the stored value is never longer than the submitted one. `0.9` therefore truncates to `0` and is rejected rather than stored as a zero-second Training. Requirement: REQ-F-7.

7. **Duration is guarded against the integer storage range.** The entity stores whole seconds in an `int`; values above `Integer.MAX_VALUE` are reported as a validation error instead of failing with an arithmetic exception. Requirement: REQ-F-7.

8. **Validators are Spring components; `TimeConfig` supplies the `Clock`.** Stage 4 can inject them directly into the create/update handlers. Requirement: REQ-DP-1.

9. **Error messages match the documented examples.** "Date must not be later than tomorrow." and "Distance must be between 0.01 and 100 km." come from `docs/openapi.yml`; the remaining messages follow the same user-facing style. Requirements: REQ-F-5, REQ-F-6.

## Specification changes

None.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-5, REQ-F-6, REQ-F-7, REQ-DP-1, REQ-DP-2.
