# Task 5 — Schema tickets excluded from test-first coverage

Coding agent: OpenCode.
Scope: testing policy for tickets 1 (Users table) and 4 (Trainings table).

## Decisions

1. **Pure schema tickets do not require tests.** Tickets whose only deliverable is a database table (entity mapping and columns) are verified by the application compiling and Hibernate creating the schema, not by dedicated test cases. Test-first remains the rule for behaviour-bearing requirements (authentication, validation, history, chart). Requirement: REQ-DP-1.

2. **Remove the Stage 1 entity tests.** `TrainingTest` and `UserAccountTest` tested only persistence of columns that are already declared by the mapping. They were deleted rather than kept as low-value coverage. Requirement: REQ-DP-1.

3. **Remove the `spring-boot-data-jpa-test` dependency.** It existed solely to supply the `@DataJpaTest` slice for those tests. `spring-boot-starter-test` remains for the behaviour tests in later stages. Requirement: REQ-DP-1.

## Specification changes

None. The specification mandates test-first for requirements, not for individual schema tickets; this decision scopes that obligation.

## Version-control conflicts

None.

## Requirement traceability

REQ-DP-1, REQ-DP-2.
