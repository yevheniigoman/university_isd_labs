package com.runningtracker.auth.dto;

/**
 * Login credentials (REQ-F-2). Validation is deliberately lenient: any
 * unusable input is reported with the same generic failure as a wrong
 * password, so the endpoint never distinguishes the two.
 */
public record LoginRequest(String email, String password) {
}
