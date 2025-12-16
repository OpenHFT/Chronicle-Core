/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractCloseableTest extends CoreTestCommon {

    @Test
    public void close() throws IllegalStateException {
        MyCloseable mc = new MyCloseable();
        assertFalse(mc.isClosed(), "close: L21");
        assertEquals(0, mc.performClose, "close: L22");

        mc.throwExceptionIfClosed();

        mc.close();
        assertTrue(mc.isClosed(), "close: L27");
        assertEquals(1, mc.performClose, "close: L28");

        mc.close();
        assertTrue(mc.isClosed(), "close: L31");
        assertEquals(1, mc.performClose, "close: L32");
    }

    @Test
    public void throwExceptionIfClosed() {
        MyCloseable mc = new MyCloseable();
        mc.close();
        assertThrows(IllegalStateException.class, mc::throwExceptionIfClosed, "throwExceptionIfClosed");

    }

    @Test
    public void warnAndCloseIfNotClosed() {
        Jvm.setResourceTracing(true);

        final Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MyCloseable mc = new MyCloseable();

        // not recorded for now.
        System.err.println("!!! The following warning is expected !!!");
        mc.warnAndCloseIfNotClosed();

        assertTrue(mc.isClosed(), "warnAndCloseIfNotClosed: L54");
        Jvm.resetExceptionHandlers();
        if (!AbstractCloseable.DISABLE_DISCARD_WARNING)
            assertEquals("Discarded without closing\n" +
                            "java.lang.IllegalStateException: net.openhft.chronicle.core.StackTrace: net.openhft.chronicle.core.io.AbstractCloseableTest$MyCloseable created here on main",
                    map.keySet().stream()
                            .map(e -> e.message() + "\n" + e.throwable())
                            .collect(Collectors.joining(", "))
                            .split(" at ")[0],
                    "warnAndCloseIfNotClosed: L57");
    }

    @Test
    public void assertCloseable() {

        final MyCloseable myCloseable = new MyCloseable() {
            int cnt = 0;

            @Override
            protected void assertCloseable() {
                if (cnt++ == 0)
                    throw new IllegalStateException("First close will always fail!");
            }
        };

        assertThrows(IllegalStateException.class, myCloseable::close);
        assertEquals(0, myCloseable.performClose, "assertCloseable: L79");

        myCloseable.close();
        assertEquals(1, myCloseable.performClose, "assertCloseable: L82");
    }

    static class MyCloseable extends AbstractCloseable {
        int performClose;

        @Override
        protected void performClose() {
            assertTrue(isClosing(), "performClose: L90");
            assertFalse(isClosed(), "performClose: L91");
            performClose++;
        }
    }
}
