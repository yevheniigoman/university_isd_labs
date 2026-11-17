package com.runningtracker.goal.dto;

import java.math.BigDecimal;

/**
 * A Monthly Goal together with its derived progress (REQ-F-15).
 *
 * <p>Distances are presented in kilometres with two decimal places and Percent
 * complete with one decimal place. Completed distance is the Goal Month's
 * Month total distance, so it follows the current Trainings without any
 * correction step.
 */
public record GoalProgressResponse(
        String month,
        BigDecimal targetDistanceKm,
        BigDecimal completedDistanceKm,
        BigDecimal remainingDistanceKm,
        BigDecimal percentComplete
) {
}
