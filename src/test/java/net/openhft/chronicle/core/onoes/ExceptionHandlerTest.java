/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.onoes;

import net.openhft.chronicle.core.util.IgnoresEverything;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerTest {

    @Test
    void ignoresEverythingReturnsNullHandler() {
        assertSame(NullExceptionHandler.NOTHING, ExceptionHandler.ignoresEverything());
        assertInstanceOf(IgnoresEverything.class, ExceptionHandler.ignoresEverything());
    }

    @Test
    void nullExceptionHandlerSwallowsClassMessageThrowable() {
        assertDoesNotThrow(() ->
                NullExceptionHandler.NOTHING.on(ExceptionHandlerTest.class, "ignored", new RuntimeException("ignored")));
    }
}
