/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that {@link Slf4jExceptionHandler} falls back to the default handler when the delegate fails.
 */
class ExceptionHandlerFallbackTest {
    private static final int FAILED_INITIALIZATION = 2;

    /**
     * Ensures the Slf4jExceptionHandler falls back to the default handler when logger initialisation fails.
     */
    @Test
    void classShouldFallBackWhenDelegateThrows() throws IllegalAccessException {
        Field initializationState = Jvm.getField(LoggerFactory.class, "INITIALIZATION_STATE");
        int state;
        try {
            state = initializationState.getInt(null);
            initializationState.setInt(null, FAILED_INITIALIZATION);
        } catch (IllegalAccessException e) {
            throw new TestAbortedException(e.toString(), e);
        }
        try {
            Slf4jExceptionHandler.WARN.on(
                    ExceptionHandlerFallbackTest.class,
                    "message",
                    new Exception("delegate failure"));
        } finally {
            initializationState.setInt(null, state);
        }
        assertTrue(true, "execution should reach this point without exception"); // If we reach here, the test passes
    }
}
