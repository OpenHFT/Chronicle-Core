package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleFunction;

import static net.openhft.chronicle.core.time.SystemTimeProvider.CLOCK;

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
        return "{ " + s + ", top10: " + top10.asString(toMicros, 5) + " }";
    }

    @Override
    public @NotNull String toLongMicrosFormat(@NotNull DoubleFunction<Double> toMicros) {
        final String s = super.toLongMicrosFormat(toMicros);
        return "{ " + s + ", top10: " + top10.asString(toMicros, 10) + " }";
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
