package com.runningtracker.dto;

/**
 * A standard error body (see {@code docs/openapi.yml}, schema {@code Error}).
 */
public record ErrorResponse(String message) {
}
