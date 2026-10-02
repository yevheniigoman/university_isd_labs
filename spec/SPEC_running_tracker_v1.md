# Software Requirements Specification: Running Tracker v1

**Project:** Running Tracker
**Version:** 1.0 (v1)
**Status:** Elicitation complete — draft
**Date:** 2026-10-02
**Source:** `spec/SPEC_running_tracker.md`
**Conventions:** IEEE 830-aligned

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Overall Description](#2-overall-description)
3. [Specific Requirements](#3-specific-requirements)
4. [Out of Scope](#4-out-of-scope)
5. [Open Questions](#5-open-questions)
6. [Appendix A: Ubiquitous Language](#appendix-a-ubiquitous-language)
7. [Appendix B: Acceptance Criteria Index](#appendix-b-acceptance-criteria-index)

---

## 1. Introduction

### 1.1 Purpose

This document specifies a minimalist running tracker: a responsive web application in which a single individual manually records their runs, reviews them grouped by month, and compares month-over-month distance through a bar chart. It is intended as a simplified alternative to adidas Running, differing chiefly in offering a more comfortable interface with visual progress reporting than a spreadsheet provides.

This document is technology-neutral. No framework, language, or library is prescribed. Sessions are described as server-maintained; persistence is described relationally; distance is described in units without binding the product to any particular system. One exception is deliberate: password storage names BCrypt (REQ-NF-1), because the choice of adaptive hashing is itself a security requirement rather than an implementation preference.

### 1.2 Scope

In scope: account registration and login, manual recording of runs, per-month history and totals, and a month-over-month comparison chart.

Out of scope for v1: GPS tracking, route visualization and maps, activity-type distinction, and the security and session-lifecycle features enumerated in [Section 4](#4-out-of-scope).

### 1.3 Definitions, Acronyms, and Abbreviations

| Term | Meaning |
|---|---|
| **Training** | A single manually recorded run. The canonical domain object. |
| **Month** | A calendar month. |
| **Month total distance** | The sum of the distances of all Trainings dated within a Month. |
| **Month Comparison Chart** | The bar-chart feature comparing one Month against another. |
| **Month Comparison Range** | The set of Months displayed by the Month Comparison Chart. |
| **Empty Month** | A Month within the Month Comparison Range containing no Trainings. Renders as `0`. |
| **Distance** | The length of a Training, expressed in kilometres, stored in whole metres. |
| **Duration** | The elapsed time of a Training. |
| **Date** | The calendar date of a Training. Carries no time and no timezone. Bounded above by the server's current date plus one day; no earliest date is imposed. |

### 1.4 References

- `spec/SPEC_running_tracker.md` — original product description and feature list
- IEEE Std 830-1998, *Recommended Practice for Software Requirements Specifications*

---

## 2. Overall Description

### 2.1 Product Perspective

A standalone web application with no third-party integrations. A registered user authenticates, records runs, and reviews them. The system maintains no public content and no interaction between accounts.

### 2.2 User Classes

There is exactly one class of user: **the account holder**, an individual who records and reviews their own runs. Each account is isolated; no user may view or modify another user's Trainings.

### 2.3 Operational Environment

A responsive web application accessed from smartphones (minimum viewport 360 px) through desktop browsers. The application is multi-device: a single account is expected to be used from more than one device concurrently.

### 2.4 Constraints

- Trainings are persisted in a relational database.
- Passwords are stored only as BCrypt hashes.
- Server-maintained sessions; session lifetime is not governed in v1.

### 2.5 Dependencies

None. The product depends on no external service, and the user on no external hardware beyond a web-capable device.

---

## 3. Specific Requirements

Requirement identifiers follow `REQ-F-<n>` for functional and `REQ-NF-<n>` for non-functional requirements. Section [3.3](#33-development-process-constraints) introduces `REQ-DP-<n>` for constraints on how the software is built rather than on what it does.

Acceptance criteria are given as `AC-<n>` within each requirement and are stated in Given/When/Then form, describing externally observable outcomes only. Criteria are provided for the core business requirements; requirements whose behaviour is conventional and evident from their statement carry none (see [Appendix B](#appendix-b-acceptance-criteria-index)).

### 3.1 External Interface Requirements

#### REQ-F-1 — Account registration

A visitor shall be able to create an account by supplying an email address and a password.

- Email addresses are compared case-insensitively. `Runner@Example.com` and `runner@example.com` denote the same account.
- An email address may be associated with at most one account.
- The system shall never disclose whether a given email address is already registered.

#### REQ-F-2 — Login

A registered user shall be able to authenticate with a valid email address and password and thereby gain access to their profile and Trainings.

- Authentication failure shall produce a single generic message — "Invalid email or password" — regardless of whether the email is unregistered or the password is incorrect.
- Passwords shall be verified against BCrypt hashes only. Plaintext passwords are never stored, logged, or compared.

#### REQ-F-3 — Session establishment

Upon successful authentication the system shall establish a server-maintained session associating the client with the account, and shall present the authenticated views to that client.

- Session state resides on the server, not in client-held storage that the client may alter.
- The session persists for the duration of the browser session.

#### REQ-F-4 — Record a Training

An authenticated user shall be able to create a Training by supplying a Date, a Distance, and a Duration.

- Each Training shall carry a distinct identity, permitting a specific Training to be subsequently retrieved, updated, or deleted.
- A Training shall be created in an initial state in which every field is empty or unset.

#### REQ-F-5 — Training Date

A Training's Date shall be a calendar date comprising no time and no timezone, and shall be stored and interpreted without timezone conversion.

- A Date later than the server's current date plus one day shall be rejected, irrespective of what the submitting client displayed as selectable.
- No earliest date shall be imposed. The Training form shall offer Dates from any past date through the client's current date; the server shall accept any Date up to the stated ceiling.
- The one-day tolerance accommodates clients whose local date is ahead of the server's, while bounding the furthest date any client may record.
- A Date may be freely changed by the user, including to a Date belonging to a different Month. All derived figures shall be recomputed accordingly.

#### REQ-F-6 — Training Distance

A Training's Distance shall be at least 0.01 km and at most 100 km.

- Distance shall be accepted with up to two decimal places and normalized on input; a submitted value of `5` is accepted and stored as `5000` m, and a value of `5.2` as `5200` m.
- Because two decimal places in kilometres corresponds to exact multiples of 10 m, storage in whole metres is lossless. No accepted input shall be silently rounded.

#### REQ-F-7 — Training Duration

A Training's Duration shall be supplied in hours, minutes, and seconds via user input, and shall be stored as a single elapsed value independent of that input format.

- Duration shall be required and shall be greater than zero.
- The hours/minutes/seconds split is a user-interface input convention only. Minutes and seconds presented for entry shall each fall within 0–59.
- The system shall accept and correctly interpret values outside 0–59 when received in stored form, since the stored representation carries no such split.

#### REQ-F-8 — View Training history

An authenticated user shall be able to view a list of all of their Trainings, newest first, grouped by Month.

- Each Month group shall display that Month total distance as a heading.
- The list shall display every Training the user has recorded. It shall not paginate, truncate, or limit its length.

#### REQ-F-9 — View Training details

An authenticated user shall be able to view the recorded Date, Distance, and Duration of any one of their Trainings, and shall be presented the means to edit or delete it.

#### REQ-F-10 — Edit a Training

An authenticated user shall be able to modify the Date, Distance, and Duration of an existing Training.

- Editing shall be performed by presenting the Training form pre-filled with that Training's current values.
- A user shall be able to cancel an edit, leaving the Training unchanged.
- All validation rules governing creation shall equally govern update.

#### REQ-F-11 — Delete a Training

An authenticated user shall be able to permanently delete a Training.

- Deletion shall require confirmation, presented as a modal prompt — "Do you want to delete this training?" — which the user must affirm.
- Deletion shall be permanent. No mechanism for restoring a deleted Training is provided in v1.
- All figures derived from the deleted Training shall be recomputed to exclude it.

#### REQ-F-12 — Month total distance

The system shall determine the Month total distance of a Month as the sum of the Distances of all Trainings dated within that Month.

- The total shall be computed from the stored Trainings at the time it is requested. It shall not be persisted, cached, or stored independently of its constituent Trainings.
- Computation shall be performed over a single consistent unit so that no unit conversion occurs within the aggregation.
- A Month containing no Trainings shall have a Month total distance of zero.
- Where a Training's Date is changed or a Training is deleted, the affected Month total distance shall reflect the change without any separate correction step.

#### REQ-F-13 — Month Comparison Chart

The system shall present a bar chart comparing the Month total distance of successive Months.

- Each bar shall represent one Month, and its value shall be that Month's Month total distance.
- Bars shall be ordered oldest Month on the left, current Month on the right.
- A bar's value shall be that Month's total alone. The chart shall not present a cumulative running total, and shall not present a target or goal line.
- The Month Comparison Range shall extend from the earlier of the user's earliest Training Month or the Month eleven months preceding the current Month, through the current Month inclusive, and shall contain at most twelve Months.
- Months preceding the user's earliest Training shall not be displayed.
- An Empty Month shall appear within the Month Comparison Range at a value of zero rather than being omitted.
- A user with no Trainings shall be presented a chart consisting of the current Month alone at a value of zero.

#### REQ-F-14 — Account data isolation

A user shall be able to view, modify, and delete only their own Trainings. No user shall be able to view, modify, or delete another user's Training, including by supplying, guessing, or constructing that Training's identity directly.

### 3.2 Non-Functional Requirements

#### REQ-NF-1 — Password storage

Passwords shall be stored exclusively as BCrypt hashes. Plaintext passwords shall not be stored, logged, or transmitted. Fast, non-adaptive digests (e.g. MD5, SHA-1) shall not be used for password storage.

#### REQ-NF-2 — Responsive presentation

The application shall be usable from a viewport of 360 px width through desktop. All user-facing views — Training history, Training details, the Training form, and the Month Comparison Chart — shall be usable at every width in this range. With twelve bars at maximum, the Month Comparison Chart shall remain legible on a smartphone, by means of horizontal scrolling or abbreviated labels.

#### REQ-NF-3 — Persistence

Trainings and accounts shall be persisted in a relational database comprising two tables: one for users and one for Trainings. Distances shall be stored as integer metres.

#### REQ-NF-4 — Scale

The system shall be designed to support approximately 5,000 Trainings per account. The Training history shall display all of a user's Trainings without pagination at that scale.

#### REQ-NF-5 — Multi-device consistency

Because a Date carries no timezone, a Training's Month assignment shall be identical on every device. A user shall observe the same Month grouping, Month total distance, and Month Comparison Chart regardless of which device is used.

### 3.3 Development Process Constraints

The following constraints govern how the software is built. They are not observable by a user of the running tracker and are therefore not classified as functional or non-functional requirements of the product itself.

#### REQ-DP-1 — Test-driven development for core business logic

Core business logic shall be developed test-first: a failing test shall be written before the implementation, and the implementation shall proceed only to the point at which that test passes.

This obligation applies to the logic whose defects would be silent or expensive to diagnose in use, namely:

- the Month Comparison Range derivation and its twelve-Month cap, Empty Month zero-filling, and bar ordering (REQ-F-13)
- the Month total distance aggregation and its treatment of Months containing no Trainings (REQ-F-12)
- the validation rules governing Date, Distance, and Duration, including the `serverToday + 1` boundary and the inclusive distance bounds (REQ-F-5, REQ-F-6, REQ-F-7)
- the normalisation of Distance input to whole metres (REQ-F-6)

This obligation does not extend to persistence wiring, configuration, or the rendering of user-facing views.

- The test framework is not prescribed by this document and remains to be selected.
- This constraint concerns the order in which code is written. It does not alter the externally observable behaviour specified in [Section 3.1](#31-external-interface-requirements).

#### REQ-DP-2 — Agent decision log

Decisions made by a coding agent while producing this software shall be recorded in `logs/`, one consolidated entry per task, following the convention established in `logs/01_agent.md`.

Each entry shall record:

- the decisions taken during the task, each with the reasoning behind it;
- the rationale for resolving any version-control conflict, identifying the branch or version chosen and why it was preferred;
- changes made to the specification itself, with the reasoning behind each change;
- references to the requirement identifiers those decisions shape, so that an entry can be traced to the requirement it justifies.

The log records **decisions, not activity**. The following shall be excluded:

- files read or written;
- terminal commands executed.

Entries accumulate as a numbered sequence, one file per task.

---

## 4. Out of Scope

The following are deliberately excluded from v1 and are recorded here as accepted limitations rather than deferred requirements.

| Exclusion | Consequence |
|---|---|
| **No logout** | A session cannot be terminated by the user. On a shared device the session remains open. |
| **No password change** | A user cannot change a forgotten or compromised password through the application. |
| **No password reset** | There is no recovery path for a lost password. Combined with the above, a lost password is unrecoverable. |
| **No HTTPS** | Credentials and session data cross the network unencrypted. Note that BCrypt protects passwords at rest only; it offers no protection for credentials in transit. |
| **No login rate limiting** | Login attempts are unthrottled and vulnerable to brute-force enumeration. |
| **No session lifetime** | Sessions do not expire; an abandoned session remains valid until the browser closes. |
| **No database backups** | Recovery from data loss is not addressed by the product. |
| **No GPS, maps, or routes** | As directed by the original specification. |
| **No activity-type distinction** | Every Training is a run. Bicycle rides are not represented in v1. |
| **No accessibility baseline** | No keyboard-navigation or contrast requirements are specified for v1. |

---

## 5. Open Questions

1. **Product framing.** The product is titled and framed as a "running tracker." If activity types are introduced in a later version, the Month total distance and the Month Comparison Chart would each need a decision on whether to combine activity types or report them separately. Terminology should be revisited at that time.
2. **Test framework.** REQ-DP-1 requires test-first development without prescribing a framework. The choice remains open.
3. **Application title and domain** remain unspecified and follow the account holder's existing deployment.

---

## Appendix A: Ubiquitous Language

The following terms carry the agreed meanings throughout this document. The synonym "run" was retired in favour of **Training**; the UI should not reintroduce it.

| Term | Definition |
|---|---|
| **Training** | A single manually recorded run, carrying an identity, a Date, a Distance, and a Duration. Distance ≥ 0.01 km and Duration > 0 are both required. |
| **Date** | A calendar date with no time and no timezone. Never converted. Bounded above by the server's current date plus one day; no earliest date is imposed. |
| **Distance** | Length in kilometres, accepted to at most two decimal places, normalized on input, stored in whole metres, bounded to [0.01 km, 100 km]. |
| **Duration** | Elapsed time, required and greater than zero. Hours/minutes/seconds is a user-interface input format only. |
| **Month** | A calendar month. |
| **Month total distance** | The sum of the Distances of the Trainings dated within a Month. Computed, never stored. |
| **Month Comparison Chart** | The month-over-month bar-chart feature. |
| **Month Comparison Range** | The Months displayed by the chart: from the earlier of the earliest Training Month or eleven months preceding the current Month, through the current Month, twelve Months maximum. |
| **Empty Month** | A Month within the Month Comparison Range containing no Trainings. Renders as `0`. |

---

## Appendix B: Acceptance Criteria Index

Criteria are provided for the core business requirements. Account registration (REQ-F-1), login (REQ-F-2), session establishment (REQ-F-3), Training Duration (REQ-F-7), and account data isolation (REQ-F-14) are conventional in behaviour and evident from their requirement statements; they carry no criteria. Where a requirement's behaviour follows necessarily from another, it likewise carries none.

### REQ-F-4 — Record a Training

| ID | Criterion |
|---|---|
| AC-1 | Given an authenticated user, when they supply a valid Date, Distance, and Duration and submit the form, then the Training is created and appears in their Training history. |
| AC-2 | Given an unauthenticated visitor, when they attempt to create a Training, then the Training is not created. |

### REQ-F-5 — Training Date

| ID | Criterion |
|---|---|
| AC-1 | Given an authenticated user, when they record a Training dated later than the server's current date plus one day, then the Training is rejected, including when the request originates without passing through the Training form. |
| AC-2 | Given an authenticated user, when they record a Training dated any earlier date, or the current date, or up to the server's current date plus one day, then the Training is accepted. |

### REQ-F-6 — Training Distance

| ID | Criterion |
|---|---|
| AC-1 | Given an authenticated user, when they submit a Distance outside the permitted range — less than 0.01 km, including zero and negative values, or greater than 100 km — then the Training is rejected and not created. |
| AC-2 | Given an authenticated user, when they submit a Distance of 0.01 km or greater, up to and including 100 km, then the Training is accepted and the Distance is stored in whole metres. |

### REQ-F-8 — View Training history

| ID | Criterion |
|---|---|
| AC-1 | Given a user with Trainings in several Months, when they open their Training history, then Trainings are ordered newest first, and each Month group is headed by that Month total distance. |
| AC-2 | Given a user with no Trainings, when they open their Training history, then they are informed that no Trainings have been recorded. |

### REQ-F-9 / REQ-F-10 — View details and edit

| ID | Criterion |
|---|---|
| AC-1 | Given a user with at least one Training, when they open a specific Training's details, then its Date, Distance, and Duration are shown. |
| AC-2 | Given a Training's details, when the user chooses to edit, then the Training form is presented pre-filled with that Training's current values. |

### REQ-F-11 — Delete a Training

| ID | Criterion |
|---|---|
| AC-1 | Given a user selects delete on a Training, when the confirmation is presented, then no deletion occurs until the user affirms it. |

### REQ-F-12 — Month total distance

| ID | Criterion |
|---|---|
| AC-1 | Given Trainings in a Month with Distances of 5.20 km and 4.80 km, when the Month total distance is requested, then it is 10.00 km. |
| AC-2 | Given a Month containing no Trainings, when its Month total distance is requested, then it is zero. |

### REQ-F-13 — Month Comparison Chart

| ID | Criterion |
|---|---|
| AC-1 | Given a user with no Trainings, when they open the Month Comparison Chart, then exactly one bar is shown, labelled with the current Month, at a value of zero. |
| AC-2 | Given a user whose earliest Training is 3 Months before the current Month, when they open the chart, then exactly 3 bars are shown: the earliest Training's Month through the current Month. |
| AC-3 | Given a user whose Trainings span more than 12 Months, when they open the chart, then exactly 12 bars are shown, displaying the most recent 12 Months. |
| AC-4 | Given the chart is displayed, when it is examined, then the oldest Month is the leftmost bar and the current Month the rightmost. |
| AC-5 | Given a user with a Training in January and a Training in March but none in February, when the chart is displayed, then February appears between them at a value of zero. |