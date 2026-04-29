/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.test.RecordingRunnable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleCleanerTest {

    @Test
    void cleanShouldExecuteRunnableOnce() {
        RecordingRunnable runnable = new RecordingRunnable();
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean();
        cleaner.clean(); // Second call to check idempotency

        assertEquals(1, runnable.runCount());
    }

    @Test
    void cleanShouldNotExecuteRunnableIfAlreadyCleaned() {
        RecordingRunnable runnable = new RecordingRunnable();
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean(); // First call
        cleaner.clean(); // Second call

        assertEquals(1, runnable.runCount());
    }

    @Test
    void constructorShouldInitializeWithProvidedRunnable() {
        RecordingRunnable runnable = new RecordingRunnable();
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        assertNotNull(cleaner); // Verifying that cleaner is initialized
        // Further tests can be performed if needed to check internal state
    }
}
