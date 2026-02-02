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
     * The current runtime container identifier.
     * Detected from environment variables at initialization.
     */
    private final String runtimeContainer;

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
     * Creates a new ConfigBase, detecting the runtime container from environment variables.
     */
    public ConfigBase() {
        this.runtimeContainer = detectRuntimeContainer();
    }

    /**
     * Detects the runtime container from various platform-specific environment variables.
     * Checks in order: JUDOSCALE_CONTAINER, DYNO (Heroku), RENDER_INSTANCE_ID (Render),
     * ECS_CONTAINER_METADATA_URI (AWS ECS), FLY_MACHINE_ID (Fly.io), RAILWAY_REPLICA_ID (Railway).
     * 
     * @return the detected container identifier, or empty string if not detected
     */
    private String detectRuntimeContainer() {
        String container = System.getenv("JUDOSCALE_CONTAINER");
        if (container != null && !container.isEmpty()) {
            return container;
        }

        // Heroku
        String dyno = System.getenv("DYNO");
        if (dyno != null && !dyno.isEmpty()) {
            return dyno;
        }

        // Render
        String renderInstanceId = System.getenv("RENDER_INSTANCE_ID");
        if (renderInstanceId != null && !renderInstanceId.isEmpty()) {
            String renderServiceId = System.getenv("RENDER_SERVICE_ID");
            if (renderServiceId != null && renderInstanceId.startsWith(renderServiceId + "-")) {
                return renderInstanceId.substring(renderServiceId.length() + 1);
            }
            return renderInstanceId;
        }

        // AWS ECS
        String ecsMetadataUri = System.getenv("ECS_CONTAINER_METADATA_URI");
        if (ecsMetadataUri != null && !ecsMetadataUri.isEmpty()) {
            int lastSlash = ecsMetadataUri.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < ecsMetadataUri.length() - 1) {
                return ecsMetadataUri.substring(lastSlash + 1);
            }
        }

        // Fly.io
        String flyMachineId = System.getenv("FLY_MACHINE_ID");
        if (flyMachineId != null && !flyMachineId.isEmpty()) {
            return flyMachineId;
        }

        // Railway
        String railwayReplicaId = System.getenv("RAILWAY_REPLICA_ID");
        if (railwayReplicaId != null && !railwayReplicaId.isEmpty()) {
            return railwayReplicaId;
        }

        return "";
    }

    /**
     * Returns the current runtime container identifier.
     * 
     * @return the runtime container, or empty string if not detected
     */
    public String getRuntimeContainer() {
        return runtimeContainer;
    }

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
