package com.judoscale.spring;

import com.judoscale.core.Adapter;
import com.judoscale.core.ApiClient;
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

/**
 * HTTP client for sending metrics to the Judoscale API.
 * Uses Apache HttpClient for Java 8 compatibility.
 */
public class JudoscaleApiClient extends ApiClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(JudoscaleApiClient.class);
    private static final Adapter ADAPTER = new Adapter(
        "judoscale-spring-boot-2",
        ReportBuilder.loadAdapterVersion(JudoscaleApiClient.class)
    );

    private final CloseableHttpClient httpClient;

    public JudoscaleApiClient(JudoscaleConfig config) {
        super(config, ADAPTER);

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
        super(config, ADAPTER);
        this.httpClient = httpClient;
    }

    @Override
    protected HttpResult sendRequest(String url, String json) {
        try {
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = response.getEntity() != null
                    ? EntityUtils.toString(response.getEntity())
                    : "";

                return HttpResult.success(statusCode, responseBody);
            }

        } catch (IOException e) {
            return HttpResult.error(e);
        }
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
