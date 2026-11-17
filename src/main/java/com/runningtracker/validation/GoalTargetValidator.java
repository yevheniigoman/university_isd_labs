package com.runningtracker.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Validates a Monthly Goal's Target distance and normalises it to whole metres
 * (REQ-F-15).
 *
 * <p>Accepted input is {@code [0.01, 1000]} km. The range is checked against
 * the submitted value, then any precision beyond two decimal places is
 * truncated toward zero: {@code 50} becomes {@code 50000} m and
 * {@code 50.239} becomes {@code 50230} m. The upper bound is wider than a
 * single Training's because a monthly target aggregates many runs.
 */
@Component
public class GoalTargetValidator {

    private static final BigDecimal MINIMUM_KM = new BigDecimal("0.01");
    private static final BigDecimal MAXIMUM_KM = new BigDecimal("1000");

    /**
     * @return the Target distance in whole metres
     * @throws GoalValidationException if the Target distance is missing or out of range
     */
    public int validateAndNormalise(BigDecimal targetKm) {
        if (targetKm == null) {
            throw new GoalValidationException("Target distance is required.");
        }

        if (targetKm.compareTo(MINIMUM_KM) < 0 || targetKm.compareTo(MAXIMUM_KM) > 0) {
            throw new GoalValidationException("Target distance must be between 0.01 and 1000 km.");
        }

        BigDecimal normalisedKm = targetKm.setScale(2, RoundingMode.DOWN);
        return normalisedKm.movePointRight(3).intValueExact();
    }
}
