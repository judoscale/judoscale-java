package com.judoscale.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ReporterTest {

    private MetricsStore metricsStore;
    private UtilizationTracker utilizationTracker;
    private TestApiClient apiClient;
    private ConfigBase config;
    private Reporter reporter;

    @BeforeEach
    void setUp() {
        metricsStore = new MetricsStore();
        utilizationTracker = new UtilizationTracker();
        apiClient = new TestApiClient();
        config = new ConfigBase();
        config.setApiBaseUrl("http://example.com/api/test-token");
        reporter = new Reporter(metricsStore, apiClient, config, utilizationTracker);
    }

    @Test
    void startDoesNothingWhenNotConfigured() {
        ConfigBase unconfiguredConfig = new ConfigBase();
        reporter = new Reporter(metricsStore, apiClient, unconfiguredConfig, utilizationTracker);

        reporter.start();

        assertThat(reporter.isStarted()).isFalse();
    }

    @Test
    void startMarksReporterAsStarted() {
        reporter.start();

        assertThat(reporter.isStarted()).isTrue();
    }

    @Test
    void startOnlyStartsOnce() {
        reporter.start();
        reporter.start();
        reporter.start();

        assertThat(reporter.isStarted()).isTrue();
    }

    @Test
    void reportMetricsDoesNothingWhenNotStarted() {
        metricsStore.push("qt", 100, Instant.now());

        reporter.reportMetrics();

        assertThat(apiClient.reportedMetricsCount).isEqualTo(0);
    }

    @Test
    void reportMetricsDoesNothingWhenNotConfigured() {
        ConfigBase unconfiguredConfig = new ConfigBase();
        reporter = new Reporter(metricsStore, apiClient, unconfiguredConfig, utilizationTracker);
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());

        reporter.reportMetrics();

        assertThat(apiClient.reportedMetricsCount).isEqualTo(0);
    }

    @Test
    void reportMetricsFlushesAndSendsCollectedMetrics() {
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());
        metricsStore.push("at", 50, Instant.now());

        reporter.reportMetrics();

        assertThat(apiClient.reportedMetricsCount).isEqualTo(2);
    }

    @Test
    void reportMetricsDoesNothingWhenNoMetrics() {
        reporter.start();

        reporter.reportMetrics();

        assertThat(apiClient.reportedMetricsCount).isEqualTo(0);
    }

    @Test
    void reportMetricsClearsMetricsAfterReporting() {
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());

        reporter.reportMetrics();

        assertThat(metricsStore.getMetrics()).isEmpty();
    }

    @Test
    void stopMarksReporterAsStopped() {
        reporter.start();
        assertThat(reporter.isStarted()).isTrue();

        reporter.stop();

        assertThat(reporter.isStarted()).isFalse();
    }

    @Test
    void reportMetricsDoesNothingAfterStop() {
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());
        reporter.stop();

        reporter.reportMetrics();

        assertThat(apiClient.reportedMetricsCount).isEqualTo(0);
    }

    @Test
    void reportMetricsCollectsUtilizationWhenTrackerIsStarted() {
        reporter.start();
        utilizationTracker.start();

        reporter.reportMetrics();

        assertThat(apiClient.reportedMetricsCount).isEqualTo(1);
    }

    @Test
    void reportMetricsDoesNotCollectUtilizationWhenTrackerIsNotStarted() {
        reporter.start();
        // Don't start utilizationTracker

        reporter.reportMetrics();

        assertThat(apiClient.reportedMetricsCount).isEqualTo(0);
    }

    // Test implementations

    private static class TestApiClient extends ApiClient {
        int reportedMetricsCount = 0;

        TestApiClient() {
            super(new ConfigBase(), new Adapter("test", "1.0.0"));
        }

        @Override
        public boolean reportMetrics(java.util.List<Metric> metrics) {
            reportedMetricsCount = metrics.size();
            return true;
        }

        @Override
        protected HttpResult sendRequest(String url, String json) {
            return HttpResult.success(200, "");
        }
    }
}
