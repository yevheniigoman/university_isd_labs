# Software Requirements Specification Addendum 1: Monthly Distance Goals

**Project:** Running Tracker
**Addendum:** 1
**Version:** 1.1 (v1, addendum 1)
**Status:** Elicitation complete — draft
**Date:** 2026-11-15
**Parent specification:** [`SPEC_running_tracker_v1.md`](SPEC_running_tracker_v1.md)
**Conventions:** IEEE 830-aligned, inherited from the parent specification

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Definitions](#2-definitions)
3. [Requirement](#3-requirement)
4. [Acceptance Criteria](#4-acceptance-criteria)
5. [Compatibility with the Parent Specification](#5-compatibility-with-the-parent-specification)
6. [Out of Scope](#6-out-of-scope)

---

## 1. Introduction

### 1.1 Purpose

This addendum extends Running Tracker v1 with **monthly distance goals**: an account holder may set a target distance for a Month and see how far they have progressed toward it.

It is a separate document. It does not modify, replace, or restate the parent specification, and no requirement identifier defined there is redefined here. Where this addendum and the parent specification touch the same behaviour, [section 5](#5-compatibility-with-the-parent-specification) states the relationship explicitly.

### 1.2 Scope

The addendum introduces:

- one new persisted concept, the **Monthly Goal**, with its own table;
- one new functional requirement, `REQ-F-15`;
- the acceptance criteria for that requirement.

It introduces no change to account registration, login, sessions, Training recording, Training history, Training details, Training edit, Training deletion, Month total distance, or the Month Comparison Chart.

### 1.3 Relationship to the Parent Specification

All definitions, conventions, development-process constraints, and out-of-scope exclusions of the parent specification continue to apply. In particular:

- `REQ-NF-1` (BCrypt password storage), `REQ-NF-2` (responsive presentation), and `REQ-NF-4` (scale) are unchanged.
- `REQ-DP-1` (test-first development) and `REQ-DP-2` (decision logging) govern this feature as they govern every other.
- `REQ-F-14` (account data isolation) extends to Monthly Goals: a user may view, modify, and delete only their own goals.

### 1.4 References

- [`SPEC_running_tracker_v1.md`](SPEC_running_tracker_v1.md) — parent specification
- IEEE Std 830-1998, *Recommended Practice for Software Requirements Specifications*

---

## 2. Definitions

The following terms are added to Appendix A of the parent specification. Terms already defined there — **Month**, **Distance**, **Month total distance**, **Month Comparison Range**, **Empty Month** — retain their existing meanings.

| Term | Definition |
|---|---|
| **Monthly Goal** | A target distance an account holder sets for one Month. At most one Monthly Goal exists per account per Month. |
| **Goal Month** | The Month a Monthly Goal applies to. A calendar month with no day and no time; never converted. |
| **Target distance** | The distance a Monthly Goal asks the account holder to cover in its Goal Month, expressed in kilometres, accepted to at most two decimal places, normalised on input, stored in whole metres, bounded to [0.01 km, 1000 km]. |
| **Completed distance** | The Month total distance of the Goal Month, derived from the account's current Trainings. Computed, never stored. |
| **Remaining distance** | The Target distance minus the Completed distance, floored at zero. Computed, never stored. |
| **Percent complete** | Completed distance as a percentage of Target distance, to one decimal place, uncapped. Computed, never stored. |

---

## 3. Requirement

### 3.1 Functional Requirements

#### REQ-F-15 — Monthly distance goals

An authenticated user shall be able to set, view, replace, and remove a Monthly Goal for a Month, and shall be shown their progress toward it.

**Setting and replacing.**

- A Monthly Goal shall be set by supplying a Goal Month and a Target distance.
- A Target distance shall be accepted only within [0.01 km, 1000 km]. Values below 0.01 km, including zero and negative values, and values above 1000 km, shall be rejected.
- A Target distance shall be accepted with up to two decimal places and normalised on input. A component beyond the second decimal place shall be truncated toward zero, never rounded, so that 50.239 km is stored as 50.23 km.
- A Target distance shall be stored in whole metres.
- A Goal Month shall be a valid calendar month. A malformed or non-existent Goal Month shall be rejected.
- At most one Monthly Goal shall exist per account per Goal Month. Setting a goal for a Month that already has one shall replace that goal rather than create a second.
- A Monthly Goal may be set for any Month, including a Month outside the Month Comparison Range.

**Progress.**

- Progress for a Monthly Goal shall consist of its Goal Month, Target distance, Completed distance, Remaining distance, and Percent complete.
- Completed distance shall be the Month total distance of the Goal Month, as defined by `REQ-F-12`, derived from the account's current Trainings.
- Remaining distance shall be Target distance minus Completed distance, and shall never be negative. Where Completed distance equals or exceeds Target distance, Remaining distance shall be zero.
- Percent complete shall be Completed distance divided by Target distance, multiplied by 100, rounded half-up to one decimal place. It shall be uncapped: a Completed distance above the Target distance shall yield a Percent complete above 100.0.
- Progress shall never be stored. It shall be recomputed from the current Trainings on every request, so that creating, editing, or deleting a Training is reflected in progress without any separate correction step.
- A Training whose Date is changed into or out of a Goal Month shall move its Distance between the affected goals' Completed distances, in the same way it moves between Month totals under `REQ-F-5`.

**Listing.**

- A user shall be able to list their Monthly Goals for the twelve Months of the Month Comparison Range, in the same oldest-first order as that range. Each entry shall carry its goal and its progress.
- A Month in that range with no Monthly Goal shall be omitted from the list rather than reported with a zero Target distance.

**Removal.**

- A user shall be able to remove a Monthly Goal for a Month. Removal shall be permanent; the parent specification's exclusion of any restore mechanism applies equally here.

**Isolation and errors.**

- A user shall be able to view, modify, and delete only their own Monthly Goals. No user shall be able to view, modify, or delete another user's Monthly Goal, including by supplying, guessing, or constructing its Goal Month directly.
- A request naming a Goal Month with no Monthly Goal for the account, and a request naming a Month belonging to another account, shall both be refused as not found, and shall not disclose whether a goal exists for that Month under another account.
- A request that fails Target distance or Goal Month validation shall be rejected and shall leave any existing Monthly Goal unchanged.

---

## 4. Acceptance Criteria

Criteria are stated in Given/When/Then form and describe externally observable outcomes only, matching the parent specification's Appendix B.

### REQ-F-15 — Monthly distance goals

| ID | Criterion |
|---|---|
| AC-1 | Given an authenticated user, when they set a Target distance of 0.01 km or greater, up to and including 1000 km, for a valid Goal Month, then the Monthly Goal is recorded for that Month and the Target distance is stored in whole metres. |
| AC-2 | Given an authenticated user, when they submit a Target distance outside the permitted range — less than 0.01 km, including zero and negative values, or greater than 1000 km — or a malformed Goal Month, then the request is rejected and no Monthly Goal is created or changed. |
| AC-3 | Given an authenticated user who already has a Monthly Goal for a Month, when they set another Target distance for the same Month, then the existing goal is replaced and exactly one Monthly Goal exists for that account and Month. |
| AC-4 | Given a Monthly Goal of 50.00 km for a Month and Trainings in that Month totalling 32.50 km, when progress is requested, then Completed distance is 32.50 km, Remaining distance is 17.50 km, and Percent complete is 65.0. |
| AC-5 | Given a Monthly Goal of 20.00 km for a Month and Trainings in that Month totalling 25.00 km, when progress is requested, then Remaining distance is zero and Percent complete is 125.0. |
| AC-6 | Given a user with Monthly Goals in some Months of the Month Comparison Range and none in others, when they list their goals, then the goals of the twelve-Month range are returned oldest Month first and the Months without a goal are omitted. |

---

## 5. Compatibility with the Parent Specification

### 5.1 Amendment to REQ-NF-3 — Persistence

`REQ-NF-3` states that Trainings and accounts are persisted "in a relational database comprising two tables: one for users and one for Trainings." This addendum adds a third table for Monthly Goals and therefore amends the table count of `REQ-NF-3` to three. The original two-table statement remains correct for the Trainings and accounts it describes; only the count is superseded.

### 5.2 Compatibility with REQ-F-12 — Month total distance

`REQ-F-12` defines the Month total distance as the sum of the Distances of the Trainings dated within a Month, computed and never stored. Monthly Goal progress reuses that definition unchanged. A Monthly Goal never contributes to, subtracts from, or otherwise alters a Month total distance.

### 5.3 Compatibility with REQ-F-13 — Month Comparison Chart

`REQ-F-13` states that the chart "shall not present a target or goal line." A Monthly Goal shall therefore not appear on the Month Comparison Chart, and the chart's data shall not be extended with goal or target values. Goals are exposed only through their own views.

### 5.4 Compatibility with REQ-F-14 — Account data isolation

`REQ-F-14` confines users to their own Trainings. This addendum extends the same isolation rule to Monthly Goals without relaxing or reinterpreting the Training rule.

### 5.5 Compatibility with REQ-NF-4 — Scale

`REQ-NF-4` sets a design target of approximately 5,000 Trainings per account. Monthly Goals are bounded more tightly: at most one per account per Month, and at most twelve appear in a listing. No change to the scale requirement is needed.

### 5.6 Compatibility with REQ-DP-1 — Test-first development

Monthly Goal behaviour is not conventional and is not fully determined by another requirement, so it falls within `REQ-DP-1`'s test-first obligations. The following shall be written as tests before their implementation: Target distance validation, Goal Month validation, goal replacement, progress arithmetic including the zero-floor and uncapped percentage, progress recomputation after Training mutation, the twelve-Month listing, and account isolation.

---

## 6. Out of Scope

The following are deliberately excluded from this addendum and remain absent from v1:

| Exclusion | Consequence |
|---|---|
| **No goal notifications or reminders** | The system never prompts a user about an approaching, met, or missed goal. |
| **No goal streaks or multi-Month targets** | Goals are per Month and independent; no weekly, yearly, or consecutive-Month goal exists. |
| **No target lines on the Month Comparison Chart** | Forbidden by `REQ-F-13`; see [5.3](#53-compatibility-with-req-f-13--month-comparison-chart). |
| **No goal history or snapshots** | Only the current Target distance is retained. A replaced Target distance is not preserved, so past progress cannot be reconstructed. |
| **No goal sharing or social features** | Goals are private to the account holder, consistent with the parent specification's exclusion of interaction between accounts. |
| **No automatic goal suggestion** | The system never proposes a Target distance. |
