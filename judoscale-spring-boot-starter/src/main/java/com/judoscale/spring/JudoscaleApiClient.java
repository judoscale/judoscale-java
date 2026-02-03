package com.judoscale.spring;

import com.judoscale.core.Adapter;
import com.judoscale.core.ApiClient;
import com.judoscale.core.Metric;
import com.judoscale.core.ReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * HTTP client for sending metrics to the Judoscale API.
 */
public class JudoscaleApiClient implements ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(JudoscaleApiClient.class);
    private static final int MAX_RETRIES = 3;
    private static final Adapter ADAPTER = new Adapter(
        "judoscale-spring-boot",
        ReportBuilder.loadAdapterVersion(JudoscaleApiClient.class)
    );

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

        String json = ReportBuilder.buildReportJson(metrics, List.of(ADAPTER));
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
                // Restore interrupt status if this was an interrupt
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
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
}
