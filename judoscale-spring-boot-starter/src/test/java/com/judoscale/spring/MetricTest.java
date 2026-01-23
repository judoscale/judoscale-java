package com.judoscale.spring;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MetricTest {

    @Test
    void createsMetricWithAllFields() {
        Instant now = Instant.now();
        Metric metric = new Metric("qt", 100, now, "default");

        assertThat(metric.identifier()).isEqualTo("qt");
        assertThat(metric.value()).isEqualTo(100);
        assertThat(metric.time()).isEqualTo(now);
        assertThat(metric.queueName()).isEqualTo("default");
    }

    @Test
    void createsWebMetricWithoutQueueName() {
        Instant now = Instant.now();
        Metric metric = new Metric("qt", 250, now);

        assertThat(metric.identifier()).isEqualTo("qt");
        assertThat(metric.value()).isEqualTo(250);
        assertThat(metric.time()).isEqualTo(now);
        assertThat(metric.queueName()).isNull();
    }

    @Test
    void createsMetricWithCurrentTime() {
        Instant before = Instant.now();
        Metric metric = new Metric("at", 50);
        Instant after = Instant.now();

        assertThat(metric.identifier()).isEqualTo("at");
        assertThat(metric.value()).isEqualTo(50);
        assertThat(metric.time()).isBetween(before, after);
        assertThat(metric.queueName()).isNull();
    }
}
