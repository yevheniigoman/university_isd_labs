package com.runningtracker.chart;

import com.runningtracker.chart.dto.MonthComparisonResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Month Comparison Chart data endpoint (REQ-F-13). Rendering is a frontend
 * concern and is not part of this API.
 */
@RestController
@RequestMapping("/api/month-comparison")
public class ChartController {

    private final MonthComparisonService monthComparisonService;

    public ChartController(MonthComparisonService monthComparisonService) {
        this.monthComparisonService = monthComparisonService;
    }

    @GetMapping
    public MonthComparisonResponse comparison(Authentication authentication) {
        return monthComparisonService.comparison(authentication.getName());
    }
}
