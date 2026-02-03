package com.judoscale.core;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background reporter that sends collected metrics to the Judoscale API.
 * Runs on a fixed schedule (default: every 10 seconds).
 * 
 * <p>This class is framework-agnostic; the scheduling mechanism is provided
 * by the framework-specific starter (e.g., Spring Boot's @Scheduled).</p>
 */
public class Reporter {

    private static final Logger logger = Logger.getLogger(Reporter.class.getName());

    private final MetricsStore metricsStore;
    private final ApiClient apiClient;
    private final ConfigBase config;
    private final UtilizationTracker utilizationTracker;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public Reporter(MetricsStore metricsStore, ApiClient apiClient, ConfigBase config,
                    UtilizationTracker utilizationTracker) {
        this.metricsStore = metricsStore;
        this.apiClient = apiClient;
        this.config = config;
        this.utilizationTracker = utilizationTracker;
    }

    /**
     * Starts the reporter.
     */
    public void start() {
        if (!config.isConfigured()) {
            logger.info("Set judoscale.api-base-url (JUDOSCALE_URL) to enable metrics reporting");
            return;
        }

        if (started.compareAndSet(false, true)) {
            logger.info("Judoscale reporter starting, will report every ~" +
                config.getReportIntervalSeconds() + " seconds");
        }
    }

    /**
     * Reports metrics to the API. Called on a schedule.
     */
    public void reportMetrics() {
        if (!started.get() || !config.isConfigured()) {
            return;
        }

        try {
            // Collect utilization metric if tracker has been started
            if (utilizationTracker.isStarted()) {
                int utilizationPct = utilizationTracker.utilizationPct();
                metricsStore.push("up", utilizationPct, Instant.now());
                logger.fine("Collected utilization: " + utilizationPct + "%");
            }

            List<Metric> metrics = metricsStore.flush();

            if (metrics.isEmpty()) {
                logger.fine("No metrics to report");
                return;
            }

            logger.info("Reporting " + metrics.size() + " metrics");
            apiClient.reportMetrics(metrics);

        } catch (Exception e) {
            // Log the exception but don't rethrow - we want the scheduled task to continue
            logger.log(Level.SEVERE, "Reporter error: " + e.getMessage(), e);
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
