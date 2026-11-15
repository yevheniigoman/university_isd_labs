# Task 9 — Stage 4, Ticket 6: Training creation endpoint

Coding agent: OpenCode.
Scope: ticket 6 (create handler) from `docs/plan.md`, backend only.

## Decisions

1. **Backend-only slice: `POST /api/trainings`.** The project delivers the REST API only, so the ticket's form is omitted. The server-side create handler, validation, persistence, and response are implemented in full. Requirements: REQ-F-4, REQ-F-5, REQ-F-6, REQ-F-7.

2. **Ownership comes from the session principal, never from the request body.** `TrainingController` passes `Authentication.getName()` (the account email) to `TrainingService`, which resolves the `UserAccount` and stores it as the Training's owner. A client cannot create a Training for another account. Requirements: REQ-F-4, REQ-F-14.

3. **The request DTO carries `BigDecimal` for Distance and Duration.** `TrainingRequest` uses `LocalDate` for the Date and `BigDecimal` for both numeric fields so the ticket 5 validators receive exact decimal input rather than binary floating-point approximations. Requirements: REQ-F-5, REQ-F-6, REQ-F-7.

4. **Every field passes through its ticket 5 validator before persistence.** `TrainingService.create` validates the Date, normalises the Distance to metres, and truncates the Duration to seconds in that order. A rejected field aborts the transaction, so no partial Training is stored. Requirements: REQ-F-5, REQ-F-6, REQ-F-7, REQ-F-4 (AC-2 for anonymous callers via the security filter chain).

5. **`TrainingValidationException` maps to `400` with the documented `Error` body.** The handler was added to `ApiExceptionHandler`, so all three validators' messages reach the client unchanged, matching the examples in `docs/openapi.yml`. Requirements: REQ-F-5, REQ-F-6, REQ-F-7.

6. **The response presents Distance in kilometres to two decimal places.** `TrainingResponse.from` converts the stored metres with `BigDecimal.valueOf(metres, 3).setScale(2, UNNECESSARY)`. The invariant that accepted distances are whole multiples of 10 m (REQ-F-6) makes the conversion exact; `UNNECESSARY` fails loudly if that invariant is ever broken. Requirement: REQ-F-6.

7. **Creation returns `201 Created` with a `Location` header.** The header is `/api/trainings/{id}`, matching the `{id}` resource documented in `docs/openapi.yml` for the read, edit, and delete operations that follow in later stages. Requirement: REQ-F-4.

8. **`TrainingService.create` is transactional.** Validation and persistence run in one transaction, so a validation failure cannot leave a partially written row. Requirement: REQ-F-4.

9. **A missing authenticated account raises `IllegalStateException`.** This can only occur if the session principal outlives its account row; it is treated as a server fault rather than a user validation error. Requirement: REQ-F-4.

10. **The repository gains `findAllByOwnerEmail`.** The derived query supports the ownership assertions in the controller tests and is the same lookup the history view (ticket 8) will need. Requirement: REQ-F-4.

11. **Fractional Duration input is tolerated and truncated, not rejected.** The documented `TrainingInput` schema describes `durationSeconds` as an integer, but the endpoint accepts a fractional JSON number and applies the REQ-F-7 truncation rule. This keeps the HTTP layer consistent with the ticket 5 validator rather than duplicating a second rule. Requirement: REQ-F-7.

12. **The lazy owner association is not traversed when building the response.** The response contains only the Training's own fields, so `open-in-view: false` remains safe and no owner data leaks into the payload. Requirements: REQ-F-4, REQ-F-14.

## Specification changes

None. The ticket's frontend form is out of scope for a backend-only deliverable.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-4, REQ-F-5, REQ-F-6, REQ-F-7, REQ-F-14, REQ-DP-1, REQ-DP-2.

## Open item

REQ-F-4 AC-1 also requires the created Training to "appear in their Training history", which is ticket 8 (Stage 5). The create slice cannot fully close that criterion until the history endpoint exists.
