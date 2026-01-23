package com.judoscale.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for sending metrics to the Judoscale API.
 */
public class JudoscaleApiClient implements ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(JudoscaleApiClient.class);
    private static final int MAX_RETRIES = 3;

    private final JudoscaleConfig config;
    private final HttpClient httpClient;

    public JudoscaleApiClient(JudoscaleConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    // Constructor for testing with mock HttpClient
    JudoscaleApiClient(JudoscaleConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    @Override
    public boolean reportMetrics(List<Metric> metrics) {
        if (!config.isConfigured()) {
            logger.debug("Judoscale API URL not configured, skipping report");
            return false;
        }

        String json = buildReportJson(metrics);
        String url = config.getApiBaseUrl() + "/v3/reports";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10))
                    .build();

                logger.debug("Posting {} bytes to {}", json.length(), url);

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    logger.debug("Reported successfully");
                    return true;
                } else {
                    logger.error("Reporter failed: {} - {}", response.statusCode(), response.body());
                    return false;
                }

            } catch (IOException | InterruptedException e) {
                if (attempt < MAX_RETRIES) {
                    logger.debug("Retry {} after error: {}", attempt, e.getMessage());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                } else {
                    logger.error("Could not connect to {}: {}", url, e.getMessage());
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Builds the JSON payload for the metrics report.
     */
    String buildReportJson(List<Metric> metrics) {
        StringBuilder metricsJson = new StringBuilder("[");

        for (int i = 0; i < metrics.size(); i++) {
            Metric m = metrics.get(i);
            if (i > 0) metricsJson.append(",");

            // Format: [timestamp, value, identifier, queueName?]
            metricsJson.append("[")
                .append(m.time().getEpochSecond())
                .append(",")
                .append(m.value())
                .append(",\"")
                .append(m.identifier())
                .append("\"");

            if (m.queueName() != null) {
                metricsJson.append(",\"").append(m.queueName()).append("\"");
            }

            metricsJson.append("]");
        }

        metricsJson.append("]");

        return """
            {"metrics":%s,"adapters":{"judoscale-spring-boot":{"adapter_version":"0.1.0"}}}
            """.formatted(metricsJson.toString()).trim();
    }
}
