package com.runningtracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Locale;

/**
 * An account. The email is the login identity: it is unique and compared
 * case-insensitively, so it is normalised to lower case before storage
 * (REQ-F-1). Only a password hash is stored, never a plaintext password
 * (REQ-NF-1).
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    protected UserAccount() {
    }

    public UserAccount(String email, String passwordHash) {
        this.email = normalise(email);
        this.passwordHash = passwordHash;
    }

    private static String normalise(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
