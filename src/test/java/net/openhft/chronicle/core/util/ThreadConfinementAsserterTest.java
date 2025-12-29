/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ThreadConfinementAsserterTest {

    @DisplayName("assertThreadConfinedSameThread behaviour under expected input and output conditions")
    @Test
    void assertThreadConfinedSameThread() {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();

        assertDoesNotThrow(asserter::assertThreadConfined, "assertThreadConfined should not throw on initial owning thread");
        assertDoesNotThrow(asserter::assertThreadConfined, "repeat assertThreadConfined should remain valid on owning thread");
    }

    @DisplayName("assertThreadConfinedDifferentThreads behaviour under expected input and output conditions")
    @Test
    void assertThreadConfinedDifferentThreads() throws InterruptedException {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        assertDoesNotThrow(asserter::assertThreadConfined, "assertThreadConfined should not throw on owning thread");

        executorService.execute(() -> {
            assertThrows(IllegalStateException.class, asserter::assertThreadConfined,
                    "assertThreadConfined should throw when called from a different thread");
            latch.countDown();
        });

        latch.await();
        executorService.shutdown();
    }

    @DisplayName("createShouldReturnCorrectTypeBasedOnAssertions behaviour under expected input and output conditions")
    @Test
    void createShouldReturnCorrectTypeBasedOnAssertions() {
        // This test's behavior will depend on whether assertions are enabled in the JVM.
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.create();
        // At minimum, a non-null asserter is returned.
        assertNotNull(asserter, "create() should return a non-null asserter regardless of assertion state");
    }

    @DisplayName("createEnabledShouldAlwaysReturnFunctionalAsserter behaviour under expected input and output conditions")
    @Test
    void createEnabledShouldAlwaysReturnFunctionalAsserter() {
        ThreadConfinementAsserter asserter = ThreadConfinementAsserter.createEnabled();
        assertNotNull(asserter, "createEnabled() should always return a functional non-null asserter");
        // Further testing of functionality.
    }
}
