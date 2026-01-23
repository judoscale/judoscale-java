package com.judoscale.spring;

import java.time.Instant;

/**
 * Represents a single metric measurement.
 * Metrics: qt = queue time, at = application time, nt = network time
 */
public record Metric(
    String identifier,
    long value,
    Instant time,
    String queueName
) {
    /**
     * Creates a web request metric (no queue name).
     */
    public Metric(String identifier, long value, Instant time) {
        this(identifier, value, time, null);
    }

    /**
     * Creates a metric with the current time.
     */
    public Metric(String identifier, long value) {
        this(identifier, value, Instant.now(), null);
    }
}
