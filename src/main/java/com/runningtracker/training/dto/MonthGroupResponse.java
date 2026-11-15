package com.runningtracker.training.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * One Month of Training history, headed by that Month's total distance
 * (REQ-F-8, REQ-F-12).
 */
public record MonthGroupResponse(
        String month,
        BigDecimal totalDistanceKm,
        List<TrainingResponse> trainings
) {
}
