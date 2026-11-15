package com.runningtracker.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Validates a Training's Duration and stores it as whole seconds (REQ-F-7).
 *
 * <p>A sub-second component is truncated toward zero, never rounded up, so a
 * stored Duration is never longer than the one submitted. The Duration is
 * required and must be greater than zero after truncation.
 */
@Component
public class TrainingDurationValidator {

    private static final BigDecimal MAXIMUM_SECONDS = BigDecimal.valueOf(Integer.MAX_VALUE);

    /**
     * @return the Duration in whole seconds
     * @throws TrainingValidationException if the Duration is missing, truncates to zero,
     *                                     or exceeds the storage range
     */
    public int validateAndTruncate(BigDecimal durationSeconds) {
        if (durationSeconds == null) {
            throw new TrainingValidationException("Duration is required.");
        }

        if (durationSeconds.compareTo(MAXIMUM_SECONDS) > 0) {
            throw new TrainingValidationException("Duration is too large.");
        }

        int wholeSeconds = durationSeconds.setScale(0, RoundingMode.DOWN).intValueExact();
        if (wholeSeconds <= 0) {
            throw new TrainingValidationException("Duration must be greater than zero.");
        }

        return wholeSeconds;
    }
}
