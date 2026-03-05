package com.judoscale.core;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for Judoscale API clients.
 * Handles the common request flow, retry logic, and error handling.
 * Subclasses provide the HTTP implementation layer.
 */
public abstract class ApiClientBase {

    protected static final Logger logger = Logger.getLogger(ApiClientBase.class.getName());
    private static final int MAX_RETRIES = 3;

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
            logger.fine("Judoscale API URL not configured, skipping report");
            return false;
        }

        String json = ReportBuilder.buildReportJson(metrics, Collections.singletonList(adapter), config.getRuntimeContainer());
        String url = config.getApiBaseUrl() + "/v3/reports";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            logger.log(Level.FINE, "Posting {0} bytes to {1}", new Object[]{json.length(), url});

            HttpResult result = sendRequest(url, json);

            if (result.isSuccess()) {
                logger.fine("Reported successfully");
                return true;
            }

            if (result.isHttpError()) {
                logger.log(Level.SEVERE, "Reporter failed: {0} - {1}", new Object[]{result.getStatusCode(), result.getBody()});
                return false;
            }

            // Network error - handle retry or failure
            Exception error = result.getError();
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            if (attempt < MAX_RETRIES) {
                long delayMs = (long) (250 * Math.pow(2, attempt - 1));
                logger.log(Level.FINE, "Retry {0} after error (waiting {1}ms): {2}", new Object[]{attempt, delayMs, error.getMessage()});
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            } else {
                logger.log(Level.SEVERE, "Could not connect to {0}: {1}", new Object[]{url, error.getMessage()});
                return false;
            }
        }

        return false;
    }

    protected ConfigBase getConfig() {
        return config;
    }
}
