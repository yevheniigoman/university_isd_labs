# Risks — Running Tracker v1

**Source:** [`spec/SPEC_running_tracker_v1.md`](../spec/SPEC_running_tracker_v1.md), [`docs/tickets.md`](tickets.md), [`docs/plan.md`](plan.md)
**Date:** 2026-10-02

Risks specific to implementation by a coding agent. Each risk is estimated as a likelihood/impact pair, then described in terms of how it manifests, its possible outcomes, and how to prevent or mitigate it.

---

## Risk 1 — Requirements misinterpretation by the coding agent

**Estimate:** Likelihood **High** · Impact **High**.

### How it manifests

The agent reads a requirement or ticket and implements a plausible but wrong reading. This project is unusually exposed because several guarantees exist only in prose and the stack is deliberately absent from the specification:

- `truncate, never round` on Duration — easy to invert.
- Three different date ceilings (`serverToday + 1`, the form's `clientToday`, no earlier floor) collapse into one.
- "Month total distance … never persisted or cached" — the agent caches for the 5,000-Training target.
- Month Comparison Range "independent of the user's Trainings"; Empty Month = 0; oldest left.
- Distance normalisation (`5` → `5000` m, `5.2` → `5200` m, lossless) with 2-dp input.
- Anti-enumeration (REQ-F-1/2) and the ownership check (REQ-F-14) — no ticket, no acceptance criterion.
- Ubiquitous language drift (`Training` versus the retired `Run`).
- Stack inference (Spring Boot + htmx + server sessions) — the agent guesses architecture with no specification guidance.

It surfaces as *silent* deviation rather than errors: the agent writes its own tests, so they pass while encoding the wrong behaviour. The worst case is a cross-ticket inconsistency (ticket 4 stores metres, ticket 5 validates kilometres and double-converts).

### Possible outcomes

- Specification non-compliance and functional bugs (wrong totals, off-by-one date rejection, wrong chart range).
- Security holes (IDOR, account enumeration) shipped as "done".
- Tests that lock in incorrect behaviour, so the defect becomes the baseline.
- Rework and re-negotiation of "done" at review.

### Prevention and mitigation

- Turn each acceptance criterion into an executable test verbatim; treat the criteria as the contract, not the agent's own invented tests.
- Require the agent to restate each ticket in its own words and cite the requirement IDs before coding (fits REQ-DP-2), forcing the interpretation into the open.
- Supply golden examples from the specification (`5` → `5000` m, `5.2` → `5200` m, `5.20 + 4.80 = 10.00`, exactly 12 months).
- Mandate stop-and-ask on ambiguity rather than guessing.
- Keep tickets small; verify the unit contract at each 4 → 5 → 6 boundary.
- Review against the specification, not against the agent's tests.

---

## Risk 2 — Context loss or obsolescence

**Estimate:** Likelihood **High** · Impact **Medium–High**.

### How it manifests

A ~9.5 h, 13-ticket run exceeds any single reliable context window. Symptoms: forgetting earlier decisions (metres versus kilometres, `Training` naming, the persistent-cookie session model), re-deriving conventions differently in later tickets, duplicate implementations (re-extracting `YearMonth` in ticket 12 instead of reusing ticket 7), and asserting things about code no longer in view. **Obsolescence** is the sibling failure: the specification or tickets change after the agent read them, and it implements against a stale version — this repository has already produced one merge conflict from divergent state.

### Possible outcomes

- Cross-module inconsistency (unit mismatch, two chart implementations, conflicting session handling).
- Regressions in earlier, already-working tickets.
- Redundant work, merge conflicts, and phantom references to non-existent code.
- Loss of the rationale trail, making later review unreliable.

### Prevention and mitigation

- Use small, scoped tickets with a fresh context per ticket; do not carry one long session across all thirteen.
- Treat the specification, tickets, and decision log as durable external memory, re-read at the start of every ticket.
- Keep a consolidated running summary (decisions, units, naming, open gaps) that the agent reloads.
- Treat tests as durable memory: each ticket's regression tests guard earlier behaviour even if the agent forgets it.
- Version-pin the specification and note it when tickets change, so obsolescence is detectable.
- Commit per ticket and review at stage boundaries; small diffs make drift visible.

---

## Risk 3 — Insufficient human control over critical decisions

**Estimate:** Likelihood **Medium–High** · Impact **High**.

### How it manifests

The agent makes irreversible or high-consequence choices autonomously: data-model and schema shape, session strategy, security enforcement (ownership checks, anti-enumeration), whether deletion is permanent, and whether to cache derived data. Because the specification explicitly omits the stack, most architectural decisions fall outside any requirement — so nothing in the tickets forces a human to see them. A related failure is the agent acting as its own judge, accepting its own tests as proof.

### Possible outcomes

- A wrong architecture discovered late, when change is expensive.
- Security flaws accepted into the "done" state.
- Specification drift — decisions made that no requirement authorised (scope creep beyond §4).
- Unverifiable completion claims and eroded accountability.

### Prevention and mitigation

- Define explicit approval gates: architecture, data model, session and security model, deletion semantics, and anything not traceable to a requirement require human sign-off.
- Place human checkpoints at stage boundaries (schema, auth, before commits) rather than only at the end.
- Enforce the REQ-DP-2 decision log (decision plus rationale plus requirement IDs) and require a human to review it, not the agent alone.
- Keep the specification authoritative: no feature, cache, or dependency beyond §3; anything else is escalated.
- Require explicit escalation on ambiguity or security-sensitive paths.
- Independently verify the uncovered guarantees (REQ-F-11 permanence, REQ-F-14 ownership, REQ-F-5/10 recomputation) — the agent must not be the sole author and judge of those tests.
