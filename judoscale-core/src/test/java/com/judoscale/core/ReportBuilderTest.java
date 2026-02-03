package com.judoscale.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportBuilderTest {

    private static final Adapter TEST_ADAPTER = new Adapter("judoscale-test", "1.0.0");

    @Test
    void buildReportJsonFormatsMetricsCorrectly() {
        Instant time = Instant.parse("2024-01-15T10:30:00Z");
        List<Metric> metrics = Arrays.asList(
            new Metric("qt", 100, time),
            new Metric("at", 50, time)
        );

        String json = ReportBuilder.buildReportJson(metrics, Collections.singletonList(TEST_ADAPTER), "web.1");

        assertThat(json).contains("\"container\":\"web.1\"");
        assertThat(json).containsPattern("\"pid\":\\d+");
        assertThat(json).contains("\"metrics\":");
        assertThat(json).contains("[1705314600,100,\"qt\"]");
        assertThat(json).contains("[1705314600,50,\"at\"]");
        assertThat(json).contains("\"adapters\":");
        assertThat(json).contains("\"judoscale-test\"");
        assertThat(json).contains("\"adapter_version\":\"1.0.0\"");
    }

    @Test
    void buildReportJsonIncludesQueueNameWhenPresent() {
        Instant time = Instant.parse("2024-01-15T10:30:00Z");
        List<Metric> metrics = Collections.singletonList(
            new Metric("qd", 5, time, "default")
        );

        String json = ReportBuilder.buildReportJson(metrics, Collections.singletonList(TEST_ADAPTER), "web.1");

        assertThat(json).contains("[1705314600,5,\"qd\",\"default\"]");
    }

    @Test
    void buildReportJsonHandlesEmptyMetricsList() {
        String json = ReportBuilder.buildReportJson(Collections.emptyList(), Collections.singletonList(TEST_ADAPTER), "web.1");

        assertThat(json).contains("\"metrics\":[]");
        assertThat(json).contains("\"container\":\"web.1\"");
    }

    @Test
    void buildReportJsonEscapesSpecialCharactersInQueueName() {
        Instant time = Instant.parse("2024-01-15T10:30:00Z");
        List<Metric> metrics = Collections.singletonList(
            new Metric("qd", 5, time, "queue\"with\\special")
        );

        String json = ReportBuilder.buildReportJson(metrics, Collections.singletonList(TEST_ADAPTER), "web.1");

        assertThat(json).contains("\"queue\\\"with\\\\special\"");
    }

    @Test
    void buildReportJsonHandlesNullContainer() {
        String json = ReportBuilder.buildReportJson(Collections.emptyList(), Collections.singletonList(TEST_ADAPTER), null);

        assertThat(json).contains("\"container\":\"\"");
    }

    @Test
    void buildReportJsonHandlesEmptyContainer() {
        String json = ReportBuilder.buildReportJson(Collections.emptyList(), Collections.singletonList(TEST_ADAPTER), "");

        assertThat(json).contains("\"container\":\"\"");
    }

    @Test
    void buildReportJsonSupportsMultipleAdapters() {
        Adapter springBootAdapter = new Adapter("judoscale-spring-boot", "1.0.0");
        Adapter sidekiqAdapter = new Adapter("judoscale-sidekiq", "2.0.0");
        List<Adapter> adapters = Arrays.asList(springBootAdapter, sidekiqAdapter);

        String json = ReportBuilder.buildReportJson(Collections.emptyList(), adapters, "web.1");

        assertThat(json).contains("\"judoscale-spring-boot\"");
        assertThat(json).contains("\"judoscale-sidekiq\"");
        assertThat(json).contains("\"adapter_version\":\"1.0.0\"");
        assertThat(json).contains("\"adapter_version\":\"2.0.0\"");
    }

    @Test
    void buildReportJsonHandlesEmptyAdaptersList() {
        String json = ReportBuilder.buildReportJson(Collections.emptyList(), Collections.emptyList(), "web.1");

        assertThat(json).contains("\"adapters\":{}");
    }

    @Test
    void loadAdapterVersionReturnsUnknownWhenFileNotFound() {
        String version = ReportBuilder.loadAdapterVersion(ReportBuilderTest.class);

        assertThat(version).isEqualTo("unknown");
    }
}
