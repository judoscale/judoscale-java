package com.judoscale.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for Judoscale API clients.
 * Handles the common request flow, retry logic, and error handling.
 * Subclasses provide the HTTP implementation layer.
 */
public abstract class ApiClientBase {

    private static final Logger logger = LoggerFactory.getLogger(ApiClientBase.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 10;

    private final ConfigBase config;
    private final Adapter adapter;

    protected ApiClientBase(ConfigBase config, Adapter adapter) {
        this.config = config;
        this.adapter = adapter;
    }

    /**
     * Result of an HTTP request.
     */
    protected static class HttpResult {
        private final int statusCode;
        private final String body;
        private final Exception error;

        private HttpResult(int statusCode, String body, Exception error) {
            this.statusCode = statusCode;
            this.body = body;
            this.error = error;
        }

        public static HttpResult success(int statusCode, String body) {
            return new HttpResult(statusCode, body, null);
        }

        public static HttpResult error(Exception error) {
            return new HttpResult(-1, null, error);
        }

        public boolean isSuccess() {
            return error == null && statusCode >= 200 && statusCode < 300;
        }

        public boolean isHttpError() {
            return error == null && (statusCode < 200 || statusCode >= 300);
        }

        public boolean isNetworkError() {
            return error != null;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public Exception getError() {
            return error;
        }
    }

    /**
     * Sends an HTTP POST request with the given JSON body to the specified URL.
     * Implementations should handle timeouts and return an HttpResult.
     *
     * @param url the URL to send the request to
     * @param json the JSON body to send
     * @return the result of the HTTP request
     */
    protected abstract HttpResult sendRequest(String url, String json);

    public boolean reportMetrics(List<Metric> metrics) {
        if (!config.isConfigured()) {
            logger.debug("Judoscale API URL not configured, skipping report");
            return false;
        }

        String json = ReportBuilder.buildReportJson(metrics, Collections.singletonList(adapter));
        String url = config.getApiBaseUrl() + "/v3/reports";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            logger.debug("Posting {} bytes to {}", json.length(), url);

            HttpResult result = sendRequest(url, json);

            if (result.isSuccess()) {
                logger.debug("Reported successfully");
                return true;
            }

            if (result.isHttpError()) {
                logger.error("Reporter failed: {} - {}", result.getStatusCode(), result.getBody());
                return false;
            }

            // Network error - handle retry or failure
            Exception error = result.getError();
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            if (attempt < MAX_RETRIES) {
                logger.debug("Retry {} after error: {}", attempt, error.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            } else {
                logger.error("Could not connect to {}: {}", url, error.getMessage());
                return false;
            }
        }

        return false;
    }

    protected ConfigBase getConfig() {
        return config;
    }
}
