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
        assertFalse(mc.isClosed(), "newly created closeable should not be closed");
        assertEquals(0, mc.performClose, "performClose should not have been called before first close");

        mc.throwExceptionIfClosed();

        mc.close();
        assertTrue(mc.isClosed(), "isClosed should return true after first close call");
        assertEquals(1, mc.performClose, "performClose should have been called exactly once after first close");

        mc.close();
        assertTrue(mc.isClosed(), "isClosed should remain true after second close call");
        assertEquals(1, mc.performClose, "performClose should not be called again on second close");
    }

    @Test
    public void throwExceptionIfClosed() {
        MyCloseable mc = new MyCloseable();
        mc.close();
        assertThrows(IllegalStateException.class, mc::throwExceptionIfClosed, "throwExceptionIfClosed should throw IllegalStateException when resource is closed");

    }

    @Test
    public void warnAndCloseIfNotClosed() {
        Jvm.setResourceTracing(true);

        final Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MyCloseable mc = new MyCloseable();

        // not recorded for now.
        System.err.println("!!! The following warning is expected !!!");
        mc.warnAndCloseIfNotClosed();

        assertTrue(mc.isClosed(), "resource should be closed after warnAndCloseIfNotClosed");
        Jvm.resetExceptionHandlers();
        if (!AbstractCloseable.DISABLE_DISCARD_WARNING)
            assertEquals("Discarded without closing\n" +
                            "java.lang.IllegalStateException: net.openhft.chronicle.core.StackTrace: net.openhft.chronicle.core.io.AbstractCloseableTest$MyCloseable created here on main",
                    map.keySet().stream()
                            .map(e -> e.message() + "\n" + e.throwable())
                            .collect(Collectors.joining(", "))
                            .split(" at ")[0],
                    "warning message should indicate resource was discarded without closing");
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
        assertEquals(0, myCloseable.performClose, "performClose should not be called when assertCloseable fails");

        myCloseable.close();
        assertEquals(1, myCloseable.performClose, "performClose should be called once after assertCloseable passes");
    }

    static class MyCloseable extends AbstractCloseable {
        int performClose;

        @Override
        protected void performClose() {
            assertTrue(isClosing(), "isClosing should return true during performClose execution");
            assertFalse(isClosed(), "isClosed should return false until performClose completes");
            performClose++;
        }
    }
}
