package com.judoscale.spring;

import com.judoscale.core.ApiClient;
import com.judoscale.core.Metric;
import com.judoscale.core.MetricsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background reporter that sends collected metrics to the Judoscale API.
 * Runs on a fixed schedule (default: every 10 seconds).
 */
public class JudoscaleReporter {

    private static final Logger logger = LoggerFactory.getLogger(JudoscaleReporter.class);

    private final MetricsStore metricsStore;
    private final ApiClient apiClient;
    private final JudoscaleConfig config;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public JudoscaleReporter(MetricsStore metricsStore, ApiClient apiClient, JudoscaleConfig config) {
        this.metricsStore = metricsStore;
        this.apiClient = apiClient;
        this.config = config;
    }

    /**
     * Starts the reporter. Called automatically by Spring.
     */
    public void start() {
        if (!config.isConfigured()) {
            logger.debug("Set judoscale.api-base-url to enable metrics reporting");
            return;
        }

        if (started.compareAndSet(false, true)) {
            logger.info("Judoscale reporter starting, will report every ~{} seconds",
                config.getReportIntervalSeconds());
        }
    }

    /**
     * Reports metrics to the API. Called on a schedule.
     * The @Scheduled annotation is handled by JudoscaleAutoConfiguration.
     */
    public void reportMetrics() {
        if (!started.get() || !config.isConfigured()) {
            return;
        }

        try {
            List<Metric> metrics = metricsStore.flush();

            if (metrics.isEmpty()) {
                logger.debug("No metrics to report");
                return;
            }

            logger.info("Reporting {} metrics", metrics.size());
            apiClient.reportMetrics(metrics);

        } catch (Exception e) {
            // Log the exception but don't rethrow - we want the scheduled task to continue
            logger.error("Reporter error: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns whether the reporter has been started.
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * Stops the reporter.
     */
    public void stop() {
        started.set(false);
    }
}
