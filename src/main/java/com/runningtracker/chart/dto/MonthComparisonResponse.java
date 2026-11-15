package com.runningtracker.chart.dto;

import java.util.List;

/**
 * Exactly twelve Months of totals for the Month Comparison Chart, oldest first
 * (REQ-F-13).
 */
public record MonthComparisonResponse(List<MonthComparisonPoint> months) {
}
