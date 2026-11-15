package com.runningtracker.validation;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Validates a Training's Date (REQ-F-5).
 *
 * <p>The Date is a calendar date with no time and no timezone. It is rejected
 * only when it is later than the server's current date plus one day; no
 * earliest date is imposed. The tolerance absorbs clients whose local date is
 * ahead of the server's. The {@link Clock} is injected so the boundary can be
 * tested deterministically.
 */
@Component
public class TrainingDateValidator {

    private final Clock clock;

    public TrainingDateValidator(Clock clock) {
        this.clock = clock;
    }

    /**
     * @return the validated Date unchanged
     * @throws TrainingValidationException if the Date is missing or beyond the ceiling
     */
    public LocalDate validate(LocalDate date) {
        if (date == null) {
            throw new TrainingValidationException("Date is required.");
        }

        LocalDate ceiling = LocalDate.now(clock).plusDays(1);
        if (date.isAfter(ceiling)) {
            throw new TrainingValidationException("Date must not be later than tomorrow.");
        }

        return date;
    }
}
