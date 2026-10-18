# Implementation Plan — Running Tracker v1

**Source:** [`spec/SPEC_running_tracker_v1.md`](../spec/SPEC_running_tracker_v1.md), [`docs/tickets.md`](tickets.md)
**Date:** 2026-10-02

A staged sequence for implementing the thirteen tickets. Stages are ordered so that every ticket's dependencies — as recorded in [`docs/tickets.md`](tickets.md) — are satisfied before it begins.

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
| **Total** | **~8–11.5 h** |

Variability is driven mostly by test-iteration cycles rather than code volume. Stages 2 and 5 skew high (shared anti-enumeration concern; history edge cases and the 5,000-Training target).

## Milestones

| Milestone | Reached after | Meaning |
|---|---|---|
| M1 | Stage 2 | Authentication usable |
| M2 | Stage 6 | Full CRUD plus history |
| M3 | Stage 7 | Feature-complete |

---

## Calendar view (Gantt)

Bars use midpoint estimates (total ≈ 9.5 h), plotted on an hour axis. The calendar mapping assumes a 4-hour productive agent block per day; at 8 h/day the plan compresses to ~1.5 days.

```
Stage                         0 1 2 3 4 5 6 7 8 9  (hours, 1 char = 0.5 h)
                              | | | | | | | | | |
1  Schema foundation        █
2  Authentication            ███
3  Domain logic                  ███
4  Training creation                 ███
5  Read views                           ███
6  Mutations                                 ███
7  Chart                                         ███
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

Calendar mapping (4 h/day → 8 half-hour units per day):

| Day | Agent-hours | Stages covered | Milestone |
|---|---|---|---|
| Day 1 | 0–4 h | 1, 2, 3, and start of 4 | **M1** — auth usable (after Stage 2) |
| Day 2 | 4–8 h | rest of 4, then 5, 6 | **M2** — full CRUD plus history (after Stage 6) |
| Day 3 | 8–9.5 h | 7 | **M3** — feature-complete (after Stage 7) |
