package com.runningtracker.validation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * REQ-F-15: a Target distance is accepted within [0.01, 1000] km, truncated
 * toward zero beyond two decimal places, and stored in whole metres.
 */
class GoalTargetValidatorTest {

    private final GoalTargetValidator validator = new GoalTargetValidator();

    @Test
    void acceptsTheMinimumTarget() {
        assertThat(validator.validateAndNormalise(new BigDecimal("0.01"))).isEqualTo(10);
    }

    @Test
    void acceptsTheMaximumTarget() {
        assertThat(validator.validateAndNormalise(new BigDecimal("1000"))).isEqualTo(1_000_000);
    }

    @Test
    void acceptsWholeKilometres() {
        assertThat(validator.validateAndNormalise(new BigDecimal("50"))).isEqualTo(50_000);
    }

    @Test
    void acceptsOneDecimalPlace() {
        assertThat(validator.validateAndNormalise(new BigDecimal("5.2"))).isEqualTo(5200);
    }

    @Test
    void truncatesPrecisionBeyondTwoDecimalPlaces() {
        assertThat(validator.validateAndNormalise(new BigDecimal("50.239"))).isEqualTo(50_230);
    }

    @Test
    void rejectsValuesBelowTheMinimum() {
        assertThatThrownBy(() -> validator.validateAndNormalise(new BigDecimal("0.009")))
                .isInstanceOf(GoalValidationException.class)
                .hasMessage("Target distance must be between 0.01 and 1000 km.");
        assertThatThrownBy(() -> validator.validateAndNormalise(BigDecimal.ZERO))
                .isInstanceOf(GoalValidationException.class)
                .hasMessage("Target distance must be between 0.01 and 1000 km.");
        assertThatThrownBy(() -> validator.validateAndNormalise(new BigDecimal("-1")))
                .isInstanceOf(GoalValidationException.class)
                .hasMessage("Target distance must be between 0.01 and 1000 km.");
    }

    @Test
    void rejectsValuesAboveTheMaximum() {
        assertThatThrownBy(() -> validator.validateAndNormalise(new BigDecimal("1000.01")))
                .isInstanceOf(GoalValidationException.class)
                .hasMessage("Target distance must be between 0.01 and 1000 km.");
    }

    @Test
    void rejectsAMissingTarget() {
        assertThatThrownBy(() -> validator.validateAndNormalise(null))
                .isInstanceOf(GoalValidationException.class)
                .hasMessage("Target distance is required.");
    }
}
