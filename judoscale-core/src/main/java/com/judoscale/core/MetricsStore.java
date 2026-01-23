package com.judoscale.core;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe storage for metrics collected from requests.
 * Metrics are stored until flushed for reporting.
 */
public class MetricsStore {

    private static final Duration MAX_AGE = Duration.ofMinutes(2);

    private final ConcurrentLinkedQueue<Metric> metrics = new ConcurrentLinkedQueue<>();
    private volatile Instant flushedAt = Instant.now();

    /**
     * Pushes a new metric to the store.
     * Metrics are ignored if it's been more than 2 minutes since the last flush,
     * to prevent unbounded memory growth if reporting fails.
     */
    public void push(String identifier, long value, Instant time) {
        push(identifier, value, time, null);
    }

    /**
     * Pushes a new metric with a queue name (for job metrics).
     */
    public void push(String identifier, long value, Instant time, String queueName) {
        // If it's been two minutes since clearing out the store, stop collecting metrics.
        // There could be an issue with the reporter, and continuing to collect will consume linear memory.
        if (flushedAt != null && flushedAt.isBefore(Instant.now().minus(MAX_AGE))) {
            return;
        }

        metrics.add(new Metric(identifier, value, time, queueName));
    }

    /**
     * Flushes all metrics from the store and returns them.
     * The store is cleared after this operation.
     */
    public List<Metric> flush() {
        flushedAt = Instant.now();
        List<Metric> flushed = new ArrayList<>();

        Metric metric;
        while ((metric = metrics.poll()) != null) {
            flushed.add(metric);
        }

        return flushed;
    }

    /**
     * Returns the current metrics without removing them (for testing).
     */
    public List<Metric> getMetrics() {
        return new ArrayList<>(metrics);
    }

    /**
     * Returns when metrics were last flushed.
     */
    public Instant getFlushedAt() {
        return flushedAt;
    }

    /**
     * Clears all metrics (for testing).
     */
    public void clear() {
        metrics.clear();
    }

    /**
     * Sets the flushed time (for testing stale metric handling).
     */
    void setFlushedAt(Instant time) {
        this.flushedAt = time;
    }
}
