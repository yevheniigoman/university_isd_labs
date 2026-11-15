package com.runningtracker.training.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The fields submitted when creating or updating a Training (REQ-F-4).
 *
 * <p>Distance and Duration are received as {@link BigDecimal} so the ticket 5
 * validators can apply the documented range and truncation rules exactly,
 * without binary floating-point error.
 */
public record TrainingRequest(
        LocalDate date,
        BigDecimal distanceKm,
        BigDecimal durationSeconds
) {
}
