package com.runningtracker.chart;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * REQ-F-13: the Month Comparison Range is always the twelve Months ending with
 * the current Month, oldest first, regardless of the user's Trainings.
 */
class MonthComparisonRangeTest {

    @Test
    void containsTwelveMonths() {
        assertThat(MonthComparisonRange.endingAt(YearMonth.of(2026, 11))).hasSize(12);
    }

    @Test
    void startsElevenMonthsBeforeTheCurrentMonthAndEndsWithIt() {
        List<YearMonth> range = MonthComparisonRange.endingAt(YearMonth.of(2026, 11));

        assertThat(range.getFirst()).isEqualTo(YearMonth.of(2025, 12));
        assertThat(range.getLast()).isEqualTo(YearMonth.of(2026, 11));
    }

    @Test
    void ordersMonthsOldestFirstWithoutDuplicates() {
        List<YearMonth> range = MonthComparisonRange.endingAt(YearMonth.of(2026, 11));

        assertThat(range).isSorted();
        assertThat(range).doesNotHaveDuplicates();
    }

    @Test
    void crossesAYearBoundary() {
        List<YearMonth> range = MonthComparisonRange.endingAt(YearMonth.of(2026, 3));

        assertThat(range.getFirst()).isEqualTo(YearMonth.of(2025, 4));
        assertThat(range.getLast()).isEqualTo(YearMonth.of(2026, 3));
    }
}
