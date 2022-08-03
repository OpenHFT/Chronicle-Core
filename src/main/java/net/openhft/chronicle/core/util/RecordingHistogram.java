/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleFunction;

import static net.openhft.chronicle.core.time.SystemTimeProvider.CLOCK;

/**
 * This is an extension of Histogram which include the top 5 or 10 longest samples, including the delay from the last reset.
 * e.g. the start is the same information as Histogram, the top list is the longest delays in decreasing order
 * <pre>
 * { 50/90 99/99.9 99.99 - worst  was: 500 / 900  1000 / 1000  1000 - 1000, top: [{ off: 10.0, dur: 1000.0 }, { off: 11.0, dur: 950.0 }, { off: 9.0, dur: 900.0 }, { off: 12.0, dur: 850.0 }, { off: 8.0, dur: 800.0 }] }
 * </pre>
 */
public class RecordingHistogram extends Histogram {
    private final Top10 top10 = new Top10();
    private long start;
    private int sampleCount;

    public RecordingHistogram() {
        super(26, 8, 1e6 / 1024);
    }

    @Override
    public void sampleNanos(long durationNs) {
        super.sampleNanos(durationNs);
        if (start == 0)
            start = currentTimeNanos();
        top10.add(durationNs);
    }

    protected long currentTimeNanos() {
        return CLOCK.currentTimeNanos();
    }

    @Override
    public @NotNull String toMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
        final String s = super.toMicrosFormat(toMicros);
        return "{ " + s + ", top: " + top10.asString(toMicros, 5) + " }";
    }

    @Override
    public @NotNull String toLongMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
        final String s = super.toLongMicrosFormat(toMicros);
        return "{ " + s + ", top: " + top10.asString(toMicros, 10) + " }";
    }

    @Override
    public void reset() {
        super.reset();
        sampleCount = 0;
        top10.reset();
    }

    @Override
    protected String was() {
        return " was: ";
    }

    class Top10 {
        final long[] top = new long[20];
        int count;

        void add(long duration) {
            if (count == 0 || duration > top[count * 2 - 1])
                add(currentTimeNanos(), duration);
        }

        void add(long time, long duration) {
            for (int i = 0; i < 20 && i < count * 2; i += 2) {
                long duration2 = top[i + 1];
                if (duration2 < duration) {
                    long time2 = top[i];
                    top[i] = time;
                    top[i + 1] = duration;
                    time = time2;
                    duration = duration2;
                }
            }
            if (count < 10) {
                top[count * 2] = time;
                top[count * 2 + 1] = duration;
                count++;
            }
        }

        void reset() {
            count = 0;
        }

        public String asString(DoubleFunction<Double> toMicros, int max) {
            if (count == 0)
                return "";
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            String sep = "";
            for (int i = 0, lim = Math.min(Math.min(20, max * 2), count * 2); i < lim; i += 2) {
                double offset = toMicros.apply(top[i] - start);
                double duration = toMicros.apply(top[i + 1]);
                sb.append(sep).append("{ off: ").append(offset).append(", dur: ").append(duration).append(" }");
                sep = ", ";
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
