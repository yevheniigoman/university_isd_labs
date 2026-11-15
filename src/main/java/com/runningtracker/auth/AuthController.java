package com.runningtracker.auth;

import com.runningtracker.auth.dto.LoginRequest;
import com.runningtracker.auth.dto.RegisterRequest;
import com.runningtracker.auth.dto.SessionResponse;
import com.runningtracker.auth.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints (see {@code docs/openapi.yml}).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AccountService accountService, AuthenticationManager authenticationManager) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Creates the account if the email is free. Always {@code 204}, whether or
     * not the email is already registered (REQ-F-1 anti-enumeration).
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@Valid @RequestBody RegisterRequest request) {
        accountService.register(request.email(), request.password());
    }

    /**
     * Verifies the credentials and stores the authenticated context in a
     * server-side session (REQ-F-2, REQ-F-3). Unknown email and wrong password
     * produce the same {@code 401}.
     */
    @PostMapping("/login")
    public SessionResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (request == null
                || request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isEmpty()) {
            throw new InvalidCredentialsException();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        httpRequest.changeSessionId();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return new SessionResponse(authentication.getName());
    }

    /**
     * Returns the account associated with the current session (REQ-F-3).
     */
    @GetMapping("/session")
    public SessionResponse session(Authentication authentication) {
        return new SessionResponse(authentication.getName());
    }
}
