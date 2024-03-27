/*
 * Copyright 2016-2020 chronicle.software
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
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;

import java.util.concurrent.TimeUnit;

/**
 * A utility class for profiling and tracking the execution stages of threads.
 * <p>
 * This class can be used to take snapshots of a thread's stack trace at different stages
 * and measure how long the thread has been blocked.
 * 
 */
@Deprecated(/* to be moved in x.26 */)
public final class JitterSampler {
    private JitterSampler() {
    }

    public static final String PROFILE_OF_THE_THREAD = "profile of the thread";
    public static final String THREAD_HAS_BLOCKED_FOR = "thread has blocked for";

    static final long JITTER_THRESHOLD =
            TimeUnit.MILLISECONDS.toNanos(
                    Jvm.getLong("chronicle.jitter.threshold", 10L));
    static volatile String desc;
    static volatile Thread thread;
    static volatile long time = Long.MAX_VALUE;

    /**
     * Marks the current stage of the thread for profiling.
     *
     * @param desc a description of the current stage
     */
    public static void atStage(String desc) {
        Jvm.startup().on(JitterSampler.class, "atStage " + desc);
        JitterSampler.desc = desc;
        thread = Thread.currentThread();
        time = System.nanoTime();
    }

    /**
     * Takes a snapshot of the current thread state if the thread has been
     * blocked longer than the specified threshold.
     *
     * @return a String representation of the thread's stack trace and the time blocked,
     * or null if the thread has been blocked less than the threshold.
     */
    public static String takeSnapshot() {
        return takeSnapshot(JITTER_THRESHOLD);
    }

    /**
     * Takes a snapshot of the current thread state if the thread has been
     * blocked longer than the specified threshold.
     *
     * @param threshold the time threshold in nanoseconds
     * @return a String representation of the thread's stack trace and the time blocked,
     * or null if the thread has been blocked less than the threshold.
     */
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

    /**
     * Marks the current thread as finished for profiling.
     */
    public static void finished() {
        Jvm.startup().on(JitterSampler.class, "finished");
        thread = null;
        desc = "finished";
        time = Long.MAX_VALUE;
    }

}
