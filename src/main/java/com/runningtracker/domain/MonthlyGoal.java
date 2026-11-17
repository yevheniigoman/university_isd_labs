package com.runningtracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.YearMonth;

/**
 * A target distance an account holder sets for one Month (REQ-F-15).
 *
 * <p>At most one Monthly Goal exists per account per Goal Month, enforced by a
 * unique constraint so replacement can never leave a duplicate. Only the
 * Target distance is stored: progress is derived from the current Trainings
 * and is never persisted.
 */
@Entity
@Table(
        name = "monthly_goals",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthly_goals_user_month",
                columnNames = {"user_id", "goal_month"})
)
public class MonthlyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount owner;

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "goal_month", nullable = false, length = 7)
    private YearMonth goalMonth;

    @Column(name = "target_metres", nullable = false)
    private int targetMetres;

    protected MonthlyGoal() {
    }

    public MonthlyGoal(UserAccount owner, YearMonth goalMonth, int targetMetres) {
        this.owner = owner;
        this.goalMonth = goalMonth;
        this.targetMetres = targetMetres;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public YearMonth getGoalMonth() {
        return goalMonth;
    }

    public int getTargetMetres() {
        return targetMetres;
    }

    /**
     * Replaces the Target distance (REQ-F-15). The value is expected to have
     * already passed {@code GoalTargetValidator}.
     */
    public void updateTarget(int targetMetres) {
        this.targetMetres = targetMetres;
    }
}
