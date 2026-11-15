package com.runningtracker.auth;

import com.runningtracker.domain.UserAccount;
import com.runningtracker.repository.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Account registration (REQ-F-1).
 *
 * <p>The password is hashed with BCrypt before anything is stored (REQ-NF-1).
 * The hash is computed even when the email is already registered, so the
 * response time does not reveal whether an account exists.
 */
@Service
public class AccountService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers the account if the email is free. A duplicate email is not an
     * error: the caller reports the same outcome either way (REQ-F-1).
     */
    public void register(String email, String rawPassword) {
        String passwordHash = passwordEncoder.encode(rawPassword);
        String normalisedEmail = email.toLowerCase(Locale.ROOT);

        if (users.existsByEmail(normalisedEmail)) {
            return;
        }

        try {
            users.save(new UserAccount(normalisedEmail, passwordHash));
        } catch (DataIntegrityViolationException raceLost) {
            // A concurrent registration inserted the same email first.
            // The observable outcome is identical, so this is not an error.
        }
    }
}
