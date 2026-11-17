package com.runningtracker.goal;

import com.runningtracker.goal.dto.GoalListResponse;
import com.runningtracker.goal.dto.GoalProgressResponse;
import com.runningtracker.goal.dto.GoalRequest;
import com.runningtracker.validation.GoalValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Monthly Goal endpoints (REQ-F-15): set or replace, read, delete, and list.
 * Goals are deliberately absent from the Month Comparison Chart, which
 * REQ-F-13 forbids from carrying a target line.
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * Creates or replaces the Month's goal and returns its progress.
     */
    @PutMapping("/{month}")
    public GoalProgressResponse set(@PathVariable String month,
                                    @RequestBody GoalRequest request,
                                    Authentication authentication) {
        return goalService.set(authentication.getName(), parseMonth(month), request);
    }

    /**
     * Returns one goal with its progress.
     */
    @GetMapping("/{month}")
    public GoalProgressResponse get(@PathVariable String month, Authentication authentication) {
        return goalService.get(authentication.getName(), parseMonth(month));
    }

    /**
     * Permanently removes the Month's goal.
     */
    @DeleteMapping("/{month}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String month, Authentication authentication) {
        goalService.delete(authentication.getName(), parseMonth(month));
    }

    /**
     * Lists the goals within the current Month Comparison Range, oldest first.
     */
    @GetMapping
    public GoalListResponse list(Authentication authentication) {
        return goalService.list(authentication.getName());
    }

    private static YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException exception) {
            throw new GoalValidationException("Month must be a valid year and month (YYYY-MM).");
        }
    }
}
