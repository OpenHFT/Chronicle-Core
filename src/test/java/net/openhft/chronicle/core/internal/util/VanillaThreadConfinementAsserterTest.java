/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.util.ThreadConfinementAsserter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VanillaThreadConfinementAsserterTest extends CoreTestCommon {

    private ThreadConfinementAsserter asserter;

    @BeforeEach
    void before() {
        asserter = new VanillaThreadConfinementAsserter();
    }

    @Test
    void assertThreadConfinedSame() {
        asserter.assertThreadConfined();
    }

    @Test
    void assertThreadConfinedOther() throws InterruptedException {
        final Thread other = new Thread(() -> asserter.assertThreadConfined(), "first");
        other.start();
        other.join();

        // The asserter is now touched by another thread
        assertThrows(IllegalStateException.class, () ->
                asserter.assertThreadConfined()
        );
    }

    @Test
    void shouldNotThrowExceptionForSameThreadAccess() {
        VanillaThreadConfinementAsserter asserter = new VanillaThreadConfinementAsserter();
        assertDoesNotThrow(asserter::assertThreadConfined, "Access by the same thread should not throw an exception");
    }

    @Test
    void shouldThrowExceptionForDifferentThreadAccess() throws InterruptedException {
        VanillaThreadConfinementAsserter asserter = new VanillaThreadConfinementAsserter();
        asserter.assertThreadConfined(); // Initialize with the current thread

        Thread otherThread = new Thread(() ->
                assertThrows(IllegalStateException.class, asserter::assertThreadConfined));

        otherThread.start();
        otherThread.join();
    }

    @Test
    void toStringShouldReturnNonNullValue() {
        VanillaThreadConfinementAsserter asserter = new VanillaThreadConfinementAsserter();
        assertNotNull(asserter.toString(), "toString should return a non-null value");
    }
}
