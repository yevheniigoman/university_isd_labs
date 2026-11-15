# Task 6 — Stage 2: Authentication

Coding agent: OpenCode.
Scope: tickets 2 (Registration) and 3 (Login) from `docs/plan.md`.

## Decisions

1. **Session-based authentication with Spring Security.** The authenticated `SecurityContext` is stored in the server-side `HttpSession` under Spring Security's standard key, so the client holds only an opaque session id (REQ-F-3). The API is stateless in shape but stateful in session, matching the specification.

2. **Custom JSON login endpoint instead of `formLogin`.** `POST /api/auth/login` calls the `AuthenticationManager` and saves the context itself. This keeps the contract exactly as documented in `docs/openapi.yml` (`200 {"email": ...}` / `401 {"message": ...}`) rather than Spring Security's redirect-and-form behaviour. Requirements: REQ-F-2, REQ-F-3.

3. **Registration always returns `204`, and the BCrypt hash is computed even for an already-registered email.** Returning the same status and doing the same expensive work makes the response indistinguishable in both content and rough timing. A lost insert race is caught and also reported as `204`. Requirements: REQ-F-1, REQ-NF-1.

4. **Login returns one generic `401 "Invalid email or password"`.** The endpoint never distinguishes an unknown email from a wrong password; Spring Security's `DaoAuthenticationProvider` already hides `UsernameNotFoundException` as `BadCredentialsException`, and the controller maps every `AuthenticationException` to the same body. Requirements: REQ-F-2.

5. **BCrypt through a `PasswordEncoder` bean.** `BCryptPasswordEncoder` is the only hashing mechanism; no plaintext password is stored, logged, or compared. Requirement: REQ-NF-1.

6. **Email lookup lower-cases the submitted address.** Registration stores the normalised form, so login is case-insensitive without a database-specific collation. Requirements: REQ-F-1.

7. **Session cookie is named `SESSION`, `HttpOnly`, `SameSite=Lax`, `Max-Age=30d`.** The specification requires a persistent, server-maintained session that survives closing the browser (REQ-F-3). `Secure` is omitted because HTTPS is explicitly out of scope (spec §4).

8. **Session fixation: `changeSessionId()` on successful login.** A new session id is issued before the authenticated context is attached, so a pre-login session id cannot be reused after authentication. Requirement: REQ-F-3.

9. **CSRF protection is disabled.** The documented API exchanges no CSRF token, and the spec's security scope excludes the machinery that would normally accompany it. This is a known limitation of the current contract, not a claim that CSRF is harmless; it is recorded here so it can be revisited if the API grows a browser client. Requirements: REQ-F-3, REQ-DP-2.

10. **Tests: MockMvc for behaviour, a real server for the cookie.** MockMvc exercises registration, login, the generic failure, case-insensitivity, and session reuse. MockMvc does not emulate the servlet container's `Set-Cookie` emission, so `LoginCookieTest` starts a real server and asserts `SESSION=`, `HttpOnly`, and `Max-Age`. Requirements: REQ-DP-1.

## Known limitations

- **Registration followed by login is still an enumeration oracle.** Anti-enumeration is satisfied at each endpoint, but an attacker can register an address and then attempt login: a `200` proves the account now exists, while a `204` followed by `401` suggests it already existed. Closing this requires email verification or rate limiting, both outside v1's scope. Requirements: REQ-F-1, REQ-F-2.
- **CSRF**, as recorded in decision 9.
- **Password length is bounded by characters (72), not bytes.** BCrypt operates on 72 bytes, so a 72-character multi-byte password is truncated by the encoder. The documented contract is character-based; this matches it. Requirement: REQ-NF-1.

## Specification changes

None.

## Version-control conflicts

None.

## Requirement traceability

REQ-F-1, REQ-F-2, REQ-F-3, REQ-NF-1, REQ-DP-1, REQ-DP-2.
