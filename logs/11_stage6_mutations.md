# Task 11 — Stage 6, Tickets 10 and 11: Training edit and delete endpoints

Coding agent: OpenCode.
Scope: ticket 10 (Training edit) and ticket 11 (Training delete) from `docs/plan.md`, backend only.

## Decisions

1. **Backend-only mutations: `PUT /api/trainings/{id}` and `DELETE /api/trainings/{id}`.** The edit form and the delete confirmation modal are omitted because the deliverable is the REST API. The endpoints match `docs/openapi.yml`. Requirements: REQ-F-10, REQ-F-11.

2. **`PUT` is a full replacement, not a partial patch.** Every request must carry Date, Distance, and Duration, exactly as creation does. This matches the documented `TrainingInput` body and avoids two validation shapes. Requirement: REQ-F-10.

3. **Create and update share one private `validate` helper.** Both paths pass through the same ticket 5 validators and receive the same normalised `(date, metres, seconds)` values, so validation parity is structural rather than a duplicated convention. Requirements: REQ-F-5, REQ-F-6, REQ-F-7, REQ-F-10.

4. **Update validates before mutating the entity.** `TrainingService.update` loads the owner-scoped Training, runs all three validators, and only then calls `Training.update(...)`. A rejected request therefore leaves every stored field untouched; tests assert the original values after each rejection. Requirement: REQ-F-10.

5. **Mutation and validation share the owner-scoped lookup.** `findByIdAndOwnerEmail` is used for update and delete as well as read, so a cross-account identifier cannot be modified or removed. Requirements: REQ-F-10, REQ-F-11, REQ-F-14.

6. **Unknown and cross-account identifiers both return `404 "Training not found."`.** The two cases remain indistinguishable, consistent with the details endpoint. Requirements: REQ-F-10, REQ-F-11, REQ-F-14.

7. **The entity exposes `update(date, metres, seconds)` rather than setters.** The domain method keeps the storage units explicit and states that its inputs are already validated, leaving the validation rule in the service layer. Requirement: REQ-F-10.

8. **Cross-Month edits are verified through the history endpoint.** Moving one Training from the previous Month to the current Month changes both groups' totals on the next request, with no correction step, because totals are derived (Stage 3) rather than stored. Requirements: REQ-F-5, REQ-F-10, REQ-F-12.

9. **Delete is permanent and returns `204`.** `TrainingRepository.delete` removes the row; no soft-delete flag or restore path is introduced, matching REQ-F-11's "no mechanism for restoring". Requirement: REQ-F-11.

10. **Deletion needs no figure correction.** Because Month totals are derived from the stored Trainings and never persisted, removing a row is reflected on the next request by construction. That recomputation is therefore not asserted separately; the delete test asserts the row is gone. Requirements: REQ-F-11, REQ-F-12.

11. **The confirmation prompt is a client responsibility.** REQ-F-11 AC-1 concerns the UI flow; a backend cannot prove the user saw a modal. The API performs a deletion only when an explicit `DELETE` request is made, and the decision log records that the modal itself is out of scope for this backend-only deliverable. Requirement: REQ-F-11.

12. **Unauthenticated mutations are rejected before the controller.** The existing security filter chain returns the documented `401`, and tests assert that the row is unchanged or still present. Requirements: REQ-F-10, REQ-F-11, REQ-F-14.

## Specification changes

None. The edit form and delete confirmation are omitted as part of the backend-only scope; the REST contract is unchanged.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-5, REQ-F-6, REQ-F-7, REQ-F-10, REQ-F-11, REQ-F-12, REQ-F-14, REQ-DP-1, REQ-DP-2.
