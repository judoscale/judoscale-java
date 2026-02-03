package com.judoscale.spring;

import com.judoscale.core.Adapter;
import com.judoscale.core.ApiClient;
import com.judoscale.core.ReportBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for sending metrics to the Judoscale API.
 * Uses Java 21's native HttpClient.
 */
public class JudoscaleApiClient extends ApiClient {

    private static final Adapter ADAPTER = new Adapter(
        "judoscale-spring-boot",
        ReportBuilder.loadAdapterVersion(JudoscaleApiClient.class)
    );

    private final HttpClient httpClient;

    public JudoscaleApiClient(JudoscaleConfig config) {
        super(config, ADAPTER);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    // Constructor for testing with mock HttpClient
    JudoscaleApiClient(JudoscaleConfig config, HttpClient httpClient) {
        super(config, ADAPTER);
        this.httpClient = httpClient;
    }

    @Override
    protected HttpResult sendRequest(String url, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            return HttpResult.success(response.statusCode(), response.body());

        } catch (IOException | InterruptedException e) {
            return HttpResult.error(e);
        }
    }
}
