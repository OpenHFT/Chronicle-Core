/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.time.TimeProvider;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TimerTest {

    private static final class FakeLoop implements EventLoop {
        final List<EventHandler> handlers = new ArrayList<>();
        private boolean closed;

        @Override
        public String name() {
            return "fake";
        }

        @Override
        public void addHandler(EventHandler handler) {
            handlers.add(handler);
        }

        @Override
        public void start() {
        }

        @Override
        public void unpause() {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isAlive() {
            return true;
        }

        @Override
        public boolean isStopped() {
            return false;
        }

        @Override
        public void close() {
            closed = true;
            handlers.clear();
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        void tickOnce() {
            for (Iterator<EventHandler> it = handlers.iterator(); it.hasNext();) {
                EventHandler h = it.next();
                try {
                    h.action();
                } catch (InvalidEventHandlerException e) {
                    it.remove();
                }
            }
        }
    }

    private static final class FakeTime implements TimeProvider {
        long now;

        @Override
        public long currentTimeMillis() {
            return now;
        }
    }

    @Test
    public void fixedRateFiresAfterInitialDelayAndPeriod() {
        FakeLoop loop = new FakeLoop();
        FakeTime time = new FakeTime();
        Timer timer = new Timer(loop, time);

        AtomicInteger calls = new AtomicInteger();
        VanillaEventHandler vh = () -> {
            calls.incrementAndGet();
            return false;
        };

        timer.scheduleAtFixedRate(vh, 10, 5);

        // t=0 no fire
        loop.tickOnce();
        assertEquals(0, calls.get());

        // advance to initialDelay
        time.now = 10;
        loop.tickOnce();
        assertEquals(1, calls.get());

        // not yet at next period
        time.now = 14;
        loop.tickOnce();
        assertEquals(1, calls.get());

        // at period boundary
        time.now = 15;
        loop.tickOnce();
        assertEquals(2, calls.get());
    }

    @Test
    public void scheduleOnceRemovesItselfAfterRun() {
        FakeLoop loop = new FakeLoop();
        FakeTime time = new FakeTime();
        CancellableTimer ct = new CancellableTimer(loop, time);

        AtomicInteger ran = new AtomicInteger();
        ct.schedule(ran::incrementAndGet, 5);

        // t=0 no run
        loop.tickOnce();
        assertEquals(0, ran.get());
        assertEquals(1, loop.handlers.size());

        // At delay boundary -> run once and remove
        time.now = 5;
        loop.tickOnce();
        assertEquals(1, ran.get());
        assertEquals(0, loop.handlers.size());
    }
}
