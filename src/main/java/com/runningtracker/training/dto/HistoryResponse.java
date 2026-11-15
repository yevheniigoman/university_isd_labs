package com.runningtracker.training.dto;

import java.util.List;

/**
 * The account's full Training history, grouped by Month, newest Month first
 * (REQ-F-8). An empty history is {@code {"months":[]}}.
 */
public record HistoryResponse(List<MonthGroupResponse> months) {
}
