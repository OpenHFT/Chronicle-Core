/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ThreadDumpTest {

    private ThreadDump threadDump;

    @BeforeEach
    void setUp() {
        threadDump = new ThreadDump();
    }

    @DisplayName("Ignored thread names are excluded from dump checks")
    @Test
    void testIgnoreThread() {
        String ignoredThreadName = "IgnoredThread";
        threadDump.ignore(ignoredThreadName);

        // Simulate an ignored thread
        Thread ignoredThread = new Thread(() -> {
        }, ignoredThreadName);
        ignoredThread.start();

        threadDump.assertNoNewThreads();

        // Clean up
        ignoredThread.interrupt();
    }

    @DisplayName("Thread dump passes when no new threads")
    @Test
    void testAssertNoNewThreads() {
        threadDump.assertNoNewThreads();
    }

    @DisplayName("Thread dump fails when new thread appears")
    @Test
    void testAssertNewThreads() {
        Assumptions.assumeFalse(Jvm.isArm());
        Thread newThread = new Thread(() -> {
            Jvm.pause(10000);
        });
        newThread.start();

        // ensure the thread has started
        Jvm.pause(100);

        // Expect an AssertionError since a new thread is running
        assertThrows(AssertionError.class, threadDump::assertNoNewThreads,
                "assertNoNewThreads should fail when newThread.isAlive()= " + newThread.isAlive());

        // Clean up
        newThread.interrupt();
    }
}
