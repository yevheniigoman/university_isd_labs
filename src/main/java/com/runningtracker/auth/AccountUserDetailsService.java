package com.runningtracker.auth;

import com.runningtracker.domain.UserAccount;
import com.runningtracker.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Loads an account for Spring Security's authentication provider. Emails are
 * looked up in lower case because registration stores them normalised
 * (REQ-F-1), which makes login case-insensitive too.
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final UserAccountRepository users;

    public AccountUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Account not found.");
        }

        UserAccount account = users.findByEmail(username.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Account not found."));

        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles("USER")
                .build();
    }
}
