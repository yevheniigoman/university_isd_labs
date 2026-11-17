package com.runningtracker.goal;

import com.runningtracker.goal.dto.GoalProgressResponse;
import com.runningtracker.history.MonthTotalAggregator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ-F-15: progress is derived from the Target and Completed distances, with
 * Remaining floored at zero and Percent complete uncapped to one decimal place.
 */
class GoalProgressCalculatorTest {

    private final GoalProgressCalculator calculator =
            new GoalProgressCalculator(new MonthTotalAggregator());

    @Test
    void reportsTargetCompletedRemainingAndPercent() {
        GoalProgressResponse progress = calculator.progress(YearMonth.of(2026, 11), 50_000, 32_500);

        assertThat(progress.month()).isEqualTo("2026-11");
        assertThat(progress.targetDistanceKm()).isEqualTo(new BigDecimal("50.00"));
        assertThat(progress.completedDistanceKm()).isEqualTo(new BigDecimal("32.50"));
        assertThat(progress.remainingDistanceKm()).isEqualTo(new BigDecimal("17.50"));
        assertThat(progress.percentComplete()).isEqualTo(new BigDecimal("65.0"));
    }

    @Test
    void reportsZeroCompletedAndFullRemainingBeforeAnyTraining() {
        GoalProgressResponse progress = calculator.progress(YearMonth.of(2026, 11), 50_000, 0);

        assertThat(progress.completedDistanceKm()).isEqualTo(new BigDecimal("0.00"));
        assertThat(progress.remainingDistanceKm()).isEqualTo(new BigDecimal("50.00"));
        assertThat(progress.percentComplete()).isEqualTo(new BigDecimal("0.0"));
    }

    @Test
    void floorsRemainingAtZeroAndLeavesPercentUncapped() {
        GoalProgressResponse progress = calculator.progress(YearMonth.of(2026, 11), 20_000, 25_000);

        assertThat(progress.remainingDistanceKm()).isEqualTo(new BigDecimal("0.00"));
        assertThat(progress.percentComplete()).isEqualTo(new BigDecimal("125.0"));
    }

    @Test
    void roundsPercentToOneDecimalPlace() {
        GoalProgressResponse progress = calculator.progress(YearMonth.of(2026, 11), 30_000, 10_000);

        assertThat(progress.percentComplete()).isEqualTo(new BigDecimal("33.3"));
    }
}
