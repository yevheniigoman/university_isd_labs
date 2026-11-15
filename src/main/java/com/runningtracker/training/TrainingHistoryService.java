package com.runningtracker.training;

import com.runningtracker.domain.Training;
import com.runningtracker.history.MonthTotalAggregator;
import com.runningtracker.repository.TrainingRepository;
import com.runningtracker.training.dto.HistoryResponse;
import com.runningtracker.training.dto.MonthGroupResponse;
import com.runningtracker.training.dto.TrainingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the Training history (REQ-F-8).
 *
 * <p>Trainings are read newest date first, so the Month groups form in newest
 * Month first order as they are encountered. Each group's total is delegated
 * to {@link MonthTotalAggregator}, which recomputes from the current rows on
 * every request rather than reading a stored total (REQ-F-12). No pagination,
 * truncation, or limit is applied.
 */
@Service
public class TrainingHistoryService {

    private final TrainingRepository trainings;
    private final MonthTotalAggregator aggregator;

    public TrainingHistoryService(TrainingRepository trainings, MonthTotalAggregator aggregator) {
        this.trainings = trainings;
        this.aggregator = aggregator;
    }

    @Transactional(readOnly = true)
    public HistoryResponse history(String ownerEmail) {
        List<Training> all = trainings.findAllByOwnerEmailOrderByDateDescIdDesc(ownerEmail);

        Map<YearMonth, List<Training>> byMonth = new LinkedHashMap<>();
        for (Training training : all) {
            byMonth.computeIfAbsent(MonthTotalAggregator.monthOf(training), month -> new ArrayList<>())
                    .add(training);
        }

        List<MonthGroupResponse> months = new ArrayList<>(byMonth.size());
        for (Map.Entry<YearMonth, List<Training>> entry : byMonth.entrySet()) {
            YearMonth month = entry.getKey();
            months.add(new MonthGroupResponse(
                    month.toString(),
                    aggregator.totalKilometres(all, month),
                    entry.getValue().stream().map(TrainingResponse::from).toList()));
        }

        return new HistoryResponse(months);
    }
}
