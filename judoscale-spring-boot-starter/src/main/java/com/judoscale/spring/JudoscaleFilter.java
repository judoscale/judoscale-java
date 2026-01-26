package com.judoscale.spring;

import com.judoscale.core.MetricsStore;
import com.judoscale.core.UtilizationTracker;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

/**
 * Servlet filter that measures request queue time and application time.
 * Queue time is calculated from the X-Request-Start header set by the load balancer.
 * Also tracks request utilization via UtilizationTracker.
 */
public class JudoscaleFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(JudoscaleFilter.class);

    // Cutoffs for determining the unit of the X-Request-Start header
    private static final long MILLISECONDS_CUTOFF = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
    private static final long MICROSECONDS_CUTOFF = MILLISECONDS_CUTOFF * 1000;
    private static final long NANOSECONDS_CUTOFF = MICROSECONDS_CUTOFF * 1000;

    private final MetricsStore metricsStore;
    private final JudoscaleConfig config;
    private final UtilizationTracker utilizationTracker;

    public JudoscaleFilter(MetricsStore metricsStore, JudoscaleConfig config, UtilizationTracker utilizationTracker) {
        this.metricsStore = metricsStore;
        this.config = config;
        this.utilizationTracker = utilizationTracker;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        Instant now = Instant.now();
        String requestStartHeader = httpRequest.getHeader("X-Request-Start");
        String requestId = httpRequest.getHeader("X-Request-Id");
        int contentLength = httpRequest.getContentLength();

        // Track queue time if header is present and request isn't too large
        if (requestStartHeader != null && shouldTrackQueueTime(contentLength)) {
            long queueTimeMs = calculateQueueTime(requestStartHeader, now);

            if (queueTimeMs >= 0) {
                metricsStore.push("qt", queueTimeMs, now);

                // Expose queue time to the application via request attribute
                httpRequest.setAttribute("judoscale.queue_time", queueTimeMs);

                logger.debug("Request queue_time={}ms request_id={} size={}",
                    queueTimeMs, requestId, contentLength);
            }
        }

        // Start utilization tracking on first request (lazy initialization)
        utilizationTracker.start();
        utilizationTracker.incr();

        // Measure application time
        long startNanos = System.nanoTime();

        try {
            chain.doFilter(request, response);
        } finally {
            long appTimeMs = (System.nanoTime() - startNanos) / 1_000_000;
            metricsStore.push("at", appTimeMs, now);
            utilizationTracker.decr();
        }
    }

    /**
     * Determines if we should track queue time based on request size.
     * Large requests can skew queue time due to network transfer time.
     */
    private boolean shouldTrackQueueTime(int contentLength) {
        if (!config.isIgnoreLargeRequests()) {
            return true;
        }
        return contentLength < 0 || contentLength <= config.getMaxRequestSizeBytes();
    }

    /**
     * Calculates the queue time in milliseconds from the X-Request-Start header.
     * Handles multiple formats: seconds, milliseconds, microseconds, nanoseconds.
     */
    long calculateQueueTime(String requestStartHeader, Instant now) {
        try {
            // Strip any non-numeric characters (e.g., "t=" prefix from NGINX)
            String cleanValue = requestStartHeader.replaceAll("[^0-9.]", "");

            long startTimeMs;

            // Use long parsing for integer values to avoid precision loss with large timestamps
            // (nanosecond timestamps can exceed double's precision)
            if (!cleanValue.contains(".")) {
                long value = Long.parseLong(cleanValue);
                startTimeMs = convertToMillis(value);
            } else {
                // Fractional values (typically seconds from NGINX)
                double value = Double.parseDouble(cleanValue);
                if (value > NANOSECONDS_CUTOFF) {
                    startTimeMs = (long) (value / 1_000_000);
                } else if (value > MICROSECONDS_CUTOFF) {
                    startTimeMs = (long) (value / 1_000);
                } else if (value > MILLISECONDS_CUTOFF) {
                    startTimeMs = (long) value;
                } else {
                    // Seconds with fractional part
                    startTimeMs = (long) (value * 1000);
                }
            }

            long queueTimeMs = now.toEpochMilli() - startTimeMs;

            // Safeguard against negative queue times
            return Math.max(0, queueTimeMs);

        } catch (NumberFormatException e) {
            logger.warn("Could not parse X-Request-Start header: {}", requestStartHeader);
            return -1;
        }
    }

    /**
     * Converts an integer timestamp to milliseconds based on its magnitude.
     */
    private long convertToMillis(long value) {
        if (value > NANOSECONDS_CUTOFF) {
            // Nanoseconds (Render)
            return value / 1_000_000;
        } else if (value > MICROSECONDS_CUTOFF) {
            // Microseconds
            return value / 1_000;
        } else if (value > MILLISECONDS_CUTOFF) {
            // Milliseconds (Heroku)
            return value;
        } else {
            // Seconds (integer seconds, rare but possible)
            return value * 1000;
        }
    }
}
