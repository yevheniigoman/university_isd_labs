package com.runningtracker.history;

import com.runningtracker.domain.Training;
import com.runningtracker.domain.UserAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ-F-12: a Month total is the sum of the Distances of the Trainings dated
 * within that Month, computed from the current Trainings in a single unit
 * (metres) and never cached.
 */
class MonthTotalAggregatorTest {

    private static final UserAccount OWNER = new UserAccount("runner@example.com", "hash");

    private final MonthTotalAggregator aggregator = new MonthTotalAggregator();

    private static Training training(int year, int month, int day, int distanceMetres) {
        return new Training(LocalDate.of(year, month, day), distanceMetres, 1800, OWNER);
    }

    @Test
    void sumsDistancesWithinTheSameMonth() {
        List<Training> trainings = List.of(
                training(2026, 11, 1, 5200),
                training(2026, 11, 20, 4800));

        assertThat(aggregator.totalMetres(trainings, YearMonth.of(2026, 11))).isEqualTo(10_000L);
        assertThat(aggregator.totalKilometres(trainings, YearMonth.of(2026, 11)))
                .isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void returnsZeroForAMonthWithNoTrainings() {
        List<Training> trainings = List.of(training(2026, 10, 31, 7500));

        assertThat(aggregator.totalMetres(trainings, YearMonth.of(2026, 11))).isZero();
        assertThat(aggregator.totalKilometres(trainings, YearMonth.of(2026, 11)))
                .isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void returnsZeroForAnEmptyHistory() {
        assertThat(aggregator.totalMetres(List.of(), YearMonth.of(2026, 11))).isZero();
        assertThat(aggregator.totalKilometres(List.of(), YearMonth.of(2026, 11)))
                .isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void ignoresTrainingsFromOtherMonths() {
        List<Training> trainings = List.of(
                training(2026, 11, 1, 5200),
                training(2026, 10, 31, 7500));

        assertThat(aggregator.totalMetres(trainings, YearMonth.of(2026, 11))).isEqualTo(5200L);
    }

    @Test
    void partitionsTrainingsByMonthInAscendingOrder() {
        List<Training> trainings = List.of(
                training(2026, 11, 1, 5200),
                training(2026, 11, 20, 4800),
                training(2026, 10, 31, 7500));

        Map<YearMonth, Long> totals = aggregator.totalMetresByMonth(trainings);

        assertThat(totals).containsExactly(
                Map.entry(YearMonth.of(2026, 10), 7500L),
                Map.entry(YearMonth.of(2026, 11), 10_000L));
    }

    @Test
    void distinguishesTheSameMonthInDifferentYears() {
        List<Training> trainings = List.of(
                training(2025, 1, 15, 1000),
                training(2026, 1, 15, 2000));

        assertThat(aggregator.totalMetresByMonth(trainings))
                .containsEntry(YearMonth.of(2025, 1), 1000L)
                .containsEntry(YearMonth.of(2026, 1), 2000L);
    }
}
