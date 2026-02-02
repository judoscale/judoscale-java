package com.judoscale.core;

/**
 * Base configuration for Judoscale.
 * Contains all configuration properties and logic shared across frameworks.
 * 
 * <p>Framework-specific implementations (e.g., Spring Boot) should extend this
 * class and add their configuration binding annotations.</p>
 */
public class ConfigBase {

    /**
     * The base URL for the Judoscale API.
     * Typically set via JUDOSCALE_URL environment variable.
     */
    private String apiBaseUrl;

    /**
     * Alternative property for the API URL (maps to JUDOSCALE_URL env var via relaxed binding).
     */
    private String url;

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

    /**
     * Returns the API base URL, preferring explicit apiBaseUrl over url.
     */
    public String getApiBaseUrl() {
        // Prefer explicit apiBaseUrl, fall back to url (which binds to JUDOSCALE_URL)
        if (apiBaseUrl != null && !apiBaseUrl.trim().isEmpty()) {
            return apiBaseUrl;
        }
        return url;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        String configuredUrl = getApiBaseUrl();
        return configuredUrl != null && !configuredUrl.trim().isEmpty();
    }
}
