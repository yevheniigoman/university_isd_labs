package com.runningtracker.repository;

import com.runningtracker.domain.MonthlyGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface MonthlyGoalRepository extends JpaRepository<MonthlyGoal, Long> {

    Optional<MonthlyGoal> findByOwnerEmailAndGoalMonth(String email, YearMonth goalMonth);

    List<MonthlyGoal> findAllByOwnerEmailOrderByGoalMonthAsc(String email);
}
