package com.runningtracker.chart.dto;

import java.math.BigDecimal;

/**
 * One bar of the Month Comparison Chart: a Month and that Month's total
 * distance alone (REQ-F-13).
 */
public record MonthComparisonPoint(
        String month,
        BigDecimal totalDistanceKm
) {
}
