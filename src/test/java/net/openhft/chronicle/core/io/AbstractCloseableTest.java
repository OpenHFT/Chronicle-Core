/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        Jvm.setResourceTracing(true);

        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        MyCloseable mc = new MyCloseable();

        // not recorded for now.
        System.err.println("!!! The following warning is expected !!!");
        mc.warnAndCloseIfNotClosed();

        assertTrue(mc.isClosed());
        Jvm.resetExceptionHandlers();
        if (!AbstractCloseable.DISABLE_DISCARD_WARNING)
            assertEquals("Discarded without closing\n" +
                            "java.lang.IllegalStateException: net.openhft.chronicle.core.StackTrace: net.openhft.chronicle.core.io.AbstractCloseableTest$MyCloseable created here on main",
                    map.keySet().stream()
                            .map(e -> e.message() + "\n" + e.throwable())
                            .collect(Collectors.joining(", ")));
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

        try {
            myCloseable.close();
            fail();
        } catch (IllegalStateException expected) {
            // do Nothing
        }
        assertEquals(0, myCloseable.performClose);

        myCloseable.close();
        assertEquals(1, myCloseable.performClose);
    }

    static class MyCloseable extends AbstractCloseable {
        int performClose;

        @Override
        protected void performClose() {
            assertTrue(isClosing());
            assertFalse(isClosed());
            performClose++;
        }
    }
}