package com.judoscale.spring;

import com.judoscale.core.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void reportMetricsReturnsFalseWhenNotConfigured() {
        config.setApiBaseUrl(null);

        boolean result = apiClient.reportMetrics(List.of(new Metric("qt", 100)));

        assertThat(result).isFalse();
    }
}
