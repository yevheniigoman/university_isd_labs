package com.runningtracker.chart;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Month Comparison Range (REQ-F-13).
 *
 * <p>The range is always twelve Months, beginning with the Month eleven Months
 * before the current Month and ending with the current Month. It is derived
 * only from the calendar, never from the user's Trainings, so it is identical
 * for an empty history and a history spanning several years.
 */
public final class MonthComparisonRange {

    public static final int LENGTH = 12;

    private MonthComparisonRange() {
    }

    /**
     * @return the twelve Months in oldest-first order, ending with the given Month
     */
    public static List<YearMonth> endingAt(YearMonth currentMonth) {
        Objects.requireNonNull(currentMonth, "currentMonth");

        List<YearMonth> months = new ArrayList<>(LENGTH);
        for (int offset = LENGTH - 1; offset >= 0; offset--) {
            months.add(currentMonth.minusMonths(offset));
        }
        return List.copyOf(months);
    }
}
