package com.judoscale.spring;

import com.judoscale.core.ApiClientBase;
import com.judoscale.core.MetricsStore;
import com.judoscale.core.UtilizationTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JudoscaleReporterTest {

    private MetricsStore metricsStore;
    private UtilizationTracker utilizationTracker;

    @Mock
    private ApiClientBase apiClient;

    private JudoscaleConfig config;
    private JudoscaleReporter reporter;

    @BeforeEach
    void setUp() {
        metricsStore = new MetricsStore();
        utilizationTracker = new UtilizationTracker();
        config = new JudoscaleConfig();
        config.setApiBaseUrl("http://example.com/api/test-token");
        reporter = new JudoscaleReporter(metricsStore, apiClient, config, utilizationTracker);
    }

    @Test
    void startDoesNothingWhenApiUrlIsNotConfigured() {
        config.setApiBaseUrl(null);

        reporter.start();

        assertThat(reporter.isStarted()).isFalse();
    }

    @Test
    void startDoesNothingWhenApiUrlIsBlank() {
        config.setApiBaseUrl("   ");

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
        // Verify no side effects from multiple starts
    }

    @Test
    void reportMetricsDoesNothingWhenNotStarted() {
        metricsStore.push("qt", 100, Instant.now());

        reporter.reportMetrics();

        verify(apiClient, never()).reportMetrics(any());
    }

    @Test
    void reportMetricsDoesNothingWhenApiUrlNotConfigured() {
        config.setApiBaseUrl(null);
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());

        reporter.reportMetrics();

        verify(apiClient, never()).reportMetrics(any());
    }

    @Test
    void reportMetricsFlushesAndSendsCollectedMetrics() {
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());
        metricsStore.push("at", 50, Instant.now());

        reporter.reportMetrics();

        verify(apiClient).reportMetrics(argThat(metrics ->
            metrics.size() == 2 &&
            metrics.get(0).identifier().equals("qt") &&
            metrics.get(1).identifier().equals("at")
        ));
    }

    @Test
    void reportMetricsDoesNothingWhenNoMetrics() {
        reporter.start();

        reporter.reportMetrics();

        verify(apiClient, never()).reportMetrics(any());
    }

    @Test
    void reportMetricsClearsMetricsAfterReporting() {
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());

        reporter.reportMetrics();

        assertThat(metricsStore.getMetrics()).isEmpty();
    }

    @Test
    void reportMetricsContinuesEvenWhenApiClientBaseThrowsException() {
        reporter.start();
        metricsStore.push("qt", 100, Instant.now());
        doThrow(new RuntimeException("boom")).when(apiClient).reportMetrics(any());

        // Should not throw
        reporter.reportMetrics();

        // Metrics were flushed (even though send failed)
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

        verify(apiClient, never()).reportMetrics(any());
    }

    @Test
    void reportMetricsCollectsUtilizationWhenTrackerIsStarted() {
        reporter.start();
        utilizationTracker.start();

        reporter.reportMetrics();

        // Should have sent the utilization metric
        verify(apiClient).reportMetrics(argThat(metrics ->
            metrics.size() == 1 &&
            metrics.get(0).identifier().equals("up")
        ));
    }

    @Test
    void reportMetricsDoesNotCollectUtilizationWhenTrackerIsNotStarted() {
        reporter.start();
        // Don't start utilizationTracker

        reporter.reportMetrics();

        // No metrics should be sent (tracker not started, no other metrics)
        verify(apiClient, never()).reportMetrics(any());
    }

    @Test
    void reportMetricsIncludesUtilizationWithOtherMetrics() {
        reporter.start();
        utilizationTracker.start();
        metricsStore.push("qt", 100, Instant.now());
        metricsStore.push("at", 50, Instant.now());

        reporter.reportMetrics();

        // Should have utilization + queue time + app time
        verify(apiClient).reportMetrics(argThat(metrics ->
            metrics.size() == 3 &&
            metrics.stream().anyMatch(m -> m.identifier().equals("up")) &&
            metrics.stream().anyMatch(m -> m.identifier().equals("qt")) &&
            metrics.stream().anyMatch(m -> m.identifier().equals("at"))
        ));
    }
}
