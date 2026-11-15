package com.runningtracker.training.dto;

import com.runningtracker.domain.Training;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * A recorded Training as exposed by the API (REQ-F-4, REQ-F-9).
 *
 * <p>Distance is stored as whole metres but presented in kilometres with two
 * decimal places, matching the {@code Training} schema in
 * {@code docs/openapi.yml}.
 */
public record TrainingResponse(
        Long id,
        LocalDate date,
        BigDecimal distanceKm,
        int durationSeconds
) {

    public static TrainingResponse from(Training training) {
        BigDecimal distanceKm = BigDecimal.valueOf(training.getDistanceMetres(), 3)
                .setScale(2, RoundingMode.UNNECESSARY);
        return new TrainingResponse(
                training.getId(),
                training.getDate(),
                distanceKm,
                training.getDurationSeconds());
    }
}
