package com.judoscale.spring;

import com.judoscale.core.ApiClientBase;
import com.judoscale.core.MetricsStore;
import com.judoscale.core.Reporter;
import com.judoscale.core.UtilizationTracker;

/**
 * Spring Boot wrapper for the core Reporter.
 * Provides the same functionality with Spring-compatible bean lifecycle.
 */
public class JudoscaleReporter extends Reporter {

    public JudoscaleReporter(MetricsStore metricsStore, ApiClientBase apiClient, JudoscaleConfig config,
                             UtilizationTracker utilizationTracker) {
        super(metricsStore, apiClient, config, utilizationTracker);
    }
}
