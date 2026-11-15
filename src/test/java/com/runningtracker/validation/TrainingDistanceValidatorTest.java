package com.runningtracker.validation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * REQ-F-6: Distance is accepted in [0.01 km, 100 km] and normalised to whole
 * metres, truncating precision beyond two decimal places toward zero.
 */
class TrainingDistanceValidatorTest {

    private final TrainingDistanceValidator validator = new TrainingDistanceValidator();

    @Test
    void normalisesAWholeNumberOfKilometres() {
        assertThat(validator.validateAndNormalise(new BigDecimal("5"))).isEqualTo(5000);
    }

    @Test
    void normalisesOneDecimalPlace() {
        assertThat(validator.validateAndNormalise(new BigDecimal("5.2"))).isEqualTo(5200);
    }

    @Test
    void acceptsTheMinimumDistance() {
        assertThat(validator.validateAndNormalise(new BigDecimal("0.01"))).isEqualTo(10);
    }

    @Test
    void acceptsTheMaximumDistance() {
        assertThat(validator.validateAndNormalise(new BigDecimal("100"))).isEqualTo(100_000);
    }

    @Test
    void acceptsTrailingZerosBeyondTwoDecimalPlaces() {
        assertThat(validator.validateAndNormalise(new BigDecimal("5.20"))).isEqualTo(5200);
    }

    @Test
    void rejectsZero() {
        assertThatThrownBy(() -> validator.validateAndNormalise(BigDecimal.ZERO))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Distance must be between 0.01 and 100 km.");
    }

    @Test
    void rejectsNegativeDistance() {
        assertThatThrownBy(() -> validator.validateAndNormalise(new BigDecimal("-0.01")))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Distance must be between 0.01 and 100 km.");
    }

    @Test
    void rejectsDistanceBelowTheMinimum() {
        assertThatThrownBy(() -> validator.validateAndNormalise(new BigDecimal("0.001")))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Distance must be between 0.01 and 100 km.");
    }

    @Test
    void rejectsDistanceAboveTheMaximum() {
        assertThatThrownBy(() -> validator.validateAndNormalise(new BigDecimal("100.01")))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Distance must be between 0.01 and 100 km.");
    }

    @Test
    void truncatesBeyondTwoDecimalPlacesInsteadOfRejecting() {
        assertThat(validator.validateAndNormalise(new BigDecimal("5.239"))).isEqualTo(5230);
    }

    @Test
    void truncatesRatherThanRoundsUp() {
        assertThat(validator.validateAndNormalise(new BigDecimal("5.999"))).isEqualTo(5990);
    }

    @Test
    void truncatesTowardTheMinimumBoundary() {
        assertThat(validator.validateAndNormalise(new BigDecimal("0.019"))).isEqualTo(10);
    }

    @Test
    void rejectsAboveTheMaximumEvenWhenTruncationWouldFit() {
        assertThatThrownBy(() -> validator.validateAndNormalise(new BigDecimal("100.009")))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Distance must be between 0.01 and 100 km.");
    }

    @Test
    void rejectsMissingDistance() {
        assertThatThrownBy(() -> validator.validateAndNormalise(null))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Distance is required.");
    }
}
