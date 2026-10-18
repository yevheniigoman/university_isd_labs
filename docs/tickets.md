# Tickets — Running Tracker v1

**Source:** [`spec/SPEC_running_tracker_v1.md`](../spec/SPEC_running_tracker_v1.md)
**Date:** 2026-10-02
**Conventions:** `spec/SPEC_running_tracker_v1.md`, REQ-DP-2.

Primary decomposition of the approved specification into implementation tickets. Every ticket traces to one or more requirement identifiers. No ticket introduces a feature absent from the specification.

Acceptance criteria are quoted from the specification's [Appendix B](../spec/SPEC_running_tracker_v1.md#appendix-b-acceptance-criteria-index). Requirements whose behaviour is conventional carry no criteria, so their tickets close against requirement prose alone.

---

## Overview

| # | Ticket | Group | Source | Depends on |
|---|---|---|---|---|
| 1 | Users table | Auth | REQ-NF-3, REQ-NF-1, REQ-F-1 | — |
| 2 | Registration page and handler | Auth | REQ-F-1, REQ-NF-1 | 1 |
| 3 | Login page and handler | Auth | REQ-F-2, REQ-F-3, REQ-NF-1 | 1, 2 |
| 4 | Trainings table | Training | REQ-NF-3, REQ-F-4, REQ-F-6, REQ-F-7 | 1 |
| 5 | Training field validation | Training | REQ-F-5, REQ-F-6, REQ-F-7 | 4 |
| 6 | Training form and create handler | Training | REQ-F-4 | 3, 4, 5 |
| 7 | Month total distance aggregation | History | REQ-F-12 | 4 |
| 8 | Training history page | History | REQ-F-8 | 3, 7, 9 |
| 9 | Training details page | Details | REQ-F-9 | 3, 4 |
| 10 | Training edit | Details | REQ-F-10 | 5, 6, 9 |
| 11 | Training delete | Details | REQ-F-11 | 4, 9 |
| 12 | Month Comparison Chart data | Chart | REQ-F-13 | 7 |
| 13 | Chart rendering | Chart | REQ-F-13, REQ-NF-2 | 12 |

Fourteen functional requirements are covered. `REQ-F-14` (account data isolation) is not a standalone ticket; its ownership check is carried by tickets 9, 10, and 11, which are the only flows that act on a client-supplied Training identifier.

---

## Auth

### 1. Users table

Create the `users` table: identifier, email, password hash. The email column is unique. Email comparison is case-insensitive, so the normalisation rule is owned here and inherited by both registration and login — otherwise a user could register `RUNNER@EXAMPLE.COM` and fail to log in as `runner@example.com`.

The table stores no plaintext password column.

**Sources:** REQ-NF-3 (two tables: users, trainings), REQ-NF-1 (BCrypt), REQ-F-1 (case-insensitive, one account per email).

### 2. Registration page and handler

Email and password form. On submit, hash the password with BCrypt and create the account if the email is not already registered.

The response shall never disclose whether the submitted email is registered. This is the anti-enumeration rule, and it is shared with ticket 3 — see the note below.

**Sources:** REQ-F-1, REQ-NF-1.

**Acceptance criteria:** none. REQ-F-1 carries no criteria in Appendix B, so this ticket closes against requirement prose alone.

### 3. Login page and handler

Email and password form. Verify the password against the stored BCrypt hash. On success, establish a server-maintained session and present the authenticated views.

Failure shall produce the single generic message "Invalid email or password", regardless of whether the email is unregistered or the password is incorrect.

Session state resides on the server and is not client-modifiable. Sessions carry no lifetime and the client cookie is persistent, so closing the browser does not end the session.

**Sources:** REQ-F-2, REQ-F-3, REQ-NF-1.

**Acceptance criteria:** none. REQ-F-2 and REQ-F-3 carry no criteria.

**Shared concern across tickets 2 and 3.** REQ-F-1 forbids registration from disclosing whether an email exists; REQ-F-2 requires login to return one indistinguishable message. These are the same defence expressed twice. A change could satisfy one and break the other, so a single test covering both paths is worth having.

---

## Training

### 4. Trainings table

Create the `trainings` table: identifier, date, distance, duration, and a foreign key to the owning user.

Distance is stored as an integer number of metres and duration as a whole number of seconds. Settling the units here is what makes ticket 5's validators free of unit conversion, and what preserves REQ-F-6's lossless round-trip guarantee.

The date column stores a calendar date only — no time and no timezone.

**Sources:** REQ-NF-3, REQ-F-4 (distinct identity), REQ-F-6 (integer metres), REQ-F-7 (whole seconds).

### 5. Training field validation

Three validators, each contractual under REQ-DP-1:

- **Date** — rejected if later than the server's current date plus one day. No earliest date is imposed. The clock must be injectable so the boundary is testable.
- **Distance** — accepted for `[0.01 km, 100 km]`, normalised to whole metres on input. `5` becomes `5000` m; `5.2` becomes `5200` m. Values outside the range are rejected.
- **Duration** — required and greater than zero, stored in whole seconds. A sub-second component is truncated, never rounded.

REQ-F-5 states that the ceiling applies "irrespective of what the submitting client displayed as selectable", so this ticket owns the server-side rule including the tolerance boundary. The form's narrower ceiling lives in ticket 6.

The 0–59 constraint on minutes and seconds is a user-interface input convention only (REQ-F-7) and therefore belongs to ticket 6, not here.

**Sources:** REQ-F-5, REQ-F-6, REQ-F-7.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-5 | AC-1 (rejection beyond `serverToday + 1`, including requests bypassing the form), AC-2 (any earlier date, the current date, or up to the ceiling accepted) |
| REQ-F-6 | AC-1 (outside `[0.01 km, 100 km]` rejected), AC-2 (`0.01 km`–`100 km` accepted and stored in whole metres) |
| REQ-F-7 | none |

### 6. Training form and create handler

Form with date, distance, and duration input. On submit, run the ticket 5 validators and persist the Training to the ticket 4 table.

The date picker offers dates through the client's current date. This ceiling is narrower than the server's by one day, deliberately, because the one-day tolerance exists to absorb clock skew between client and server.

Minutes and seconds presented for entry each fall within 0–59.

**Sources:** REQ-F-4, REQ-F-5, REQ-F-6, REQ-F-7.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-4 | AC-1 (valid Date, Distance, and Duration creates the Training), AC-2 (an unauthenticated user cannot create a Training) |

**Forward dependency.** REQ-F-4 AC-1 asserts the created Training "appears in their Training history", which is ticket 8. This ticket cannot fully close until history exists.

---

## History

### 7. Month total distance aggregation

Compute a Month's total as the sum of the Distances of all Trainings dated within it, from the stored Trainings at the time of request. Never persisted or cached.

This ticket also owns the partition-by-Month logic, so ticket 12 can reuse it rather than reimplementing the same `YearMonth` extraction.

**Sources:** REQ-F-12.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-12 | AC-1 (5.20 km + 4.80 km = 10.00 km), AC-2 (a Month with no Trainings totals zero) |

### 8. Training history page

List all of the user's Trainings, newest first, grouped by Month. Each Month group is headed by that Month's total distance, taken from ticket 7.

The list displays every Training without pagination, truncation, or an imposed limit. REQ-NF-4 sets the design target at approximately 5,000 Trainings per account.

A user with no Trainings is informed that none have been recorded.

**Sources:** REQ-F-8, REQ-NF-4.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-8 | AC-1 (newest first, Month groups headed by total distance), AC-2 (no Trainings → informed that none exist) |

Each row links to ticket 9. Edit and Delete actions are ticket 10 and ticket 11; this ticket renders the list and links out.

---

## Details and mutations

All three tickets act on a Training identified by an identifier supplied by the client, which is the attack surface REQ-F-14 names. Each must verify the Training belongs to the session's account before acting.

### 9. Training details page

Fetch one Training by identifier and render its Date, Distance, and Duration. Present the Edit and Delete actions.

This is the navigation entry point: history → details → Edit or Delete. It is therefore the first place a cross-account identifier would be accepted, and where the ownership check carries the most weight.

**Sources:** REQ-F-9.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-9 | AC-1 (Date, Distance, and Duration are shown) |

### 10. Training edit

Present the ticket 6 form pre-filled with the Training's current values. On submit, run the same validators as creation and persist the update.

Not a new form — the existing one in a second mode. All validation rules governing creation equally govern update.

**Sources:** REQ-F-10.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-10 | AC-2 (the form is presented pre-filled with the Training's current values) |

**Cross-Month edits.** REQ-F-5 permits re-dating a Training into a different Month, and the affected Month totals must follow. Because totals are derived (ticket 7), this holds automatically provided the update persists the new date correctly — worth asserting here rather than discovering the fault in the chart.

### 11. Training delete

Present the modal confirmation "Do you want to delete this training?" and remove the Training permanently on affirmation.

Derived figures need no correction step: Month total distance is never stored, so removing a Training is reflected automatically on the next request.

**Sources:** REQ-F-11.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-11 | AC-1 (no deletion occurs until the user affirms) |

**Coverage note.** Permanence of deletion is stated in REQ-F-11's prose but verified by no acceptance criterion, and is not among REQ-DP-1's mandated test targets. It is the only guarantee in the specification covered by neither.

---

## Chart

### 12. Month Comparison Chart data

Derive the twelve Months beginning with the Month eleven months preceding the current Month and ending with the current Month. The range is independent of the user's Trainings. For each Month, take the total distance from ticket 7, substituting zero for an Empty Month. Emit the list ordered oldest Month first.

This ticket covers all three of REQ-DP-1's test-first obligations for this feature: the range calculation and its twelve-Month length, Empty Month zero-filling, and bar ordering. Emitting the list already ordered means ordering is settled here rather than left to the renderer.

Because the data carries per-month totals only, a cumulative chart cannot be produced downstream — REQ-F-13's prohibition on cumulative totals and target lines is satisfied by construction.

**Sources:** REQ-F-13.

**Acceptance criteria:**

| Requirement | Criteria |
|---|---|
| REQ-F-13 | AC-1 (twelve bars covering the twelve Months ending with the current Month, whether the user has no Trainings, recent Trainings only, or more than twelve Months of them), AC-2 (oldest leftmost, current rightmost), AC-3 (a gap Month appears at zero) |

### 13. Chart rendering

Draw twelve bars from the ticket 12 data, in the order given.

The chart shall remain legible at a 360 px viewport. With twelve bars always shown, this requires either horizontal scrolling or abbreviated labels.

**Sources:** REQ-F-13, REQ-NF-2.

**Acceptance criteria:** none beyond those closed by ticket 12; the rendering constraint is carried by REQ-NF-2, which has no criteria of its own.