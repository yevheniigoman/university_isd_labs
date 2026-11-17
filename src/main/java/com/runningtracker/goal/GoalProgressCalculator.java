package com.runningtracker.goal;

import com.runningtracker.goal.dto.GoalProgressResponse;
import com.runningtracker.history.MonthTotalAggregator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Derives a Monthly Goal's progress from its Target distance and the Goal
 * Month's Completed distance (REQ-F-15).
 *
 * <p>Remaining distance is floored at zero, so an overshoot never reports a
 * negative remainder. Percent complete is rounded half-up to one decimal place
 * and is deliberately uncapped: exceeding the target yields a value above
 * {@code 100.0}. Nothing here is stored; callers supply the current Completed
 * distance on every call.
 */
@Component
public class GoalProgressCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final MonthTotalAggregator aggregator;

    public GoalProgressCalculator(MonthTotalAggregator aggregator) {
        this.aggregator = aggregator;
    }

    /**
     * @param month            the Goal Month
     * @param targetMetres     the Target distance in whole metres
     * @param completedMetres  the Goal Month's total distance in whole metres
     */
    public GoalProgressResponse progress(YearMonth month, int targetMetres, long completedMetres) {
        Objects.requireNonNull(month, "month");

        BigDecimal targetKm = aggregator.toKilometres(targetMetres);
        BigDecimal completedKm = aggregator.toKilometres(completedMetres);
        BigDecimal remainingKm = aggregator.toKilometres(Math.max(0L, targetMetres - (long) completedMetres));
        BigDecimal percentComplete = completedKm
                .multiply(HUNDRED)
                .divide(targetKm, 1, RoundingMode.HALF_UP);

        return new GoalProgressResponse(
                month.toString(), targetKm, completedKm, remainingKm, percentComplete);
    }
}
