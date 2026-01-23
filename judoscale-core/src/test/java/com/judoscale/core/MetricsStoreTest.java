package com.judoscale.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsStoreTest {

    private MetricsStore store;

    @BeforeEach
    void setUp() {
        store = new MetricsStore();
    }

    @Test
    void pushKeepsTrackOfMetricsInMemory() {
        assertThat(store.getMetrics()).isEmpty();

        store.push("qt", 100, Instant.now());

        assertThat(store.getMetrics()).hasSize(1);
        assertThat(store.getMetrics().get(0).identifier()).isEqualTo("qt");
    }

    @Test
    void pushAcceptsMetricsWithQueueName() {
        store.push("qd", 5, Instant.now(), "default");

        assertThat(store.getMetrics()).hasSize(1);
        assertThat(store.getMetrics().get(0).queueName()).isEqualTo("default");
    }

    @Test
    void pushStopsTrackingMetricsAfterTwoMinutesToAvoidUnboundedMemoryGrowth() {
        Instant now = Instant.now();

        store.push("qt", 100, now);
        assertThat(store.getMetrics()).hasSize(1);

        // Simulate 121 seconds passing since last flush
        store.setFlushedAt(now.minusSeconds(121));

        store.push("qt", 200, Instant.now());
        // Should still be 1 because the second push was ignored
        assertThat(store.getMetrics()).hasSize(1);
    }

    @Test
    void flushReturnsAllMetricsAndClearsTheStore() {
        store.push("qt", 1, Instant.now());
        store.push("qt", 2, Instant.now());
        store.push("qt", 3, Instant.now());

        assertThat(store.getMetrics()).hasSize(3);

        List<Metric> flushed = store.flush();

        assertThat(flushed).hasSize(3);
        assertThat(store.getMetrics()).isEmpty();
    }

    @Test
    void flushUpdatesFlushedAtTimestamp() {
        Instant before = store.getFlushedAt();

        // Small delay to ensure time difference
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        store.flush();

        assertThat(store.getFlushedAt()).isAfter(before);
    }

    @Test
    void flushReturnsEmptyListWhenNoMetrics() {
        List<Metric> flushed = store.flush();

        assertThat(flushed).isEmpty();
    }

    @Test
    void isThreadSafe() throws InterruptedException {
        int threadCount = 10;
        int metricsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < metricsPerThread; j++) {
                    store.push("qt", j, Instant.now());
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(store.getMetrics()).hasSize(threadCount * metricsPerThread);
    }
}
