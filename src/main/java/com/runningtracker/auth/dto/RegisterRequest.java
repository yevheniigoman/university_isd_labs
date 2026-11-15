package com.runningtracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration credentials (REQ-F-1). The email is compared
 * case-insensitively, so the account entity lower-cases it on construction.
 */
public record RegisterRequest(
        @NotBlank(message = "Invalid email format.")
        @Email(message = "Invalid email format.")
        @Size(max = 320, message = "Invalid email format.")
        String email,

        @NotBlank(message = "Password must be between 1 and 72 characters.")
        @Size(max = 72, message = "Password must be between 1 and 72 characters.")
        String password
) {
}
