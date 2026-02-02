package com.judoscale.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for building JSON report payloads for the Judoscale API.
 */
public final class ReportBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ReportBuilder() {
        // Utility class, no instantiation
    }

    /**
     * Builds the JSON payload for the metrics report.
     * 
     * @param metrics the metrics to include in the report
     * @param adapterVersion the version of the adapter
     * @return the JSON string
     */
    public static String buildReportJson(List<Metric> metrics, String adapterVersion) {
        ObjectNode root = objectMapper.createObjectNode();

        // Build metrics array: each metric is [timestamp, value, identifier, queueName?]
        ArrayNode metricsArray = objectMapper.createArrayNode();
        for (Metric m : metrics) {
            ArrayNode metricArray = objectMapper.createArrayNode();
            metricArray.add(m.time().getEpochSecond());
            metricArray.add(m.value());
            metricArray.add(m.identifier());
            if (m.queueName() != null) {
                metricArray.add(m.queueName());
            }
            metricsArray.add(metricArray);
        }
        root.set("metrics", metricsArray);

        // Build adapters object
        ObjectNode adapters = objectMapper.createObjectNode();
        ObjectNode springBootAdapter = objectMapper.createObjectNode();
        springBootAdapter.put("adapter_version", adapterVersion);
        adapters.set("judoscale-spring-boot", springBootAdapter);
        root.set("adapters", adapters);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize metrics to JSON", e);
        }
    }

    /**
     * Loads the adapter version from the META-INF/judoscale.properties file.
     * Falls back to "unknown" if the file cannot be read.
     * 
     * @param loaderClass the class to use for loading the resource
     * @return the adapter version
     */
    public static String loadAdapterVersion(Class<?> loaderClass) {
        try (InputStream is = loaderClass.getResourceAsStream("/META-INF/judoscale.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("version", "unknown");
            }
        } catch (IOException e) {
            // Fall through to return unknown
        }
        return "unknown";
    }
}
