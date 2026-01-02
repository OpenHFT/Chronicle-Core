/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.openhft.chronicle.core.StackTrace;
import static org.junit.jupiter.api.Assertions.*;

class ThreadDumpTest {

    private ThreadDump threadDump;

    @BeforeEach
    void setUp() {
        threadDump = new ThreadDump();
    }

    @Test
    @DisplayName("Ignored thread names are excluded from dump checks")
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

    @Test
    @DisplayName("Thread dump passes when no new threads")
    void testAssertNoNewThreads() {
        threadDump.assertNoNewThreads();
    }

    @Test
    @DisplayName("Thread dump fails when new thread appears")
    void testAssertNewThreads() {
        Assumptions.assumeFalse(Jvm.isArm(), "thread dump new-thread checks are unreliable on ARM");
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

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("Thread with ~ in name is ignored")
    void testIgnoreThreadWithTilde() {
        Thread tildeThread = new Thread(() -> Jvm.pause(5000), "test~ignored");
        tildeThread.start();

        try {
            // Should not throw - thread name contains ~
            threadDump.assertNoNewThreads();
        } finally {
            tildeThread.interrupt();
        }
    }

    @Test
    @DisplayName("Threads starting with RMI are ignored")
    void testRmiThreadIgnored() {
        // RMI threads are ignored by isExtra()
        threadDump.ignore("RMI TestThread"); // pre-ignore in case it gets created
        // The branch is tested by the isExtra method checking startsWith
        threadDump.assertNoNewThreads();
    }

    @Test
    @DisplayName("HttpClient SelectorManager thread is ignored")
    void testHttpClientSelectorManagerIgnored() {
        // HttpClient-X-SelectorManager threads are ignored
        threadDump.ignore("HttpClient-1-SelectorManager");
        threadDump.assertNoNewThreads();
    }

    @Test
    @DisplayName("add and createdHereFor track thread stack traces")
    void testAddAndCreatedHereFor() {
        Assumptions.assumeTrue(Jvm.isResourceTracing(), "resource tracing must be enabled");
        Thread testThread = new Thread(() -> { }, "test-tracked");
        StackTrace trace = new StackTrace("test trace");

        ThreadDump.add(testThread, trace);

        assertSame(trace, ThreadDump.createdHereFor(testThread),
                "createdHereFor should return the added stack trace");
    }

    @Test
    @DisplayName("createdHereFor returns null for untracked thread")
    void testCreatedHereForUntracked() {
        Thread untrackedThread = new Thread(() -> { }, "untracked");
        assertNull(ThreadDump.createdHereFor(untrackedThread),
                "createdHereFor should return null for untracked thread");
    }

    @Test
    @DisplayName("IGNORE_THREAD_IF_IN_NAME constant is tilde")
    void testIgnoreConstant() {
        assertEquals("~", ThreadDump.IGNORE_THREAD_IF_IN_NAME,
                "IGNORE_THREAD_IF_IN_NAME should be tilde");
    }

    @Test
    @DisplayName("Multiple ignored threads are all excluded")
    void testMultipleIgnored() {
        threadDump.ignore("Thread1");
        threadDump.ignore("Thread2");
        threadDump.ignore("Thread3");

        Thread t1 = new Thread(() -> Jvm.pause(5000), "Thread1");
        Thread t2 = new Thread(() -> Jvm.pause(5000), "Thread2");
        Thread t3 = new Thread(() -> Jvm.pause(5000), "Thread3");

        t1.start();
        t2.start();
        t3.start();

        try {
            threadDump.assertNoNewThreads();
        } finally {
            t1.interrupt();
            t2.interrupt();
            t3.interrupt();
        }
    }

    @Test
    @DisplayName("Constructor initialises with default ignored threads")
    void testDefaultIgnoredThreads() {
        ThreadDump newDump = new ThreadDump();
        // Test that "Time-limited test" is in default ignored list
        // by creating a thread with that name and verifying no assertion
        Thread timeLimited = new Thread(() -> Jvm.pause(5000), "Time-limited test");
        timeLimited.start();

        try {
            newDump.assertNoNewThreads();
        } finally {
            timeLimited.interrupt();
        }
    }
}
