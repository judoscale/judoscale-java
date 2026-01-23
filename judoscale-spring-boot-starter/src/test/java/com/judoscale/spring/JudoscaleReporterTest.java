package com.judoscale.spring;

import com.judoscale.core.ApiClient;
import com.judoscale.core.MetricsStore;
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

    @Mock
    private ApiClient apiClient;

    private JudoscaleConfig config;
    private JudoscaleReporter reporter;

    @BeforeEach
    void setUp() {
        metricsStore = new MetricsStore();
        config = new JudoscaleConfig();
        config.setApiBaseUrl("http://example.com/api/test-token");
        reporter = new JudoscaleReporter(metricsStore, apiClient, config);
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
    void reportMetricsContinuesEvenWhenApiClientThrowsException() {
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
}
