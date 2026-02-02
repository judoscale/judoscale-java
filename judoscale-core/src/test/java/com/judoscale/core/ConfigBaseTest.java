package com.judoscale.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigBaseTest {

    private ConfigBase config;

    @BeforeEach
    void setUp() {
        config = new ConfigBase();
    }

    @Test
    void defaultValues() {
        assertThat(config.getReportIntervalSeconds()).isEqualTo(10);
        assertThat(config.getMaxRequestSizeBytes()).isEqualTo(100_000);
        assertThat(config.isIgnoreLargeRequests()).isTrue();
        assertThat(config.getLogLevel()).isEqualTo("INFO");
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void isConfiguredReturnsFalseWhenUrlIsNull() {
        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfiguredReturnsFalseWhenUrlIsBlank() {
        config.setApiBaseUrl("   ");
        assertThat(config.isConfigured()).isFalse();
    }

    @Test
    void isConfiguredReturnsTrueWhenApiBaseUrlIsSet() {
        config.setApiBaseUrl("http://example.com/api/test-token");
        assertThat(config.isConfigured()).isTrue();
    }

    @Test
    void isConfiguredReturnsTrueWhenUrlIsSet() {
        config.setUrl("http://example.com/api/test-token");
        assertThat(config.isConfigured()).isTrue();
    }

    @Test
    void getApiBaseUrlPrefersApiBaseUrlOverUrl() {
        config.setApiBaseUrl("http://explicit.com");
        config.setUrl("http://fallback.com");

        assertThat(config.getApiBaseUrl()).isEqualTo("http://explicit.com");
    }

    @Test
    void getApiBaseUrlFallsBackToUrlWhenApiBaseUrlIsNull() {
        config.setUrl("http://fallback.com");

        assertThat(config.getApiBaseUrl()).isEqualTo("http://fallback.com");
    }

    @Test
    void getApiBaseUrlFallsBackToUrlWhenApiBaseUrlIsBlank() {
        config.setApiBaseUrl("   ");
        config.setUrl("http://fallback.com");

        assertThat(config.getApiBaseUrl()).isEqualTo("http://fallback.com");
    }

    @Test
    void settersUpdateValues() {
        config.setReportIntervalSeconds(30);
        config.setMaxRequestSizeBytes(50_000);
        config.setIgnoreLargeRequests(false);
        config.setLogLevel("DEBUG");
        config.setEnabled(false);

        assertThat(config.getReportIntervalSeconds()).isEqualTo(30);
        assertThat(config.getMaxRequestSizeBytes()).isEqualTo(50_000);
        assertThat(config.isIgnoreLargeRequests()).isFalse();
        assertThat(config.getLogLevel()).isEqualTo("DEBUG");
        assertThat(config.isEnabled()).isFalse();
    }

    @Test
    void runtimeContainerIsNeverNull() {
        // Runtime container is detected from environment variables.
        // In test environment without those vars set, it should return empty string, not null.
        assertThat(config.getRuntimeContainer()).isNotNull();
    }
}
