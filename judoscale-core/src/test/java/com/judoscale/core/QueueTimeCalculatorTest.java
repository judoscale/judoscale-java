package com.judoscale.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class QueueTimeCalculatorTest {

    @Test
    void calculateQueueTimeFromMilliseconds() {
        // Heroku format: milliseconds since epoch
        Instant now = Instant.parse("2024-01-15T10:30:00.100Z");
        String header = "1705314600000"; // 2024-01-15T10:30:00Z in ms

        long queueTime = QueueTimeCalculator.calculateQueueTime(header, now);

        assertThat(queueTime).isEqualTo(100);
    }

    @Test
    void calculateQueueTimeFromMicroseconds() {
        Instant now = Instant.parse("2024-01-15T10:30:00.100Z");
        String header = "1705314600000000"; // microseconds

        long queueTime = QueueTimeCalculator.calculateQueueTime(header, now);

        assertThat(queueTime).isEqualTo(100);
    }

    @Test
    void calculateQueueTimeFromNanoseconds() {
        // Render format: nanoseconds since epoch
        Instant now = Instant.parse("2024-01-15T10:30:00.100Z");
        String header = "1705314600000000000"; // nanoseconds

        long queueTime = QueueTimeCalculator.calculateQueueTime(header, now);

        assertThat(queueTime).isEqualTo(100);
    }

    @Test
    void calculateQueueTimeFromSecondsWithFraction() {
        // NGINX format: seconds with fractional part
        Instant now = Instant.parse("2024-01-15T10:30:00.100Z");
        String header = "1705314600.000"; // seconds

        long queueTime = QueueTimeCalculator.calculateQueueTime(header, now);

        assertThat(queueTime).isEqualTo(100);
    }

    @Test
    void calculateQueueTimeStripsNonNumericPrefix() {
        // NGINX sometimes prefixes with "t="
        Instant now = Instant.parse("2024-01-15T10:30:00.100Z");
        String header = "t=1705314600000";

        long queueTime = QueueTimeCalculator.calculateQueueTime(header, now);

        assertThat(queueTime).isEqualTo(100);
    }

    @Test
    void calculateQueueTimeReturnsZeroForNegativeValues() {
        // If the header timestamp is in the future, return 0
        Instant now = Instant.parse("2024-01-15T10:30:00Z");
        String header = "1705314601000"; // 1 second in the future

        long queueTime = QueueTimeCalculator.calculateQueueTime(header, now);

        assertThat(queueTime).isEqualTo(0);
    }

    @Test
    void calculateQueueTimeReturnsNegativeOneForInvalidHeader() {
        Instant now = Instant.now();

        long queueTime = QueueTimeCalculator.calculateQueueTime("invalid", now);

        assertThat(queueTime).isEqualTo(-1);
    }

    @Test
    void calculateQueueTimeReturnsNegativeOneForEmptyHeader() {
        Instant now = Instant.now();

        long queueTime = QueueTimeCalculator.calculateQueueTime("", now);

        assertThat(queueTime).isEqualTo(-1);
    }
}
