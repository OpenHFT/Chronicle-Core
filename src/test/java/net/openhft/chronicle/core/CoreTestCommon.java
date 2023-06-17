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

import net.openhft.chronicle.core.internal.JvmExceptionTracker;
import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import net.openhft.chronicle.core.threads.CleaningThread;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.testframework.internal.ExceptionTracker;
import org.junit.After;
import org.junit.Before;

import static net.openhft.chronicle.core.io.AbstractCloseable.waitForCloseablesToClose;
import static net.openhft.chronicle.core.io.AbstractReferenceCounted.assertReferencesReleased;

/**
 * This class provides common functionality and setup for core tests.
 * It includes methods for enabling reference tracing, capturing thread dumps,
 * creating exception trackers, and performing cleanup after tests.
 */
public class CoreTestCommon {
    protected ThreadDump threadDump;
    private ExceptionTracker<?> exceptionTracker;

    /**
     * Enables reference tracing for AbstractReferenceCounted objects.
     * This should be called before running any tests that involve reference counting.
     */
    @Before
    public void enableReferenceTracing() {
        AbstractReferenceCounted.enableReferenceTracing();
    }

    /**
     * Captures a thread dump for diagnostic purposes.
     * Call this method in a subclass before running tests that involve thread management.
     */
    // Add @Before in subsclass where appropriate.
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    /**
     * Checks the captured thread dump to ensure that no new threads were created during the test.
     * This method should be called after completing tests that involve thread management.
     */
    public void checkThreadDump() {
        if (threadDump != null)
            threadDump.assertNoNewThreads();
        threadDump = null;
    }

    /**
     * Creates an exception tracker to track and assert expected exceptions during tests.
     * Call this method in a subclass before running tests that involve exception handling.
     */
    @Before
    public void createExceptionTracker() {
        exceptionTracker = JvmExceptionTracker.create();
    }

    /**
     * Specifies an expected exception message to be tracked by the exception tracker.
     * Use this method in tests to assert that a specific exception is thrown with the given message.
     * @param message The expected exception message.
     */
    public void expectException(String message) {
        exceptionTracker.expectException(message);
    }

    /**
     * Specifies an exception message to be ignored by the exception tracker.
     * Use this method in tests to ignore certain exceptions that may be thrown during testing.
     * @param message The exception message to ignore.
     */
    public void ignoreException(String message) {
        exceptionTracker.ignoreException(message);
    }

    /**
     * Performs necessary cleanup after tests.
     * This method should be annotated with the @After annotation in the subclass.
     * It performs cleanup tasks such as resource disposal, garbage collection,
     * reference release assertion, thread dump checks, and exception tracking checks.
     */
    @After
    public void afterChecks() {
        CleaningThread.performCleanup(Thread.currentThread());

        // find any discarded resources.
        System.gc();
        waitForCloseablesToClose(100);

        assertReferencesReleased();
        checkThreadDump();
        exceptionTracker.checkExceptions();
        AbstractReferenceCounted.disableReferenceTracing();
    }
}
