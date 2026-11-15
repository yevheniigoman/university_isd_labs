package com.runningtracker.auth.dto;

/**
 * The authenticated account (see {@code docs/openapi.yml}, schema
 * {@code Session}).
 */
public record SessionResponse(String email) {
}
