package com.judoscale.spring;

import com.judoscale.core.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JudoscaleApiClientTest {

    private JudoscaleConfig config;
    private JudoscaleApiClient apiClient;

    @BeforeEach
    void setUp() {
        config = new JudoscaleConfig();
        config.setApiBaseUrl("http://example.com/api/test-token");
        apiClient = new JudoscaleApiClient(config);
    }

    @Test
    void buildReportJsonFormatsMetricsCorrectly() {
        Instant time = Instant.parse("2024-01-15T10:30:00Z");
        List<Metric> metrics = List.of(
            new Metric("qt", 100, time),
            new Metric("at", 50, time)
        );

        String json = apiClient.buildReportJson(metrics);

        assertThat(json).contains("\"metrics\":");
        assertThat(json).contains("[1705314600,100,\"qt\"]");
        assertThat(json).contains("[1705314600,50,\"at\"]");
        assertThat(json).contains("\"adapters\":");
        assertThat(json).contains("\"judoscale-spring-boot\"");
    }

    @Test
    void buildReportJsonIncludesQueueNameWhenPresent() {
        Instant time = Instant.parse("2024-01-15T10:30:00Z");
        List<Metric> metrics = List.of(
            new Metric("qd", 5, time, "default")
        );

        String json = apiClient.buildReportJson(metrics);

        assertThat(json).contains("[1705314600,5,\"qd\",\"default\"]");
    }

    @Test
    void buildReportJsonHandlesEmptyMetricsList() {
        String json = apiClient.buildReportJson(List.of());

        assertThat(json).contains("\"metrics\":[]");
    }

    @Test
    void reportMetricsReturnsFalseWhenNotConfigured() {
        config.setApiBaseUrl(null);

        boolean result = apiClient.reportMetrics(List.of(new Metric("qt", 100)));

        assertThat(result).isFalse();
    }
}
