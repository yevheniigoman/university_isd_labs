package com.runningtracker.goal.dto;

import java.math.BigDecimal;

/**
 * The body submitted when setting a Monthly Goal's Target distance (REQ-F-15).
 *
 * <p>The target is received as {@link BigDecimal} so the range and truncation
 * rules apply exactly, without binary floating-point error.
 */
public record GoalRequest(BigDecimal targetDistanceKm) {
}
