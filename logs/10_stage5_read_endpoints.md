# Task 10 — Stage 5, Tickets 9 and 8: Training details and history endpoints

Coding agent: OpenCode.
Scope: ticket 9 (Training details) and ticket 8 (Training history) from `docs/plan.md`, backend only.

## Decisions

1. **Backend-only read endpoints.** `GET /api/trainings/{id}` returns one Training and `GET /api/trainings` returns the history, matching `docs/openapi.yml`. The pages, row links, and Edit/Delete controls are omitted because the deliverable is the REST API. Requirements: REQ-F-8, REQ-F-9.

2. **Details are looked up by identifier *and* owner in one query.** `findByIdAndOwnerEmail` prevents a cross-account identifier from ever loading another account's row, rather than loading first and comparing afterwards. Requirement: REQ-F-14.

3. **A missing Training and another account's Training produce the same `404 "Training not found."`.** Both cases are indistinguishable to the caller, so the endpoint cannot be used to probe which identifiers exist. Requirements: REQ-F-9, REQ-F-14.

4. **History is read in `date DESC, id DESC` order.** Date descending satisfies "newest first"; the identifier is a deterministic tie-breaker for two Trainings on the same date, so repeated requests return a stable order. Requirement: REQ-F-8.

5. **Month groups are formed in encounter order with a `LinkedHashMap`.** Because the rows arrive newest first, the first appearance of each `YearMonth` is its newest Training, which yields newest Month first without a second sort. Requirement: REQ-F-8.

6. **Each group's total is delegated to `MonthTotalAggregator`.** The Stage 3 component recomputes from the same row set on every request, so history never persists or caches a total (REQ-F-12). It also keeps the Month partition rule in one place for the chart ticket. Requirements: REQ-F-8, REQ-F-12.

7. **No pagination, truncation, or limit is applied.** The repository returns the full owner-scoped list and the response embeds all of it. A 30-Training test guards against an accidental default page size; the REQ-NF-4 design target remains roughly 5,000 Trainings per account. Requirements: REQ-F-8, REQ-NF-4.

8. **An empty history is `{"months": []}`.** With no frontend, the empty array is the API's representation of "no Trainings have been recorded"; the response shape is unchanged for populated and empty histories. Requirement: REQ-F-8 (AC-2).

9. **Both read paths reuse `TrainingResponse`.** Distance is presented in kilometres to two decimal places from the stored metres, so details and history rows cannot diverge in units or formatting. Requirements: REQ-F-6, REQ-F-8, REQ-F-9.

10. **Read services are `@Transactional(readOnly = true)`.** This matches the read-only intent and avoids dirty-checking work on large histories. Requirement: REQ-F-8.

11. **Details include the identifier alongside Date, Distance, and Duration.** REQ-F-9 requires the three recorded fields; the identifier is also needed by the client to address the later edit and delete operations. Requirement: REQ-F-9.

12. **Ticket 6's forward dependency is now closed.** A created Training is persisted by the Stage 4 handler and is returned by `GET /api/trainings`, satisfying REQ-F-4 AC-1's "appears in their Training history". Requirements: REQ-F-4, REQ-F-8.

## Specification changes

None.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-4, REQ-F-6, REQ-F-8, REQ-F-9, REQ-F-12, REQ-F-14, REQ-NF-4, REQ-DP-1, REQ-DP-2.
