package com.judoscale.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilizationTrackerTest {

    private TestableUtilizationTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new TestableUtilizationTracker();
    }

    @Test
    void isNotStartedInitially() {
        assertThat(tracker.isStarted()).isFalse();
    }

    @Test
    void startMarksTrackerAsStarted() {
        tracker.start();
        assertThat(tracker.isStarted()).isTrue();
    }

    @Test
    void startIsIdempotent() {
        tracker.start();
        tracker.start();
        assertThat(tracker.isStarted()).isTrue();
    }

    @Test
    void incrIncrementsActiveRequestCount() {
        tracker.start();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(0);

        tracker.incr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(1);

        tracker.incr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(2);
    }

    @Test
    void decrDecrementsActiveRequestCount() {
        tracker.start();
        tracker.incr();
        tracker.incr();

        tracker.decr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(1);

        tracker.decr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(0);
    }

    @Test
    void tracksUtilizationPercentageBasedOnTimeSpentWithNoActiveRequests() {
        // T=0:   Start tracker
        // T=1:   Request 1 starts -> total_idle_time=1
        // T=2:   Request 1 ends   -> total_idle_time=1
        // T=4:   Request 2 starts -> total_idle_time=3 (1 + 2)
        // T=5:   Request 3 starts -> total_idle_time=3
        // T=6:   Request 2 ends   -> total_idle_time=3
        // T=8:   Request 3 ends   -> total_idle_time=3
        // T=10:  Report cycle     -> total_idle_time=5 (3 + 2), utilization_pct=50

        // T=0: Tracker starts
        tracker.setCurrentTime(0);
        tracker.start();
        assertThat(tracker.utilizationPct(false)).isEqualTo(100); // No time has passed yet

        // T=1: Request 1 starts
        tracker.setCurrentTime(1);
        tracker.incr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(0); // 1 second idle out of 1 total second = 100% idle

        // T=2: Request 1 ends
        tracker.setCurrentTime(2);
        tracker.decr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(50); // 1 second idle out of 2 total seconds = 50% idle

        // T=4: Request 2 starts
        tracker.setCurrentTime(4);
        tracker.incr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(25); // 3 seconds idle out of 4 total seconds = 75% idle

        // T=5: Request 3 starts
        tracker.setCurrentTime(5);
        tracker.incr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(40); // 3 seconds idle out of 5 total seconds = 60% idle

        // T=6: Request 2 ends
        tracker.setCurrentTime(6);
        tracker.decr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(50); // 3 seconds idle out of 6 total seconds = 50% idle

        // T=8: Request 3 ends
        tracker.setCurrentTime(8);
        tracker.decr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(62); // 3 seconds idle out of 8 total seconds = 37.5% idle

        // T=10: Report cycle - should calculate final utilization percentage
        tracker.setCurrentTime(10);
        assertThat(tracker.utilizationPct()).isEqualTo(50); // 5 seconds idle out of 10 total seconds = 50% idle
    }

    @Test
    void resetsTheTrackingCycleWhenUtilizationPctIsRequested() {
        // T=0:   Start tracker
        // T=1:   Request 1 starts -> total_idle_time=1
        // T=2:   Request 1 ends   -> total_idle_time=1
        // T=4:   Report cycle     -> total_idle_time=3 (1 + 2), utilization_pct=25
        // T=5:   Request 2 starts -> total_idle_time=1
        // T=8:   Report cycle     -> total_idle_time=1 (request still running), utilization_pct=75

        // T=0: Tracker starts
        tracker.setCurrentTime(0);
        tracker.start();
        assertThat(tracker.utilizationPct(false)).isEqualTo(100); // No time has passed yet

        // T=1: Request 1 starts
        tracker.setCurrentTime(1);
        tracker.incr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(0); // 1 second idle out of 1 total second

        // T=2: Request 1 ends
        tracker.setCurrentTime(2);
        tracker.decr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(50); // 1 second idle out of 2 total seconds

        tracker.setCurrentTime(3);
        assertThat(tracker.utilizationPct(false)).isEqualTo(33); // 2 seconds idle out of 3 total seconds

        // T=4: Report cycle - this should reset the tracking cycle
        tracker.setCurrentTime(4);
        assertThat(tracker.utilizationPct()).isEqualTo(25); // 3 seconds idle out of 4 total seconds

        // T=5: Request 2 starts - new cycle started
        tracker.setCurrentTime(5);
        tracker.incr();
        assertThat(tracker.utilizationPct(false)).isEqualTo(0); // 1 second idle out of 1 total second

        // T=8: Report cycle
        tracker.setCurrentTime(8);
        assertThat(tracker.utilizationPct()).isEqualTo(75); // 1 second idle out of 4 total seconds = 25% idle
    }

    @Test
    void returns100PercentWhenConstantlyBusy() {
        tracker.setCurrentTime(0);
        tracker.start();

        tracker.incr(); // Start request

        tracker.setCurrentTime(10);
        assertThat(tracker.utilizationPct(false)).isEqualTo(100); // Always busy
    }

    @Test
    void returns0PercentWhenCompletelyIdle() {
        tracker.setCurrentTime(0);
        tracker.start();

        tracker.setCurrentTime(10);
        assertThat(tracker.utilizationPct(false)).isEqualTo(0); // Always idle
    }

    @Test
    void decrDoesNotGoNegative() {
        tracker.start();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(0);

        // Decrement without any prior increment - should stay at 0
        tracker.decr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(0);

        // Multiple decrements should still stay at 0
        tracker.decr();
        tracker.decr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(0);
    }

    @Test
    void decrAfterMismatchedCallsStaysAtZero() {
        tracker.start();

        // One increment
        tracker.incr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(1);

        // Two decrements - second one should be ignored
        tracker.decr();
        tracker.decr();
        assertThat(tracker.getActiveRequestCount()).isEqualTo(0);
    }

    @Test
    void isThreadSafe() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 1000;
        Thread[] threads = new Thread[threadCount];

        tracker.start();

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    tracker.incr();
                    // Simulate some work
                    Thread.yield();
                    tracker.decr();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        // After all operations, counter should be back to 0
        assertThat(tracker.getActiveRequestCount()).isEqualTo(0);
    }

    /**
     * A testable version of UtilizationTracker that allows controlling time.
     */
    private static class TestableUtilizationTracker extends UtilizationTracker {
        private long currentTime = 0;

        void setCurrentTime(long time) {
            this.currentTime = time;
        }

        @Override
        protected long getCurrentTime() {
            return currentTime;
        }
    }
}
