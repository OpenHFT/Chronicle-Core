/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class StackTraceTest extends CoreTestCommon {
    private static final CountDownLatch threadStarted = new CountDownLatch(1);
    private static final String TIMESTAMP_REGEX = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z$";

    /**
     * Simulates a thread that sleeps/stalls so we can capture its stack trace.
     */
    private static void thinking() {
        threadStarted.countDown();
        Jvm.pause(5_000);
    }

    @Test
    public void testDefaultConstructor() {
        StackTrace st = new StackTrace(true);
        String currentThreadName = Thread.currentThread().getName();
        String regex = "stack trace on " + currentThreadName + " at " + TIMESTAMP_REGEX;
        assertTrue(
                st.getMessage() + " expected to match " + regex,
                st.getMessage().matches(regex)
        );
    }

    @Test
    public void testConstructorWithMessage() {
        String message = "test message";
        StackTrace st = new StackTrace(message, true);

        assertTrue(
                String.format("%s must match regular expression expecting '%s on %s at' with following timestamp", st.getMessage(), message, Thread.currentThread().getName()),
                st.getMessage().matches(message + " on " + Thread.currentThread().getName() + " at " + TIMESTAMP_REGEX)
        );
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "test message";
        Throwable cause = new RuntimeException("cause");
        StackTrace st = new StackTrace(message, cause, true);

        assertTrue(
                String.format("%s must match regular expression expecting '%s on %s at' with following timestamp", st.getMessage(), message, Thread.currentThread().getName()),
                st.getMessage().matches(message + " on " + Thread.currentThread().getName() + " at " + TIMESTAMP_REGEX)
        );
        assertEquals("Cause should match the supplied runtime exception", cause, st.getCause());
    }

    @Test
    public void testForThread_NullThread() {
        assertNull("forThread(null) should return null", StackTrace.forThread(null));
    }

    @Test
    public void testForThread() {
        Thread thread = new Thread();
        StackTrace st = StackTrace.forThread(thread);
        assertNotNull(st);
        // Only check the prefix because the thread name is appended with the time
        assertEquals(thread.toString(), st.getMessage().split(" on ")[0]);
    }

    /**
     * Verifies capturing a live thread's stack trace.
     */
    @Test
    public void forThread() throws InterruptedException {
        // Ensure Jvm class is loaded before spawning threads
        Jvm.init();

        // Start a background thread that stalls
        Thread t = new Thread(StackTraceTest::thinking, "background");
        t.start();
        boolean started = threadStarted.await(1, TimeUnit.SECONDS);
        assertTrue(started);

        // Give it time to enter the Jvm.pause
        Jvm.pause(50);

        // Capture the background thread's stack
        StackTrace st = StackTrace.forThread(t);

        // Interrupt the thread so it can stop
        t.interrupt();

        if (Jvm.isJava20Plus()) {
            // The exact string might differ in Java 20+ if the thread is displayed differently
            assertTrue(String.format("%s must match regular expression expecting timestamp to nanosecond precision", st.getMessage()),
                    st.getMessage().matches("Thread\\[\\#\\d+,background,5,main\\] on main at " + TIMESTAMP_REGEX));
            assertTopFramesContainPauseOrSleep(st);
        } else {
            assertTrue(st.getMessage() + " must match regular expression expecting timestamp to nanosecond precision",
                    st.getMessage().matches("Thread\\[background,5,main\\] on main at " + TIMESTAMP_REGEX));
            assertTopFramesContainPauseOrSleep(st);
        }
    }

    @Test
    public void testTimeIsUTC() {
        // Confirm the appended timestamp is in UTC
        StackTrace st = new StackTrace(true);
        String msg = st.getMessage(); // e.g. "... at 2030-12-31T23:59:59.999999999Z"
        assertTrue("Timestamp should end with 'Z' for UTC", msg.endsWith("Z"));
    }

    private static void assertTopFramesContainPauseOrSleep(StackTrace st) {
        String f0 = st.getStackTrace()[0].toString().split("\\(")[0].replaceAll("^app//", "").replaceFirst("^[^/]+/", "");
        String f1 = st.getStackTrace().length > 1 ? st.getStackTrace()[1].toString().split("\\(")[0].replaceAll("^app//", "").replaceFirst("^[^/]+/", "") : "";
        boolean ok =
                "net.openhft.chronicle.core.Jvm.pause".equals(f0) ||
                        "net.openhft.chronicle.core.Jvm.pause".equals(f1) ||
                        "java.lang.Thread.sleep".equals(f0) ||
                        "java.lang.Thread.sleep".equals(f1);
        assertTrue("Expected top frames to include Jvm.pause or Thread.sleep but were: " + Arrays.asList(f0, f1), ok);
    }
}
