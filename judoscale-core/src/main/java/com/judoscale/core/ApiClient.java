package com.judoscale.core;

import java.util.List;

/**
 * Interface for the Judoscale API client.
 * Extracted for easier testing and to allow different implementations.
 */
public interface ApiClient {

    /**
     * Reports metrics to the Judoscale API.
     * Returns true if successful, false otherwise.
     */
    boolean reportMetrics(List<Metric> metrics);
}
