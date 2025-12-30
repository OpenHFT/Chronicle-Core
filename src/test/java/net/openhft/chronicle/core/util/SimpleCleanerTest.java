/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SimpleCleanerTest {

    @DisplayName("Clean should execute runnable once simple")
    @Test
    void cleanShouldExecuteRunnableOnce() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean();
        cleaner.clean(); // Second call to check idempotency

        verify(runnable, times(1)).run();
    }

    @DisplayName("Clean should not execute runnable if already cleaned")
    @Test
    void cleanShouldNotExecuteRunnableIfAlreadyCleaned() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        cleaner.clean(); // First call
        cleaner.clean(); // Second call

        verify(runnable, times(1)).run();
    }

    @DisplayName("Constructor should initialize with provided runnable")
    @Test
    void constructorShouldInitializeWithProvidedRunnable() {
        Runnable runnable = mock(Runnable.class);
        SimpleCleaner cleaner = new SimpleCleaner(runnable);

        assertNotNull(cleaner, "required object should not be null"); // Verifying that cleaner is initialized
        // Further tests can be performed if needed to check internal state
    }
}
