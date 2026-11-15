package com.runningtracker.chart;

import com.runningtracker.domain.Training;
import com.runningtracker.history.MonthTotalAggregator;
import com.runningtracker.repository.TrainingRepository;
import com.runningtracker.chart.dto.MonthComparisonPoint;
import com.runningtracker.chart.dto.MonthComparisonResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Produces the Month Comparison Chart data (REQ-F-13).
 *
 * <p>The twelve-Month range comes from {@link MonthComparisonRange}, not from
 * the user's data. Each point is that Month's total alone, computed from the
 * account's current Trainings; a Month without Trainings is present with a
 * total of zero rather than omitted. Nothing is cumulative and no target is
 * introduced, so the response cannot produce either downstream.
 */
@Service
public class MonthComparisonService {

    private final TrainingRepository trainings;
    private final MonthTotalAggregator aggregator;
    private final Clock clock;

    public MonthComparisonService(TrainingRepository trainings,
                                  MonthTotalAggregator aggregator,
                                  Clock clock) {
        this.trainings = trainings;
        this.aggregator = aggregator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MonthComparisonResponse comparison(String ownerEmail) {
        List<Training> owned = trainings.findAllByOwnerEmail(ownerEmail);
        Map<YearMonth, Long> totalsByMonth = aggregator.totalMetresByMonth(owned);

        List<MonthComparisonPoint> months = MonthComparisonRange.endingAt(YearMonth.now(clock)).stream()
                .map(month -> new MonthComparisonPoint(
                        month.toString(),
                        aggregator.toKilometres(totalsByMonth.getOrDefault(month, 0L))))
                .toList();

        return new MonthComparisonResponse(months);
    }
}
