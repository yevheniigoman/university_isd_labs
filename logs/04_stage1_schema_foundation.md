# Task 4 — Stage 1: Schema foundation

Coding agent: OpenCode.
Scope: tickets 1 (Users table) and 4 (Trainings table) from `docs/plan.md`.

## Decisions

1. **Spring Boot 4.1.2, Java 21 target.** The stack is mandated as Spring Boot + Spring Security + Hibernate. 4.1.2 is the current stable release and supports the installed JDK 25, while compiling to a Java 21 target reduces bytecode-compatibility risk for Hibernate's bytecode tooling. Requirement: REQ-DP-1, REQ-NF-3.

2. **H2 as the relational store.** Runtime uses a file-backed H2 database. This satisfies the relational two-table model without requiring an external database for v1. Requirements: REQ-NF-3.

3. **Entity named `UserAccount`, mapped to the `users` table.** Avoids a name collision with Spring Security's `User`, which will be needed for authentication in Stage 2, while keeping the specification's `users` table name. Requirements: REQ-NF-3, REQ-NF-1.

4. **Email normalised to lower case before storage, plus a unique constraint on the column.** Case-insensitive uniqueness is enforced portably in application code and in the schema, rather than relying on a database-specific collation. Requirements: REQ-F-1.

5. **Only a password hash column is stored.** The entity has `password_hash` and no plaintext password field; the hashing itself belongs to ticket 2 (registration). Requirement: REQ-NF-1.

6. **Training columns fixed to storage units.** `distance_metres` and `duration_seconds` are integer columns, and `training_date` is a `DATE` (renamed from `date` to avoid a reserved word). The date is a `LocalDate`, so no time or timezone can be stored. Requirements: REQ-F-4, REQ-F-6, REQ-F-7.

7. **Training ownership is a non-null foreign key to `users`.** Each Training has exactly one owner and its own generated identity. Requirements: REQ-F-4, REQ-F-14.

8. **Schema tickets are not covered by tests.** This entry originally added the Boot 4 `spring-boot-data-jpa-test` slice to test the two entities; the user subsequently decided that simple table-creation tickets do not need test cases, and both schema tests and the slice dependency were removed (see `05_schema_test_policy.md`). Requirement: REQ-DP-1.

## Specification changes

None.

## Version-control conflicts

None.

## Requirement traceability

REQ-NF-1, REQ-NF-3, REQ-F-1, REQ-F-4, REQ-F-6, REQ-F-7, REQ-DP-1, REQ-DP-2.
