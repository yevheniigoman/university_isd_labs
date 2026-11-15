package com.runningtracker.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Validates a Training's Distance and normalises it to whole metres
 * (REQ-F-6).
 *
 * <p>Accepted input is {@code [0.01, 100]} km. The range is checked against
 * the submitted value, then any precision beyond two decimal places is
 * truncated toward zero: {@code 5} becomes {@code 5000} m, {@code 5.2}
 * becomes {@code 5200} m, and {@code 5.239} becomes {@code 5230} m. Because
 * two decimal places of a kilometre are exact multiples of 10 m, the accepted
 * value is stored losslessly; truncation never increases the distance.
 */
@Component
public class TrainingDistanceValidator {

    private static final BigDecimal MINIMUM_KM = new BigDecimal("0.01");
    private static final BigDecimal MAXIMUM_KM = new BigDecimal("100");

    /**
     * @return the Distance in whole metres
     * @throws TrainingValidationException if the Distance is missing or out of range
     */
    public int validateAndNormalise(BigDecimal distanceKm) {
        if (distanceKm == null) {
            throw new TrainingValidationException("Distance is required.");
        }

        if (distanceKm.compareTo(MINIMUM_KM) < 0 || distanceKm.compareTo(MAXIMUM_KM) > 0) {
            throw new TrainingValidationException("Distance must be between 0.01 and 100 km.");
        }

        BigDecimal normalisedKm = distanceKm.setScale(2, RoundingMode.DOWN);
        return normalisedKm.movePointRight(3).intValueExact();
    }
}
