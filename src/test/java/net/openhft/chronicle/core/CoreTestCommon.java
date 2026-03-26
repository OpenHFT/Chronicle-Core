/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import net.openhft.chronicle.core.internal.JvmExceptionTracker;
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;
import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import net.openhft.chronicle.core.threads.CleaningThread;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.testframework.exception.ExceptionTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static net.openhft.chronicle.core.io.AbstractCloseable.waitForCloseablesToClose;

public class CoreTestCommon {
    private ThreadDump threadDump;
    private ExceptionTracker<?> exceptionTracker;

    @BeforeEach
    void beforeEachCoreTestCommon() {
        enableReferenceTracing();
        createExceptionTracker();
    }

    public void enableReferenceTracing() {
        AbstractReferenceCounted.enableReferenceTracing();
    }

    // Add @Before in tests where this could be a problem. It's expensive to add to every test
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    public void createExceptionTracker() {
        exceptionTracker = JvmExceptionTracker.create();
    }

    protected void expectException(String message) {
        exceptionTracker.expectException(message);
    }

    protected void ignoreException(String message) {
        exceptionTracker.ignoreException(message);
    }

    @AfterEach
    protected void afterChecks() {
        CleaningThread.performCleanup(Thread.currentThread());

        waitForCloseablesToClose(10000);

        assertReferencesReleased();

        if (threadDump != null)
            checkThreadDump();

        exceptionTracker.checkExceptions();
        AbstractReferenceCounted.disableReferenceTracing();
    }

    protected void assertReferencesReleased() {
        ReferenceCountedUtils.assertReferencesReleased();
    }
}
