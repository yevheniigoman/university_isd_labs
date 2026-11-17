# Implementation Plan — Running Tracker v1

**Source:** [`spec/SPEC_running_tracker_v1.md`](../spec/SPEC_running_tracker_v1.md), [`spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md`](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md), [`docs/tickets.md`](tickets.md)
**Date:** 2026-10-02, amended 2026-11-15

A staged sequence for implementing the fourteen tickets. Stages are ordered so that every ticket's dependencies — as recorded in [`docs/tickets.md`](tickets.md) — are satisfied before it begins. Tickets 1–13 come from the parent specification; ticket 14 comes from [addendum 1](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md) and is scheduled as Stage 8.

Estimates assume a single coding agent working autonomously, test-first per REQ-DP-1, including writing code and tests, running them, and iterating on failures. Human review and merge are excluded. Durations are wall-clock ranges.

---

## Stage 1 — Schema foundation

| Ticket | Depends on | Est. |
|---|---|---|
| 1 Users table | — | 15–20 min |
| 4 Trainings table | 1 | 15–25 min |

Establishes all persistence (units, foreign key, case-insensitive email) before any logic.

## Stage 2 — Authentication

| Ticket | Depends on | Est. |
|---|---|---|
| 2 Registration | 1 | 45 min–1 h |
| 3 Login | 1, 2 | 45 min–1 h |

Ticket 3 is sequential after 2 (shared BCrypt and the anti-enumeration concern).

## Stage 3 — Training domain logic

| Ticket | Depends on | Est. |
|---|---|---|
| 5 Validation | 4 | 45 min–1 h |
| 7 Month aggregation | 4 | 15–30 min |

Pure, test-first logic. Tickets 5 and 7 are independent and may be taken in either order.

## Stage 4 — Training creation

| Ticket | Depends on | Est. |
|---|---|---|
| 6 Form and create handler | 3, 4, 5 | 1–1.5 h |

First end-to-end vertical slice (authenticate → validate → persist).

## Stage 5 — Read views

| Ticket | Depends on | Est. |
|---|---|---|
| 9 Training details | 3, 4 | 30–45 min |
| 8 Training history | 3, 7, 9 | 1–1.25 h |

Ticket 9 precedes 8 (history rows link to details). This stage closes ticket 6's forward dependency on ticket 8.

## Stage 6 — Mutations

| Ticket | Depends on | Est. |
|---|---|---|
| 10 Training edit | 5, 6, 9 | 40 min–1 h |
| 11 Training delete | 4, 9 | 20–30 min |

## Stage 7 — Month Comparison Chart

| Ticket | Depends on | Est. |
|---|---|---|
| 12 Chart data | 7 | 45 min–1 h |
| 13 Chart rendering | 12 | 45 min–1 h |

Ticket 13 is frontend work and is out of scope for this backend-only deliverable; Stage 7 delivers ticket 12 alone.

## Stage 8 — Monthly distance goals

Ticket 14, defined by [addendum 1](../spec/SPEC_running_tracker_v1_addendum_1_monthly_goals.md). The feature is independent of the chart, so it can begin as soon as Stage 3's aggregation exists; it is scheduled last to keep the parent specification's stages intact.

| Step | Work | Depends on | Est. |
|---|---|---|---|
| 8.1 | `monthly_goals` table, entity, repository | 1 | 20–30 min |
| 8.2 | Target distance and Goal Month validation | 8.1 | 20–30 min |
| 8.3 | Progress derivation from Month total distance | 7, 8.1 | 20–30 min |
| 8.4 | `PUT`, `GET`, `DELETE /api/goals/{month}` | 3, 8.2, 8.3 | 45 min–1 h |
| 8.5 | `GET /api/goals` twelve-Month listing | 8.4 | 20–30 min |

Step 8.1 is schema-only and carries no dedicated tests, per the policy recorded in [`logs/05_schema_test_policy.md`](../logs/05_schema_test_policy.md). The unique `(user_id, goal_month)` constraint is exercised by the replacement test in 8.4 rather than asserted directly.

**Test-first order.**

- **8.2** — a valid target is stored in whole metres; an out-of-range target is rejected; a malformed Goal Month is rejected; a component beyond two decimal places is truncated toward zero.
- **8.3** — progress arithmetic (32.50 of 50.00 → 17.50, 65.0); Remaining distance floored at zero; Percent complete uncapped above 100.0; progress recomputed after a Training is created, edited (including re-dated across Months), or deleted.
- **8.4** — set, replace, read, and delete; a second target replaces the first rather than adding a row; `404` for a goal that does not exist and for another account's goal; a rejected request leaves an existing goal unchanged; `401` when unauthenticated.
- **8.5** — twelve Months in oldest-first order, matching the Month Comparison Range; Months without a goal omitted; only the session account's goals returned.

**Close-out.** Extend `docs/openapi.yml` with the four paths and their schemas, add `REQ-F-15` to `docs/traceabilitymatrix.md`, and record the stage in `logs/14_stage8_monthly_goals.md` under REQ-DP-2. No chart endpoint changes: `REQ-F-13` forbids a goal line, so progress stays on the goal endpoints.

## Estimate summary

| Stage | Est. (agent) |
|---|---|
| 1 Schema foundation | 30–45 min |
| 2 Authentication | 1.5–2 h |
| 3 Training domain logic | 1–1.5 h |
| 4 Training creation | 1–1.5 h |
| 5 Read views | 1.5–2 h |
| 6 Mutations | 1–1.5 h |
| 7 Chart | 1.5–2 h |
| 8 Monthly goals | 2–3 h |
| **Total** | **~10–14.5 h** |

Variability is driven mostly by test-iteration cycles rather than code volume. Stages 2 and 5 skew high (shared anti-enumeration concern; history edge cases and the 5,000-Training target). Stage 8 skews high for its size because progress derivation and account isolation are exercised through several Training-mutation scenarios.

## Milestones

| Milestone | Reached after | Meaning |
|---|---|---|
| M1 | Stage 2 | Authentication usable |
| M2 | Stage 6 | Full CRUD plus history |
| M3 | Stage 7 | Parent specification feature-complete |
| M4 | Stage 8 | Monthly goals complete |

---

## Calendar view (Gantt)

Bars use midpoint estimates (total ≈ 12 h), plotted on an hour axis. The calendar mapping assumes a 4-hour productive agent block per day; at 8 h/day the plan compresses to ~1.5 days.

```
Stage                       0   2   4   6   8   10  12  (hours, 1 char = 0.5 h)
                            |   |   |   |   |   |   |
1  Schema foundation        █
2  Authentication            ███
3  Domain logic                 ███
4  Training creation               ███
5  Read views                         ███
6  Mutations                             ███
7  Chart                                    ███
8  Monthly goals                               █████
```

Per-stage spans:

| Stage | Start | End |
|---|---|---|
| 1 Schema | 0.0 h | 0.5 h |
| 2 Auth | 0.5 h | 2.0 h |
| 3 Domain logic | 2.0 h | 3.5 h |
| 4 Creation | 3.5 h | 5.0 h |
| 5 Read views | 5.0 h | 6.5 h |
| 6 Mutations | 6.5 h | 8.0 h |
| 7 Chart | 8.0 h | 9.5 h |
| 8 Monthly goals | 9.5 h | 12.0 h |

Calendar mapping (4 h/day → 8 half-hour units per day):

| Day | Agent-hours | Stages covered | Milestone |
|---|---|---|---|
| Day 1 | 0–4 h | 1, 2, 3, and start of 4 | **M1** — auth usable (after Stage 2) |
| Day 2 | 4–8 h | rest of 4, then 5, 6 | **M2** — full CRUD plus history (after Stage 6) |
| Day 3 | 8–12 h | 7, then 8 | **M3** — parent specification feature-complete (after Stage 7); **M4** — monthly goals complete (after Stage 8) |
