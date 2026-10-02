# Traceability Matrix

**Source specification:** [`spec/SPEC_running_tracker_v1.md`](../spec/SPEC_running_tracker_v1.md)
**Version:** 1.0 (v1)
**Date:** 2026-10-02

This matrix traces each requirement to the acceptance criteria that verify it. Requirement identifiers are defined in the specification's [Section 3](../spec/SPEC_running_tracker_v1.md#3-specific-requirements); acceptance criteria are defined in its [Appendix B](../spec/SPEC_running_tracker_v1.md#appendix-b-acceptance-criteria-index).

Acceptance criteria are numbered locally within each requirement. Where one block of criteria is shared between two requirements — `REQ-F-9` and `REQ-F-10` are specified together in the appendix — the criteria are listed against both.

## Requirements with acceptance criteria

| Requirement | Requirement name | Acceptance criteria |
|---|---|---|
| REQ-F-4 | Record a Training | AC-1, AC-2 |
| REQ-F-5 | Training Date | AC-1, AC-2 |
| REQ-F-6 | Training Distance | AC-1, AC-2 |
| REQ-F-8 | View Training history | AC-1, AC-2 |
| REQ-F-9 | View Training details | AC-1, AC-2 |
| REQ-F-10 | Edit a Training | AC-1, AC-2 |
| REQ-F-11 | Delete a Training | AC-1 |
| REQ-F-12 | Month total distance | AC-1, AC-2 |
| REQ-F-13 | Month Comparison Chart | AC-1, AC-2, AC-3, AC-4, AC-5 |

## Notes

- `REQ-F-9` and `REQ-F-10` share a single block of two criteria in the specification. The same two criteria therefore verify both requirements, and the criteria listed above are not additive across the pair.
- Coverage in this matrix reflects the specification as written. Several guarantees stated in requirement prose are not verified by any criterion — cross-Month recomputation on Date change (`REQ-F-5`), validation parity between creation and update (`REQ-F-10`), and permanence of deletion (`REQ-F-11`). The specification mandates test-first development for this logic under `REQ-DP-1`.