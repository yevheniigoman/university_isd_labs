package com.runningtracker.validation;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * REQ-F-5: a Date is a calendar date; the server rejects anything later than
 * its current date plus one day and imposes no earliest date.
 */
class TrainingDateValidatorTest {

    private static final LocalDate SERVER_TODAY = LocalDate.of(2026, 11, 15);

    private final TrainingDateValidator validator = new TrainingDateValidator(
            Clock.fixed(Instant.parse("2026-11-15T10:15:30Z"), ZoneOffset.UTC));

    @Test
    void acceptsTheCurrentDate() {
        assertThat(validator.validate(SERVER_TODAY)).isEqualTo(SERVER_TODAY);
    }

    @Test
    void acceptsTheOneDayToleranceBoundary() {
        LocalDate tomorrow = SERVER_TODAY.plusDays(1);

        assertThat(validator.validate(tomorrow)).isEqualTo(tomorrow);
    }

    @Test
    void acceptsAnyPastDate() {
        LocalDate distantPast = LocalDate.of(1970, 1, 1);

        assertThat(validator.validate(distantPast)).isEqualTo(distantPast);
    }

    @Test
    void rejectsTheDayAfterTomorrow() {
        LocalDate dayAfterTomorrow = SERVER_TODAY.plusDays(2);

        assertThatThrownBy(() -> validator.validate(dayAfterTomorrow))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Date must not be later than tomorrow.");
    }

    @Test
    void rejectsFarFutureDates() {
        LocalDate farFuture = SERVER_TODAY.plusYears(1);

        assertThatThrownBy(() -> validator.validate(farFuture))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Date must not be later than tomorrow.");
    }

    @Test
    void rejectsMissingDate() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(TrainingValidationException.class)
                .hasMessage("Date is required.");
    }
}
