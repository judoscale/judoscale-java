package com.judoscale.spring;

import com.judoscale.core.Adapter;
import com.judoscale.core.ApiClient;
import com.judoscale.core.Metric;
import com.judoscale.core.ReportBuilder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * HTTP client for sending metrics to the Judoscale API.
 * Uses Apache HttpClient for Java 8 compatibility.
 */
public class JudoscaleApiClient implements ApiClient, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(JudoscaleApiClient.class);
    private static final int MAX_RETRIES = 3;
    private static final Adapter ADAPTER = new Adapter(
        "judoscale-spring-boot-2",
        ReportBuilder.loadAdapterVersion(JudoscaleApiClient.class)
    );

    private final JudoscaleConfig config;
    private final CloseableHttpClient httpClient;

    public JudoscaleApiClient(JudoscaleConfig config) {
        this.config = config;

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(10000)
            .setConnectionRequestTimeout(5000)
            .build();

        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
    }

    // Constructor for testing with mock HttpClient
    JudoscaleApiClient(JudoscaleConfig config, CloseableHttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    @Override
    public boolean reportMetrics(List<Metric> metrics) {
        if (!config.isConfigured()) {
            logger.debug("Judoscale API URL not configured, skipping report");
            return false;
        }

        String json = ReportBuilder.buildReportJson(metrics, Collections.singletonList(ADAPTER), config.getRuntimeContainer());
        String url = config.getApiBaseUrl() + "/v3/reports";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpPost request = new HttpPost(url);
                request.setHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

                logger.debug("Posting {} bytes to {}", json.length(), url);

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = response.getEntity() != null
                        ? EntityUtils.toString(response.getEntity())
                        : "";

                    if (statusCode >= 200 && statusCode < 300) {
                        logger.debug("Reported successfully");
                        return true;
                    } else {
                        logger.error("Reporter failed: {} - {}", statusCode, responseBody);
                        return false;
                    }
                }

            } catch (IOException e) {
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
     * Closes the underlying HTTP client and releases any system resources associated with it.
     * This includes connection pools and background threads maintained by Apache HttpClient.
     */
    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
            logger.debug("HTTP client closed");
        }
    }
}
