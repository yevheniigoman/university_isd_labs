# SKED Elicitation Log — Running Tracker v1

**Task:** Socratic elicitation of the running tracker specification by requirement ID with acceptance criteria, producing `spec/SPEC_running_tracker_v1.md`.
**Date:** 2026-10-02
**Conventions:** `spec/SPEC_running_tracker_v1.md`, REQ-DP-2.

This entry records decisions and their reasoning. It does not record files read or written, nor terminal commands executed.

---

## 1. Main uncertainties revealed

**Whether authentication was warranted for a single individual.** The description requires accounts and login while naming a single-user product. Resolved as a genuine requirement once multi-device access was confirmed; had the answer been single-device, the whole of REQ-F-1 through REQ-F-3 would have collapsed into a local profile lock.

**What the bar chart actually compared.** "Total distance for each month" and "the values show the total distance in kilometers" were each readable as a per-month sum or a running cumulative total. A cumulative bar chart rises monotonically by construction and so cannot depict a decrease — making it useless for the stated purpose. Human confirmed per-month totals, with no target line.

**How many Months the chart should display.** Initially framed as a fixed twelve-month window, then treated as derived from the user's earliest Training. Both were replaced by a rule with no dependency on user data: the range is always the twelve Months ending with the current Month. This removed the empty-user and short-history special cases entirely. See REQ-F-13.

**Whether "Run" or "Training" was canonical.** The description used both interchangeably. Human chose **Training** as canonical, and the term is now retired from specification and UI wording.

**Whether Month total distance should be persisted.** Not stated anywhere in the description. A stored total has three independent routes to staleness once Trainings become editable, deletable, and re-datable across Months. Human confirmed it is computed on every request and never stored. See REQ-F-12.

**Whose clock bounds a Training's Date.** A user in a timezone ahead of the server's cannot record today's run under a strict `serverToday` ceiling — blocked near local midnight. A one-day tolerance was chosen over either trusting the client (no server enforcement) or strict server-side comparison. Storing the Date with a timezone was considered and rejected: it does not affect the validation decision, which is taken before storage, and it would make Month assignment ambiguous. See REQ-F-5.

**Whether a Date floor should exist.** Proposed as `01-01-2000`, then reconsidered and removed at both layers. Neither the form nor the server imposes an earliest date; the only bound is the ceiling. See REQ-F-5.

**Whether validation and session expiry are product requirements.** Both were initially written as requirements. Human judged them trivial and generic to web applications, and both were removed.

**Whether Training could record activity other than a run.** Bicycle riding was raised as a use case, which would have required a type field and made Month total distance ambiguous (combined, or reported per activity). Human resolved v1 as runs only, with no type field, so each Month retains a single unambiguous total. See REQ-F-12, REQ-F-13.

**Whether storage units are requirements, and at what layer.** Distance in integer metres and Duration in whole seconds are stated as behavioural guarantees in REQ-F-6 and REQ-F-7 — needed because they make accepted input round-trip losslessly. Their removal from REQ-NF-3 followed from the judgement that a database column type is an implementation decision, not a quality attribute. Sub-second input is truncated rather than rounded, so a stored Duration is never longer than submitted.

**Whether the geographic scope should be a requirement.** Raised as a Ukraine-only deployment intended to remove timezone handling. Human chose not to add it, leaving REQ-F-5's tolerance standing without a stated deployment basis. This also removed the only anchor for the server's notion of "today."

**Provenance of specification content.** Two disagreements emerged between the original description and work already merged on `main`; the specification's Date-over-time reading prevailed. See `logs/02_merge_conflict_featuretraining_to_main.md`.

---

## 2. Decisions taken by the human

| # | Decision | Requirement |
|---|---|---|
| 1 | Multi-device login implies real server-side authentication, not a local lock. | REQ-F-1…3 |
| 2 | Bar chart shows per-month totals only; no cumulative series, no target line. | REQ-F-13 |
| 3 | Month Comparison Range is always the twelve Months ending with the current Month, independent of the user's Trainings. | REQ-F-13 |
| 4 | Bars ordered oldest Month on the left, current Month on the right. | REQ-F-13 |
| 5 | Empty Months render as `0` rather than being omitted. | REQ-F-13 |
| 6 | Canonical term is **Training**; **Run** retired. | Appendix A |
| 7 | A Training requires distance and duration both greater than zero. | REQ-F-4, REQ-F-7 |
| 8 | No Date floor exists at either layer; the form's ceiling is the client's current date, the server's is `serverToday + 1`. | REQ-F-5 |
| 9 | Date rule enforced server-side, including requests bypassing the form. | REQ-F-5 |
| 10 | Distance bounded `[0.01 km, 100 km]`, stored in whole metres. | REQ-F-6 |
| 11 | Distance accepted to two decimal places, normalized rather than rejected. | REQ-F-6 |
| 12 | Duration stored in whole seconds; sub-second components truncated, never rounded. | REQ-F-7 |
| 13 | Trainings are editable and deletable; each carries an identity. | REQ-F-9…11 |
| 14 | Editing presents the form pre-filled. | REQ-F-10 |
| 15 | Deletion is permanent and requires modal confirmation. | REQ-F-11 |
| 16 | History is a reverse-chronological list grouped by Month, unpaginated. | REQ-F-8, REQ-NF-4 |
| 17 | Email addresses are case-insensitive and unique per account. | REQ-F-1 |
| 18 | Passwords stored exclusively as BCrypt hashes. | REQ-NF-1 |
| 19 | No HTTPS, no login rate limiting, no session lifetime, no backups in v1. | §4 |
| 20 | Register and login only; no logout, password change, or password reset in v1. | §4 |
| 21 | Sessions carry no lifetime and the client cookie is persistent, so closing the browser does not end a session. | REQ-F-3 |
| 22 | Responsive web application from 360 px to desktop; no accessibility baseline. | REQ-NF-2 |
| 23 | Relational database, two tables: users and trainings. | REQ-NF-3 |
| 24 | Approximately 5,000 Trainings per account. | REQ-NF-4 |
| 25 | TDD required for core business logic; framework left unselected. | REQ-DP-1 |
| 26 | Agent log records decisions with rationale, one entry per task, referencing requirement IDs. | REQ-DP-2 |
| 27 | Acceptance criteria written for core business requirements only; trivial and generic cases omitted. | Appendix B |
| 28 | Storage data types excluded from non-functional requirements. | REQ-NF-3 |
| 29 | "Unauthenticated user" used throughout; the term "visitor" retired. | REQ-F-1, REQ-F-4 |
| 30 | The **Identity** glossary entry was removed. | Appendix A |

**Consequences accepted as v1 limitations** (recorded in §4 rather than argued against): no logout leaves a session open indefinitely on a shared device, with neither password change nor password reset available as a recovery path. No HTTPS leaves credentials unencrypted in transit, which BCrypt does not address.

---

## 3. Agent assumptions corrected or dismissed

| # | Assumption made | Outcome | Requirement |
|---|---|---|---|
| 1 | Date-only storage with no timezone conversion is a non-functional requirement. | Corrected — non-obvious user-visible behaviour, so reclassified as functional. | REQ-F-5 |
| 2 | Client-side validation must never be stricter than the server's. | Dismissed as trivial and generic; requirement removed, resolving an apparent conflict with the form's narrower Date window. | former REQ-NF-5 |
| 3 | Month total distance is stored in metres. | Corrected — computed from Trainings on each request and never persisted. | REQ-F-12 |
| 4 | Minimum Training distance is `meters >= 10`. | Corrected twice — first `>= 100`, then `>= 10` after precision was fixed at two decimal places. | REQ-F-6 |
| 5 | Empty Months are omitted from the chart. | Corrected — rendered as `0`. | REQ-F-13 |
| 6 | A 12-month distance ceiling is justified by bicycle rides. | Corrected — v1 covers runs only; ceiling reduced to 100 km and its rationale dropped. | REQ-F-6 |
| 7 | "Window" is the correct name for both the chart feature and the month set. | Corrected — split into **Month Comparison Chart** and **Month Comparison Range**. | Appendix A |
| 8 | "Month total" is unambiguous. | Corrected — renamed **Month total distance**. | Appendix A |
| 9 | Sessions end when the browser closes. | Corrected — sessions carry no lifetime and the client cookie is persistent. | REQ-F-3 |
| 10 | TDD and the agent log belong under `REQ-NF`. | Corrected — reclassified as development process constraints. | REQ-DP-1, REQ-DP-2 |
| 11 | The Month Comparison Range is derived from the user's earliest Training. | Corrected — replaced with a fixed twelve-Month range independent of user data, removing two special cases. | REQ-F-13 |
| 12 | A new Training begins in a state with all fields empty. | Corrected — an implementation detail, unreachable through the specified flow, and in conflict with the field-level validation rules. | REQ-F-4 |
| 13 | Duration is "stored as a single elapsed value" was sufficient. | Corrected — unit omitted; now stated as whole seconds with a truncation rule. | REQ-F-7 |
| 14 | A server Date floor of `01-01-2000` should apply. | Corrected — no floor exists at either layer. | REQ-F-5 |
| 15 | Storage units belong in REQ-NF-3. | Dismissed — data types removed; behavioural guarantees retained in REQ-F-6 and REQ-F-7. | REQ-NF-3 |
| 16 | Acceptance criteria should cover cross-Month recomputation, update validation parity, and deletion permanence. | Dismissed as trivial consequences of derivation; prose retained, criteria removed. | REQ-F-5, REQ-F-10, REQ-F-11 |
| 17 | An unauthenticated **visitor** is the correct term. | Corrected — "unauthenticated user" used throughout. | REQ-F-1, REQ-F-4 |
| 18 | Ukraine-only deployment should be recorded as a requirement. | Dismissed. | — |
| 19 | Storing the Date with a timezone would resolve the client/server clock difference. | Dismissed — the validation decision precedes storage, and a timezone-bearing type would make Month assignment ambiguous. | REQ-F-5, REQ-NF-5 |
| 20 | The `Identity` term is fully retired from the glossary and from prose. | Partly corrected — the glossary row was removed, but the word remains in REQ-F-4, REQ-F-14, and Appendix A as ordinary prose. | — |

**Agent errors corrected during the task:** a proposed cumulative-chart reading the human had already ruled out; a count of 13 acceptance criteria where the correct total was 18, arising from an arithmetic error in a summary table; and an assertion that Distance had no upper bound, which the human had already bounded at 100 km.

---

## 4. Specification state at end of task

Fourteen functional requirements (REQ-F-1…14), five non-functional (REQ-NF-1…5), and two development process constraints (REQ-DP-1…2). Sixteen acceptance criteria across nine requirements, indexed in Appendix B of the specification and traced in `docs/traceabilitymatrix.md`.

Criteria distribution: REQ-F-4 and REQ-F-5 and REQ-F-6 and REQ-F-8 and REQ-F-12 carry two each; REQ-F-9 and REQ-F-10 share a single block of two; REQ-F-11 carries one; REQ-F-13 carries three.

Open questions recorded in §5 of the specification: product framing if activity types are later introduced, test framework selection, and application title and domain.

Three guarantees are stated in requirement prose but verified by no acceptance criterion: cross-Month recomputation on Date change (REQ-F-5), validation parity between creation and update (REQ-F-10), and permanence of deletion (REQ-F-11). REQ-DP-1 mandates test-first coverage for the first two; the third is covered by no criterion and by no mandated test.