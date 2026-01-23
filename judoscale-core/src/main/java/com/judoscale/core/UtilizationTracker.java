package com.judoscale.core;

/**
 * Tracks application utilization as the percentage of time spent processing requests vs idle.
 * 
 * Utilization is calculated as: (1 - idle_ratio) * 100
 * where idle_ratio is the fraction of time with no active requests.
 * 
 * This class is thread-safe and uses monotonic time (System.nanoTime) to avoid
 * issues with clock drift or adjustments.
 */
public class UtilizationTracker {

    private final Object lock = new Object();
    private int activeRequestCounter = 0;
    private boolean started = false;
    
    // Idle time tracking (in nanoseconds, using monotonic clock)
    private boolean isIdle = false;
    private long idleStartedAt = 0;
    private long totalIdleTime = 0;
    private long reportCycleStartedAt = 0;

    /**
     * Starts the utilization tracker. Must be called before tracking begins.
     * Typically called on the first request.
     */
    public void start() {
        synchronized (lock) {
            if (!started) {
                started = true;
                initIdleReportCycle();
            }
        }
    }

    /**
     * Returns whether the tracker has been started.
     */
    public boolean isStarted() {
        synchronized (lock) {
            return started;
        }
    }

    /**
     * Increments the active request counter. Call when a request starts.
     */
    public void incr() {
        synchronized (lock) {
            if (activeRequestCounter == 0 && isIdle) {
                // We were idle and now we're not - add to total idle time
                totalIdleTime += getCurrentTime() - idleStartedAt;
                isIdle = false;
            }

            activeRequestCounter++;
        }
    }

    /**
     * Decrements the active request counter. Call when a request ends.
     */
    public void decr() {
        synchronized (lock) {
            activeRequestCounter--;

            if (activeRequestCounter == 0) {
                // We're now idle - start tracking idle time
                idleStartedAt = getCurrentTime();
                isIdle = true;
            }
        }
    }

    /**
     * Calculates and returns the utilization percentage (0-100).
     * Resets the tracking cycle after calculation.
     */
    public int utilizationPct() {
        return utilizationPct(true);
    }

    /**
     * Calculates and returns the utilization percentage (0-100).
     * 
     * @param reset if true, resets the tracking cycle after calculation
     */
    public int utilizationPct(boolean reset) {
        synchronized (lock) {
            long currentTime = getCurrentTime();
            double idleRatio = getIdleRatio(currentTime);

            if (reset) {
                resetIdleReportCycle(currentTime);
            }

            return (int) ((1.0 - idleRatio) * 100.0);
        }
    }

    /**
     * Returns the current active request count (for testing/debugging).
     */
    public int getActiveRequestCount() {
        synchronized (lock) {
            return activeRequestCounter;
        }
    }

    /**
     * Returns the current monotonic time in nanoseconds.
     * Protected to allow overriding in tests.
     */
    protected long getCurrentTime() {
        return System.nanoTime();
    }

    private void initIdleReportCycle() {
        long currentTime = getCurrentTime();
        idleStartedAt = currentTime;
        isIdle = true;
        resetIdleReportCycle(currentTime);
    }

    private void resetIdleReportCycle(long currentTime) {
        totalIdleTime = 0;
        reportCycleStartedAt = currentTime;
    }

    private double getIdleRatio(long currentTime) {
        long totalReportCycleTime = currentTime - reportCycleStartedAt;

        if (totalReportCycleTime <= 0) {
            return 0.0;
        }

        // Capture remaining idle time if currently idle
        if (isIdle) {
            totalIdleTime += currentTime - idleStartedAt;
            // Update idleStartedAt so we don't double count on next read
            idleStartedAt = currentTime;
        }

        return (double) totalIdleTime / totalReportCycleTime;
    }
}
