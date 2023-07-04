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
import static org.junit.Assume.assumeTrue;

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
        assumeTrue(Jvm.isResourceTracing());

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

    @Test
    public void testThreadSafetyCheck() throws InterruptedException {
        MyCloseable mc = new MyCloseable();
        Thread newThread = new Thread(mc::close);
        newThread.start();
        newThread.join();
        assertThrows(IllegalStateException.class, mc::throwExceptionIfClosed);
    }

    @Test
    public void testDisableThreadSafetyCheck() {
        MyCloseable mc = new MyCloseable();
        mc.singleThreadedCheckDisabled(true);

        // Since thread safety check is disabled, no exception should be thrown.
        mc.throwExceptionIfClosed();

        // Make sure thread safety check is disabled
        assertTrue(mc.singleThreadedCheckDisabled());
        mc.close();
    }

    @Test
    public void testCloseableTracing() {
        MyCloseable.disableCloseableTracing();

        expectException("Discarded without closing");
        MyCloseable mc = new MyCloseable();
        assertNotNull(mc.createdHere());

        assertNull(mc.closedHere);

        MyCloseable.enableCloseableTracing();
        // doesn't detect the mc created while tracing was off
        AbstractCloseable.assertCloseablesClosed();
    }

    @Test(timeout = 2000)
    public void testWaitForCloseablesToClose() throws InterruptedException {
        MyCloseable mc = new MyCloseable();
        Thread closingThread = new Thread(mc::close);

        closingThread.start();
        closingThread.join();

        assertTrue(MyCloseable.waitForCloseablesToClose(1000));
    }

    @Test
    public void testIsClosing() {
        MyCloseable mc = new MyCloseable();
        assertFalse(mc.isClosing());

        // Initiate closing
        new Thread(mc::close).start();

        // Wait for closing to start
        while (!mc.isClosing()) {
            Thread.yield();
        }

        assertTrue(mc.isClosing());
    }

    @Test
    public void testCloseOnThreadInterrupt() throws InterruptedException {
        MyCloseable mc = new MyCloseable();

        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // Loop until interrupted
            }

            // Call close on interruption
            mc.close();
        });

        t.start();

        // Interrupt the thread
        t.interrupt();
        t.join();

        // Assert close() has been called
        assertTrue(mc.isClosed());
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowExceptionIfClosed() {
        MyCloseable mc = new MyCloseable();
        mc.close();
        mc.throwExceptionIfClosed();
    }

    @Test
    public void testConcurrentCloseCalls() throws InterruptedException {
        final MyCloseable mc = new MyCloseable();
        final int numThreads = 10;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(mc::close);
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }

        // performClose() should be called only once
        assertEquals(1, mc.performClose);
    }

    @Test
    public void testExceptionInPerformClose() {
        final MyCloseable myCloseable = new MyCloseable() {
            @Override
            protected void performClose() {
                super.performClose();
                throw new RuntimeException("Error in performClose");
            }
        };

        // expect the exception to be caught and logged, not thrown out
        expectException("Error in performClose");
        myCloseable.close();

        assertTrue(myCloseable.isClosed());
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