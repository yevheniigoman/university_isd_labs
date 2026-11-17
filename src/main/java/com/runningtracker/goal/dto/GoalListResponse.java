package com.runningtracker.goal.dto;

import java.util.List;

/**
 * The account's Monthly Goals within the Month Comparison Range, oldest Month
 * first (REQ-F-15).
 */
public record GoalListResponse(List<GoalProgressResponse> goals) {
}
