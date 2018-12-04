package net.openhft.chronicle.core.threads;

import java.util.concurrent.TimeUnit;

public enum JitterSampler {
    ;
    public static final String PROFILE_OF_THE_THREAD = "profile of the thread";
    public static final String THREAD_HAS_BLOCKED_FOR = "thread has blocked for";

    static final long JITTER_THRESHOLD =
            TimeUnit.MILLISECONDS.toNanos(
                    Long.getLong("chronicle.jitter.threshold", 10));
    static String desc;
    static Thread thread;
    static volatile long time = Long.MAX_VALUE;

    public static void atStage(String desc) {
        JitterSampler.desc = desc;
        thread = Thread.currentThread();
        time = System.nanoTime();
    }

    public static String takeSnapshot() {
        return takeSnapshot(JITTER_THRESHOLD);
    }

    public static String takeSnapshot(long threshold) {
        long time = JitterSampler.time;
        long now = System.nanoTime();
        if (time > now - threshold)
            return null;
        Thread thread = JitterSampler.thread;
        String desc = JitterSampler.desc;
        if (thread == null || desc == null)
            return null;
        StackTraceElement[] stes = thread.getStackTrace();
        if (stes.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        sb.append(PROFILE_OF_THE_THREAD)
                .append(' ').append(thread.getName())
                .append(' ').append(desc)
                .append(" " + THREAD_HAS_BLOCKED_FOR + " ").append((now - time) / 1000_000)
                .append(" ms\n");
        for (StackTraceElement ste : stes) {
            sb.append("\tat ").append(ste).append('\n');
        }
        return sb.toString();
    }

    public static void finished() {
        thread = null;
        desc = null;
        time = Long.MAX_VALUE;
    }

    public static void sleepSilently(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
