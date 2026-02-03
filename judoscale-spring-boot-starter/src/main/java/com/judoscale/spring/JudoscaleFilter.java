package com.judoscale.spring;

import com.judoscale.core.MetricsStore;
import com.judoscale.core.QueueTimeCalculator;
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
            long queueTimeMs = QueueTimeCalculator.calculateQueueTime(requestStartHeader, now);

            if (queueTimeMs >= 0) {
                metricsStore.push("qt", queueTimeMs, now);

                // Expose queue time to the application via request attribute
                httpRequest.setAttribute("judoscale.queue_time", queueTimeMs);

                logger.debug("Request queue_time={}ms request_id={} size={}",
                    queueTimeMs, requestId, contentLength);
            } else {
                logger.warn("Could not parse X-Request-Start header: {}", requestStartHeader);
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
}
