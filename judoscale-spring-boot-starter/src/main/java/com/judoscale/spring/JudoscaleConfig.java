package com.judoscale.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Judoscale.
 * Can be set via application.properties/yml or environment variables.
 */
@ConfigurationProperties(prefix = "judoscale")
public class JudoscaleConfig {

    /**
     * The base URL for the Judoscale API.
     * Typically set via JUDOSCALE_URL environment variable.
     */
    private String apiBaseUrl;

    /**
     * How often to report metrics, in seconds. Default is 10.
     */
    private int reportIntervalSeconds = 10;

    /**
     * Maximum request body size in bytes before ignoring queue time.
     * Large requests can skew queue time measurements. Default is 100KB.
     */
    private int maxRequestSizeBytes = 100_000;

    /**
     * Whether to ignore queue time for large requests. Default is true.
     */
    private boolean ignoreLargeRequests = true;

    /**
     * Log level for Judoscale logging. Default is INFO.
     */
    private String logLevel = "INFO";

    /**
     * Whether Judoscale is enabled. Default is true.
     */
    private boolean enabled = true;

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public int getReportIntervalSeconds() {
        return reportIntervalSeconds;
    }

    public void setReportIntervalSeconds(int reportIntervalSeconds) {
        this.reportIntervalSeconds = reportIntervalSeconds;
    }

    public int getMaxRequestSizeBytes() {
        return maxRequestSizeBytes;
    }

    public void setMaxRequestSizeBytes(int maxRequestSizeBytes) {
        this.maxRequestSizeBytes = maxRequestSizeBytes;
    }

    public boolean isIgnoreLargeRequests() {
        return ignoreLargeRequests;
    }

    public void setIgnoreLargeRequests(boolean ignoreLargeRequests) {
        this.ignoreLargeRequests = ignoreLargeRequests;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns true if the API URL is configured and not blank.
     */
    public boolean isConfigured() {
        return apiBaseUrl != null && !apiBaseUrl.isBlank();
    }
}
