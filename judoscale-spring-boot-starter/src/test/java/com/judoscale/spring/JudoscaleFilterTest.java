package com.judoscale.spring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JudoscaleFilterTest {

    private MetricsStore metricsStore;
    private JudoscaleConfig config;
    private JudoscaleFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        metricsStore = new MetricsStore();
        config = new JudoscaleConfig();
        config.setApiBaseUrl("http://example.com/api/test-token");
        filter = new JudoscaleFilter(metricsStore, config);

        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/foo");

        response = new MockHttpServletResponse();
    }

    @Test
    void passesRequestThroughTheFilterChain() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void collectsApplicationTimeForEveryRequest() throws Exception {
        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).identifier()).isEqualTo("at");
        assertThat(metrics.get(0).value()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void collectsQueueTimeWhenXRequestStartHeaderIsPresent() throws Exception {
        // 5 seconds ago in milliseconds
        long fiveSecondsAgo = Instant.now().toEpochMilli() - 5000;
        request.addHeader("X-Request-Start", String.valueOf(fiveSecondsAgo));

        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(2);

        Metric queueTimeMetric = metrics.get(0);
        assertThat(queueTimeMetric.identifier()).isEqualTo("qt");
        // Allow 100ms tolerance for test execution time
        assertThat(queueTimeMetric.value()).isBetween(4900L, 5100L);

        Metric appTimeMetric = metrics.get(1);
        assertThat(appTimeMetric.identifier()).isEqualTo("at");
    }

    @Test
    void exposesQueueTimeAsRequestAttribute() throws Exception {
        long fiveSecondsAgo = Instant.now().toEpochMilli() - 5000;
        request.addHeader("X-Request-Start", String.valueOf(fiveSecondsAgo));

        filter.doFilter(request, response, filterChain);

        Object queueTime = request.getAttribute("judoscale.queue_time");
        assertThat(queueTime).isNotNull();
        assertThat((Long) queueTime).isBetween(4900L, 5100L);
    }

    @Test
    void doesNotCollectQueueTimeWhenHeaderIsMissing() throws Exception {
        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).identifier()).isEqualTo("at");
    }

    @Test
    void ignoresLargeRequestsWhenConfigured() throws Exception {
        config.setIgnoreLargeRequests(true);
        config.setMaxRequestSizeBytes(100_000);

        request.addHeader("X-Request-Start", String.valueOf(Instant.now().toEpochMilli() - 5000));
        request.setContentType("application/octet-stream");
        request.setContent(new byte[110_000]);

        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        // Should only have app time, not queue time
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).identifier()).isEqualTo("at");
    }

    @Test
    void tracksLargeRequestsWhenIgnoreLargeRequestsIsDisabled() throws Exception {
        config.setIgnoreLargeRequests(false);

        request.addHeader("X-Request-Start", String.valueOf(Instant.now().toEpochMilli() - 5000));
        request.setContent(new byte[110_000]);

        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(2);
        assertThat(metrics.get(0).identifier()).isEqualTo("qt");
    }

    @Test
    void handlesNginxFormatWithTPrefix() throws Exception {
        // NGINX format: "t=1234567890.123" (Unix timestamp in seconds with fractional part)
        double fiveSecondsAgoInSeconds = (Instant.now().toEpochMilli() - 5000) / 1000.0;
        request.addHeader("X-Request-Start", String.format("t=%.3f", fiveSecondsAgoInSeconds));

        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(2);
        assertThat(metrics.get(0).identifier()).isEqualTo("qt");
        assertThat(metrics.get(0).value()).isBetween(4900L, 5100L);
    }

    @Test
    void handlesNanosecondFormat() throws Exception {
        // Render uses nanoseconds
        long fiveSecondsAgoNanos = (Instant.now().toEpochMilli() - 5000) * 1_000_000;
        request.addHeader("X-Request-Start", String.valueOf(fiveSecondsAgoNanos));

        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(2);
        assertThat(metrics.get(0).identifier()).isEqualTo("qt");
        assertThat(metrics.get(0).value()).isBetween(4900L, 5100L);
    }

    @Test
    void handlesMicrosecondsFormat() throws Exception {
        long fiveSecondsAgoMicros = (Instant.now().toEpochMilli() - 5000) * 1_000;
        request.addHeader("X-Request-Start", String.valueOf(fiveSecondsAgoMicros));

        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(2);
        assertThat(metrics.get(0).identifier()).isEqualTo("qt");
        assertThat(metrics.get(0).value()).isBetween(4900L, 5100L);
    }

    @Test
    void safeguardsAgainstNegativeQueueTimes() throws Exception {
        // Future timestamp (shouldn't happen, but let's be safe)
        long futureTime = Instant.now().toEpochMilli() + 5000;
        request.addHeader("X-Request-Start", String.valueOf(futureTime));

        filter.doFilter(request, response, filterChain);

        List<Metric> metrics = metricsStore.flush();
        Metric queueTimeMetric = metrics.stream()
            .filter(m -> m.identifier().equals("qt"))
            .findFirst()
            .orElse(null);

        assertThat(queueTimeMetric).isNotNull();
        assertThat(queueTimeMetric.value()).isEqualTo(0);
    }

    @Test
    void collectsAppTimeEvenWhenExceptionIsThrown() throws Exception {
        doThrow(new ServletException("boom")).when(filterChain).doFilter(any(), any());

        try {
            filter.doFilter(request, response, filterChain);
        } catch (ServletException ignored) {
            // Expected
        }

        List<Metric> metrics = metricsStore.flush();
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).identifier()).isEqualTo("at");
    }
}
