package com.judoscale.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single metric measurement.
 * Metrics: qt = queue time, at = application time, nt = network time, up = utilization percentage
 */
public final class Metric {

    private final String identifier;
    private final long value;
    private final Instant time;
    private final String queueName;

    /**
     * Creates a metric with all fields.
     */
    public Metric(String identifier, long value, Instant time, String queueName) {
        this.identifier = identifier;
        this.value = value;
        this.time = time;
        this.queueName = queueName;
    }

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

    public String identifier() {
        return identifier;
    }

    public long value() {
        return value;
    }

    public Instant time() {
        return time;
    }

    public String queueName() {
        return queueName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return value == metric.value &&
                Objects.equals(identifier, metric.identifier) &&
                Objects.equals(time, metric.time) &&
                Objects.equals(queueName, metric.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, value, time, queueName);
    }

    @Override
    public String toString() {
        return "Metric{" +
                "identifier='" + identifier + '\'' +
                ", value=" + value +
                ", time=" + time +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
