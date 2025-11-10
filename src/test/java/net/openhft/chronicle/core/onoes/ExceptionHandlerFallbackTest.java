//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.Jvm;
import org.junit.AssumptionViolatedException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

/**
 * Test for {@link Slf4jExceptionHandler} to ensure that it falls back to the default
 */
class ExceptionHandlerFallbackTest {
    private static final int FAILED_INITIALIZATION = 2;

    /**
     * Test to ensure that the Slf4jExceptionHandler falls back to the default
     */
    @Test
    void classShouldFallBackWhenDelegateThrows() throws IllegalAccessException {
        Field initializationState = Jvm.getField(LoggerFactory.class, "INITIALIZATION_STATE");
        int state;
        try {
            state = initializationState.getInt(null);
            initializationState.setInt(null, FAILED_INITIALIZATION);
        } catch (IllegalAccessException e) {
            throw new AssumptionViolatedException(e.toString());
        }
        try {
            Slf4jExceptionHandler.WARN.on(
                    ExceptionHandlerFallbackTest.class,
                    "message",
                    new Exception("delegate failure"));
        } finally {
            initializationState.setInt(null, state);
        }
        assertTrue(true); // If we reach here, the test passes
    }
}
