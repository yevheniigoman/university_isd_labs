package com.runningtracker.validation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * REQ-F-7: Duration is required, greater than zero, and stored in whole
 * seconds by truncation, never rounding.
 */
class TrainingDurationValidatorTest {

    private final TrainingDurationValidator validator = new TrainingDurationValidator();

    @Test
    void acceptsOneSecond() {
        assertThat(validator.validateAndTruncate(BigDecimal.ONE)).isEqualTo(1);
    }

    @Test
    void acceptsAWholeNumberOfSeconds() {
        assertThat(validator.validateAndTruncate(new BigDecimal("3600"))).isEqualTo(3600);
    }

    @Test
    void truncatesSubSecondComponents() {
        assertThat(validator.validateAndTruncate(new BigDecimal("1.9"))).isEqualTo(1);
    }

    @Test
    void truncatesRatherThanRoundsUp() {
        assertThat(validator.validateAndTruncate(new BigDecimal("2.999"))).isEqualTo(2);
    }

    @Test
    void acceptsLargeDurations() {
        assertThat(validator.validateAndTruncate(new BigDecimal("360000"))).isEqualTo(360_000);
    }

    @Test
    void rejectsZero() {
        assertThatThrownBy(() -> validator.validateAndTruncate(BigDecimal.ZERO))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Duration must be greater than zero.");
    }

    @Test
    void rejectsNegativeDuration() {
        assertThatThrownBy(() -> validator.validateAndTruncate(new BigDecimal("-1")))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Duration must be greater than zero.");
    }

    @Test
    void rejectsAValueThatTruncatesToZero() {
        assertThatThrownBy(() -> validator.validateAndTruncate(new BigDecimal("0.9")))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Duration must be greater than zero.");
    }

    @Test
    void rejectsMissingDuration() {
        assertThatThrownBy(() -> validator.validateAndTruncate(null))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Duration is required.");
    }
}
