package com.runningtracker.auth.exception;

/**
 * Authentication failed. Always carries the single generic message required
 * by REQ-F-2, regardless of whether the email is unknown or the password is
 * wrong.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
