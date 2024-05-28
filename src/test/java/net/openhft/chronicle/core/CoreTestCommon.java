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
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;
import net.openhft.chronicle.core.io.AbstractReferenceCounted;
import net.openhft.chronicle.core.threads.CleaningThread;
import net.openhft.chronicle.core.threads.ThreadDump;
import net.openhft.chronicle.testframework.exception.ExceptionTracker;
import org.junit.After;
import org.junit.Before;

import static net.openhft.chronicle.core.io.AbstractCloseable.waitForCloseablesToClose;

public class CoreTestCommon {
    protected ThreadDump threadDump;
    private ExceptionTracker<?> exceptionTracker;

    @Before
    public void enableReferenceTracing() {
        AbstractReferenceCounted.enableReferenceTracing();
    }

    // Add @Before in tests where this could be a problem. It's expensive to add to every test
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Before
    public void createExceptionTracker() {
        exceptionTracker = JvmExceptionTracker.create();
    }

    public void expectException(String message) {
        exceptionTracker.expectException(message);
    }

    public void ignoreException(String message) {
        exceptionTracker.ignoreException(message);
    }

    @After
    public void afterChecks() {
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
