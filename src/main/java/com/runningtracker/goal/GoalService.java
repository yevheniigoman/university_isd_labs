package com.runningtracker.goal;

import com.runningtracker.chart.MonthComparisonRange;
import com.runningtracker.domain.MonthlyGoal;
import com.runningtracker.domain.UserAccount;
import com.runningtracker.goal.dto.GoalListResponse;
import com.runningtracker.goal.dto.GoalProgressResponse;
import com.runningtracker.goal.dto.GoalRequest;
import com.runningtracker.history.MonthTotalAggregator;
import com.runningtracker.repository.MonthlyGoalRepository;
import com.runningtracker.repository.TrainingRepository;
import com.runningtracker.repository.UserAccountRepository;
import com.runningtracker.validation.GoalTargetValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Sets, reads, lists, and removes Monthly Goals for the authenticated account
 * (REQ-F-15).
 *
 * <p>The Target distance passes through its validator before any stored goal is
 * changed, so a rejected request leaves the existing goal untouched. Progress
 * is derived on every call from the account's current Trainings; no progress
 * figure is ever stored.
 */
@Service
public class GoalService {

    private final MonthlyGoalRepository goals;
    private final TrainingRepository trainings;
    private final UserAccountRepository users;
    private final MonthTotalAggregator aggregator;
    private final GoalProgressCalculator calculator;
    private final GoalTargetValidator targetValidator;
    private final Clock clock;

    public GoalService(MonthlyGoalRepository goals,
                       TrainingRepository trainings,
                       UserAccountRepository users,
                       MonthTotalAggregator aggregator,
                       GoalProgressCalculator calculator,
                       GoalTargetValidator targetValidator,
                       Clock clock) {
        this.goals = goals;
        this.trainings = trainings;
        this.users = users;
        this.aggregator = aggregator;
        this.calculator = calculator;
        this.targetValidator = targetValidator;
        this.clock = clock;
    }

    /**
     * Creates the Goal Month's goal or replaces its Target distance, then
     * returns the resulting progress (REQ-F-15).
     *
     * @throws com.runningtracker.validation.GoalValidationException if the Target distance is invalid
     */
    @Transactional
    public GoalProgressResponse set(String ownerEmail, YearMonth month, GoalRequest request) {
        int targetMetres = targetValidator.validateAndNormalise(
                request == null ? null : request.targetDistanceKm());

        MonthlyGoal goal = goals.findByOwnerEmailAndGoalMonth(ownerEmail, month)
                .map(existing -> {
                    existing.updateTarget(targetMetres);
                    return existing;
                })
                .orElseGet(() -> goals.save(new MonthlyGoal(owner(ownerEmail), month, targetMetres)));

        return progress(ownerEmail, goal);
    }

    /**
     * @throws GoalNotFoundException if the account has no goal for the Month,
     *                               including when the Month's goal belongs to another account
     */
    @Transactional(readOnly = true)
    public GoalProgressResponse get(String ownerEmail, YearMonth month) {
        MonthlyGoal goal = goals.findByOwnerEmailAndGoalMonth(ownerEmail, month)
                .orElseThrow(GoalNotFoundException::new);
        return progress(ownerEmail, goal);
    }

    /**
     * Permanently removes the Goal Month's goal (REQ-F-15).
     *
     * @throws GoalNotFoundException if the account has no goal for the Month,
     *                               including when the Month's goal belongs to another account
     */
    @Transactional
    public void delete(String ownerEmail, YearMonth month) {
        MonthlyGoal goal = goals.findByOwnerEmailAndGoalMonth(ownerEmail, month)
                .orElseThrow(GoalNotFoundException::new);
        goals.delete(goal);
    }

    /**
     * Lists the account's goals within the Month Comparison Range, oldest Month
     * first. Months without a goal are omitted (REQ-F-15).
     */
    @Transactional(readOnly = true)
    public GoalListResponse list(String ownerEmail) {
        List<YearMonth> range = MonthComparisonRange.endingAt(YearMonth.now(clock));
        Map<YearMonth, Long> completedByMonth =
                aggregator.totalMetresByMonth(trainings.findAllByOwnerEmail(ownerEmail));

        List<GoalProgressResponse> progress = goals.findAllByOwnerEmailOrderByGoalMonthAsc(ownerEmail).stream()
                .filter(goal -> range.contains(goal.getGoalMonth()))
                .map(goal -> calculator.progress(
                        goal.getGoalMonth(),
                        goal.getTargetMetres(),
                        completedByMonth.getOrDefault(goal.getGoalMonth(), 0L)))
                .toList();

        return new GoalListResponse(progress);
    }

    private GoalProgressResponse progress(String ownerEmail, MonthlyGoal goal) {
        long completedMetres = aggregator.totalMetres(
                trainings.findAllByOwnerEmail(ownerEmail), goal.getGoalMonth());
        return calculator.progress(goal.getGoalMonth(), goal.getTargetMetres(), completedMetres);
    }

    private UserAccount owner(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated account not found."));
    }
}
