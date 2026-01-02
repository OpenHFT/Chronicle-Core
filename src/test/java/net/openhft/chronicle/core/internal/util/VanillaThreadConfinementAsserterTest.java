/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ThreadConfinementAsserter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VanillaThreadConfinementAsserterTest extends CoreTestCommon {

    private ThreadConfinementAsserter asserter;

    @BeforeEach
    public void before() {
        asserter = new VanillaThreadConfinementAsserter();
    }

    @Test
    @DisplayName("Assert thread confined same vanilla confinement")
    void assertThreadConfinedSame() {
        asserter.assertThreadConfined();
    }

    @Test
    @DisplayName("Assert thread confined other vanilla confinement")
    void assertThreadConfinedOther() throws InterruptedException {
        final Thread other = new Thread(asserter::assertThreadConfined, "first");
        other.start();
        other.join();

        // The asserter is now touched by another thread
        assertThrows(IllegalStateException.class, asserter::assertThreadConfined,
                "assertThreadConfined should throw after another thread accessed the asserter");
    }

    @Test
    @DisplayName("Should not throw exception for same thread access")
    void shouldNotThrowExceptionForSameThreadAccess() {
        VanillaThreadConfinementAsserter asserter = new VanillaThreadConfinementAsserter();
        assertDoesNotThrow(asserter::assertThreadConfined, "Access by the same thread should not throw an exception");
    }

    @Test
    @DisplayName("Should throw exception for different thread access")
    void shouldThrowExceptionForDifferentThreadAccess() throws InterruptedException {
        VanillaThreadConfinementAsserter asserter = new VanillaThreadConfinementAsserter();
        asserter.assertThreadConfined(); // Initialize with the current thread

        Thread otherThread = new Thread(() -> {
            assertThrows(IllegalStateException.class, asserter::assertThreadConfined,
                    "assertThreadConfined should throw when called from a different thread");
        });

        otherThread.start();
        otherThread.join();
    }

    @Test
    @DisplayName("To string should return non null value vanilla thread confinement")
    void toStringShouldReturnNonNullValue() {
        VanillaThreadConfinementAsserter asserter = new VanillaThreadConfinementAsserter();
        assertNotNull(asserter.toString(), "toString should return a non-null diagnostic string");
    }
}
