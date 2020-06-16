package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AbstractCloseableTest extends CoreTestCommon {

    @Test
    public void close() throws IllegalStateException {
        MyCloseable mc = new MyCloseable();
        assertFalse(mc.isClosed());
        assertEquals(0, mc.performClose);

        mc.throwExceptionIfClosed();

        mc.close();
        assertTrue(mc.isClosed());
        assertEquals(1, mc.performClose);

        mc.close();
        assertTrue(mc.isClosed());
        assertEquals(1, mc.performClose);
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionIfClosed() throws IllegalStateException {
        MyCloseable mc = new MyCloseable();
        mc.close();
        mc.throwExceptionIfClosed();
    }

    @Test
    public void warnAndCloseIfNotClosed() {
        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MyCloseable mc = new MyCloseable();
        // not recorded for now.
        mc.warnAndCloseIfNotClosed();
        assertTrue(mc.isClosed());
        Jvm.resetExceptionHandlers();
        assertEquals("Discarded without closing\n" +
                        "java.lang.IllegalStateException: net.openhft.chronicle.core.StackTrace: Created Here on main",
                map.keySet().stream()
                        .map(e -> e.message + "\n" + e.throwable)
                        .collect(Collectors.joining(", ")));
    }

    static class MyCloseable extends AbstractCloseable {
        int performClose;

        @Override
        protected void performClose() {
            performClose++;
        }
    }
}