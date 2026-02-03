package com.judoscale.core;

import java.time.Instant;

/**
 * Utility class for calculating request queue time from the X-Request-Start header.
 * Handles multiple formats: seconds, milliseconds, microseconds, nanoseconds.
 */
public final class QueueTimeCalculator {

    // Cutoffs for determining the unit of the X-Request-Start header
    private static final long MILLISECONDS_CUTOFF = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
    private static final long MICROSECONDS_CUTOFF = MILLISECONDS_CUTOFF * 1000;
    private static final long NANOSECONDS_CUTOFF = MICROSECONDS_CUTOFF * 1000;

    private QueueTimeCalculator() {
        // Utility class, no instantiation
    }

    /**
     * Calculates the queue time in milliseconds from the X-Request-Start header.
     * 
     * @param requestStartHeader the X-Request-Start header value
     * @param now the current instant
     * @return the queue time in milliseconds, or -1 if the header could not be parsed
     */
    public static long calculateQueueTime(String requestStartHeader, Instant now) {
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
            return -1;
        }
    }

    /**
     * Converts an integer timestamp to milliseconds based on its magnitude.
     */
    private static long convertToMillis(long value) {
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
