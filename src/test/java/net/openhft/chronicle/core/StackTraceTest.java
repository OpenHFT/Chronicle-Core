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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.time.SetTimeProvider;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class StackTraceTest extends CoreTestCommon {
    private static final CountDownLatch threadStarted = new CountDownLatch(1);

    /**
     * Simulates a thread that sleeps/stalls so we can capture its stack trace.
     */
    static void thinking() {
        threadStarted.countDown();
        Jvm.pause(5_000);
    }

    // Mock clock for deterministic timestamp testing, adjust as needed
    @Before
    public void setUp() {
        // Mock the current time so the constructor's message is deterministic
        SystemTimeProvider.CLOCK = new SetTimeProvider("2024-01-02T03:04:05.006007008");
    }

    @After
    public void tearDown() {
        // Restore the original system time provider
        SystemTimeProvider.CLOCK = SystemTimeProvider.INSTANCE;
    }

    @Test
    public void testDefaultConstructor() {
        StackTrace st = new StackTrace();
        String currentThreadName = Thread.currentThread().getName();
        assertEquals(
                "stack trace on " + currentThreadName
                        + " at 2024-01-02T03:04:05.006007008Z",
                st.getMessage()
        );
    }

    @Test
    public void testConstructorWithMessage() {
        String message = "test message";
        StackTrace st = new StackTrace(message);
        assertEquals(
                message + " on " + Thread.currentThread().getName()
                        + " at 2024-01-02T03:04:05.006007008Z",
                st.getMessage()
        );
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "test message";
        Throwable cause = new RuntimeException("cause");
        StackTrace st = new StackTrace(message, cause);

        assertEquals(
                message + " on " + Thread.currentThread().getName()
                        + " at 2024-01-02T03:04:05.006007008Z",
                st.getMessage()
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
            assertTrue(st.getMessage().endsWith("background,5,main] on main at 2024-01-02T03:04:05.006007008Z"));
            assertEquals(
                    "net.openhft.chronicle.core.Jvm.pause",
                    st.getStackTrace()[1].toString().split("\\(")[0].replaceAll("^app//", "")
            );
        } else {
            assertEquals(
                    "Thread[background,5,main] on main at 2024-01-02T03:04:05.006007008Z",
                    st.getMessage()
            );
            // The top of the captured stack trace should be our Jvm.pause call
            assertEquals(
                    "net.openhft.chronicle.core.Jvm.pause",
                    st.getStackTrace()[0].toString().split("\\(")[0].replaceAll("^app//", "")
            );
        }
    }

    @Test
    public void testTimeIsUTC() {
        // Confirm the appended timestamp is in UTC
        StackTrace st = new StackTrace();
        String msg = st.getMessage(); // e.g. "... at 2030-12-31T23:59:59.999999999Z"
        assertTrue("Timestamp should end with 'Z' for UTC", msg.endsWith("Z"));
    }
}
