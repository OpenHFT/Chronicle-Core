//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;

public class RecordingHistogramTest extends CoreTestCommon {
    @Test
    public void singleSample() {
        Histogram h = new MyRecordingHistogram();
        h.sampleNanos(100_000);
        assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 100.0 / 100.0  100.0 / 100.0  100.0 - 100.0, top: [{ off: 1.0, dur: 100.0 }] }", h.toMicrosFormat());
        for (int i = 1; i <= 10; i++)
            h.sampleNanos(i * 100_000L);
        for (int i = 19; i > 0; i -= 2)
            h.sampleNanos(i * 50_000L);
        assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 500 / 900  1000 / 1000  1000 - 1000, top: [{ off: 10.0, dur: 1000.0 }, { off: 11.0, dur: 950.0 }, { off: 9.0, dur: 900.0 }, { off: 12.0, dur: 850.0 }, { off: 8.0, dur: 800.0 }] }", h.toMicrosFormat());
    }

    @Test
    public void testSampleNanosAndTopDurations() {
        SystemTimeProvider.CLOCK = new SetTimeProvider();
        RecordingHistogram histogram = new RecordingHistogram();

        histogram.sampleNanos(1000);
        histogram.sampleNanos(2000);
        histogram.sampleNanos(5500);
        histogram.sampleNanos(3000);
        assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 2.002 / 5.50  5.50 / 5.50  5.50 - 5.50, " +
                        "top: [{ off: 0.0, dur: 5.5 }, { off: 0.0, dur: 3.0 }, { off: 0.0, dur: 2.0 }, { off: 0.0, dur: 1.0 }] }",
                histogram.toMicrosFormat());

        // Assert that formatted output reflects recorded samples
        String s = histogram.toMicrosFormat(d -> d);
        assertTrue(s.contains("top:"));
    }

    @Test
    public void testReset() {
        RecordingHistogram histogram = new RecordingHistogram();
        String noData = histogram.toMicrosFormat();
        histogram.sampleNanos(10000);
        assertNotEquals(noData, histogram.toMicrosFormat());
        histogram.reset();
        assertEquals(noData, histogram.toMicrosFormat());
    }

    @Test
    public void testSamples() {
        Histogram h = new MyRecordingHistogram();

        long seed = 2141;
        for (int i = 0; i <= 500; i++) {
            h.sampleNanos(seed);
            seed += 128_981;
            if (seed > 1_000_000)
                seed -= 1_000_000;
        }

        for (int i = 1; i <= 100; i++)
            assertEquals(i, percentile(h, i / 100.0), 1);

        assertEquals("{ 50/90 99/99.9 99.99 - worst  was: 500 / 900  990 / 998  998 - 998, " +
                        "top: [{ off: 32.0, dur: 998.963 }, { off: 36.0, dur: 997.374 }, { off: 39.0, dur: 995.785 }, { off: 41.0, dur: 994.196 }, { off: 43.0, dur: 992.607 }] }",
                h.toMicrosFormat());
        assertEquals("{ 50/90 97/99 99.7/99.9 99.97/99.99 - worst  was: 500 / 900  970 / 990  996 / 998  998 / 998 - 998, " +
                        "top: [{ off: 32.0, dur: 998.963 }, { off: 36.0, dur: 997.374 }, { off: 39.0, dur: 995.785 }, { off: 41.0, dur: 994.196 }, { off: 43.0, dur: 992.607 }, { off: 44.0, dur: 991.018 }, { off: 45.0, dur: 989.429 }, { off: 46.0, dur: 987.84 }, { off: 47.0, dur: 986.251 }, { off: 48.0, dur: 984.662 }] }",
                h.toLongMicrosFormat());
    }

    private int percentile(@NotNull Histogram h, double fraction) {
        return (int) h.percentile(fraction) / 10000;
    }

    private static class MyRecordingHistogram extends RecordingHistogram {
        long timeNS = 0;

        @Override
        protected long currentTimeNanos() {
            return timeNS += 1000;
        }
    }
}
