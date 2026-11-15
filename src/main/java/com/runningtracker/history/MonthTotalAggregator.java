package com.runningtracker.history;

import com.runningtracker.domain.Training;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Computes Month total distances from the current Trainings (REQ-F-12).
 *
 * <p>Totals are summed in a single unit — whole metres — so no conversion
 * happens inside the aggregation. Kilometres are produced only at the boundary
 * by dividing the finished total by 1000. Nothing is persisted or cached:
 * every call recomputes from the supplied Trainings, so an added, deleted, or
 * re-dated Training is reflected on the next request without a correction
 * step.
 */
@Component
public class MonthTotalAggregator {

    /**
     * Partitions the Trainings by the calendar Month of their Date and sums
     * each Month's distances in metres.
     *
     * @return a map ordered oldest Month first
     */
    public Map<YearMonth, Long> totalMetresByMonth(Collection<Training> trainings) {
        Objects.requireNonNull(trainings, "trainings");

        Map<YearMonth, Long> totals = new TreeMap<>();
        for (Training training : trainings) {
            totals.merge(monthOf(training), (long) training.getDistanceMetres(), Long::sum);
        }
        return totals;
    }

    /**
     * @return the total distance in whole metres for the given Month, or zero
     *         when no Training falls within it
     */
    public long totalMetres(Collection<Training> trainings, YearMonth month) {
        Objects.requireNonNull(trainings, "trainings");
        Objects.requireNonNull(month, "month");

        long total = 0L;
        for (Training training : trainings) {
            if (month.equals(monthOf(training))) {
                total += training.getDistanceMetres();
            }
        }
        return total;
    }

    /**
     * @return the total distance in kilometres for the given Month, or
     *         {@code 0.00} when no Training falls within it
     */
    public BigDecimal totalKilometres(Collection<Training> trainings, YearMonth month) {
        return toKilometres(totalMetres(trainings, month));
    }

    /**
     * Converts an already-summed metre total to kilometres with two decimal
     * places. Stored distances are whole multiples of 10 m (REQ-F-6), so the
     * conversion is exact and {@code UNNECESSARY} fails loudly if that
     * invariant is ever broken.
     */
    public BigDecimal toKilometres(long totalMetres) {
        return BigDecimal.valueOf(totalMetres, 3).setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * The calendar Month a Training belongs to, derived from its Date.
     */
    public static YearMonth monthOf(Training training) {
        Objects.requireNonNull(training, "training");
        return YearMonth.from(training.getDate());
    }
}
