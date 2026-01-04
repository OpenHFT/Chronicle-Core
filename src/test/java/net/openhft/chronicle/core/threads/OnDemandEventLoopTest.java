/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OnDemandEventLoop} covering lazy initialisation, delegation, and lifecycle edge cases.
 */
class OnDemandEventLoopTest extends CoreTestCommon {

    @Test
    @DisplayName("On demand event loop lazily creates delegate")
    void onDemand() {
        OnDemandEventLoop el = new OnDemandEventLoop(() -> new StubEventLoop("dummy"));
        assertFalse(el.hasEventLoop(), "event loop should not exist before first access");
        assertEquals("dummy", el.name(), "accessing name should trigger event loop creation and return delegate name");
        assertTrue(el.hasEventLoop(), "event loop should exist after being accessed");
        el.close();
    }

    @Test
    @DisplayName("hasEventLoop returns false before any method triggers creation")
    void hasEventLoopReturnsFalseInitially() {
        OnDemandEventLoop loop = new OnDemandEventLoop(StubEventLoop::new);
        assertFalse(loop.hasEventLoop(), "event loop should not be created initially");
    }

    @Test
    @DisplayName("eventLoop supplier is called only once")
    void supplierCalledOnlyOnce() {
        AtomicInteger callCount = new AtomicInteger();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> {
            callCount.incrementAndGet();
            return new StubEventLoop();
        });

        loop.name();
        loop.name();
        loop.addHandler(() -> false);

        assertEquals(1, callCount.get(), "supplier should be invoked exactly once");
    }

    @Test
    @DisplayName("addHandler triggers event loop creation and delegates")
    void addHandlerDelegates() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        EventHandler handler = () -> false;
        loop.addHandler(handler);

        assertTrue(loop.hasEventLoop(), "addHandler should trigger event loop creation");
        assertEquals(1, stub.handlerCount, "handler should be added to underlying event loop");
    }

    @Test
    @DisplayName("start triggers event loop creation and delegates")
    void startDelegates() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start();

        assertTrue(loop.hasEventLoop(), "start should trigger event loop creation");
        assertTrue(stub.started, "start should delegate to underlying event loop");
    }

    @Test
    @DisplayName("unpause does nothing when event loop not created")
    void unpauseDoesNothingWithoutEventLoop() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.unpause();

        assertFalse(loop.hasEventLoop(), "unpause should not trigger event loop creation");
        assertFalse(stub.unpaused, "unpause should not delegate when event loop not created");
    }

    @Test
    @DisplayName("unpause delegates when event loop exists")
    void unpauseDelegatesWhenEventLoopExists() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop
        loop.unpause();

        assertTrue(stub.unpaused, "unpause should delegate when event loop exists");
    }

    @Test
    @DisplayName("stop does nothing when event loop not created")
    void stopDoesNothingWithoutEventLoop() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.stop();

        assertFalse(loop.hasEventLoop(), "stop should not trigger event loop creation");
        assertFalse(stub.stopped, "stop should not delegate when event loop not created");
    }

    @Test
    @DisplayName("stop delegates when event loop exists")
    void stopDelegatesWhenEventLoopExists() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop
        loop.stop();

        assertTrue(stub.stopped, "stop should delegate when event loop exists");
    }

    @Test
    @DisplayName("isClosed returns false when event loop exists and is open")
    void isClosedReturnsFalseWhenEventLoopExists() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop
        // When event loop exists, isClosed returns false (hasEventLoop is true, so !hasEventLoop is false)
        assertFalse(loop.isClosed(), "isClosed should return false when event loop exists");
    }

    @Test
    @DisplayName("isClosed triggers creation when event loop not created and delegates")
    void isClosedTriggersCreationAndDelegates() {
        StubEventLoop stub = new StubEventLoop();
        stub.closed = true;
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        // When not created, isClosed triggers creation (hasEventLoop false, then calls eventLoop())
        // Since stub.closed is true, it returns true
        assertTrue(loop.isClosed(), "isClosed should trigger creation and return underlying closed state");
        assertTrue(loop.hasEventLoop(), "isClosed should trigger event loop creation");
    }

    @Test
    @DisplayName("isClosed returns false when event loop not created and underlying not closed")
    void isClosedReturnsFalseWhenUnderlyingNotClosed() {
        StubEventLoop stub = new StubEventLoop();
        stub.closed = false;
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        // When not created and underlying is not closed
        assertFalse(loop.isClosed(), "isClosed should return false when underlying is not closed");
    }

    @Test
    @DisplayName("OnDemandEventLoop isAlive returns false before event loop creation")
    void isAliveReturnsFalseWithoutEventLoop() {
        OnDemandEventLoop loop = new OnDemandEventLoop(StubEventLoop::new);
        assertFalse(loop.isAlive(), "isAlive should return false when event loop not created");
    }

    @Test
    @DisplayName("isAlive returns true when event loop exists and is alive")
    void isAliveReturnsTrueWhenAlive() {
        StubEventLoop stub = new StubEventLoop();
        stub.alive = true;
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop

        assertTrue(loop.isAlive(), "isAlive should return true when underlying is alive");
    }

    @Test
    @DisplayName("isAlive returns false when event loop exists but not alive")
    void isAliveReturnsFalseWhenNotAlive() {
        StubEventLoop stub = new StubEventLoop();
        stub.alive = false;
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop

        assertFalse(loop.isAlive(), "isAlive should return false when underlying is not alive");
    }

    @Test
    @DisplayName("isStopped returns false when event loop is not created yet")
    void isStoppedReturnsFalseWithoutEventLoop() {
        OnDemandEventLoop loop = new OnDemandEventLoop(StubEventLoop::new);
        assertFalse(loop.isStopped(), "isStopped should return false when event loop not created");
    }

    @Test
    @DisplayName("isStopped returns true when event loop exists and is stopped")
    void isStoppedReturnsTrueWhenStopped() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop
        stub.stoppedState = true;

        assertTrue(loop.isStopped(), "isStopped should return true when underlying is stopped");
    }

    @Test
    @DisplayName("isStopped returns false when event loop exists and not stopped")
    void isStoppedReturnsFalseWhenNotStopped() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop
        stub.stoppedState = false;

        assertFalse(loop.isStopped(), "isStopped should return false when underlying is not stopped");
    }

    @Test
    @DisplayName("close does nothing when event loop not created")
    void closeDoesNothingWithoutEventLoop() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.close();

        assertFalse(loop.hasEventLoop(), "close should not trigger event loop creation");
        assertFalse(stub.closed, "close should not delegate when event loop not created");
    }

    @Test
    @DisplayName("close delegates when event loop exists")
    void closeDelegatesWhenEventLoopExists() {
        StubEventLoop stub = new StubEventLoop();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> stub);

        loop.start(); // create event loop
        loop.close();

        assertTrue(stub.closed, "close should delegate when event loop exists");
    }

    @Test
    @DisplayName("constructor rejects null supplier argument input")
    void constructorRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> new OnDemandEventLoop(null),
                "constructor should reject null supplier");
    }

    @Test
    @DisplayName("concurrent access to eventLoop is thread-safe")
    void concurrentAccessIsThreadSafe() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger();
        OnDemandEventLoop loop = new OnDemandEventLoop(() -> {
            callCount.incrementAndGet();
            return new StubEventLoop();
        });

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(loop::name);
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        assertEquals(1, callCount.get(), "supplier should be invoked exactly once under concurrent access");
    }

    /**
     * Stub implementation of EventLoop for testing delegation.
     */
    private static class StubEventLoop implements EventLoop {
        final String name;
        boolean started;
        boolean stopped;
        boolean stoppedState;
        boolean closed;
        boolean alive;
        boolean unpaused;
        int handlerCount;

        StubEventLoop() {
            this("stub");
        }

        StubEventLoop(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void addHandler(EventHandler handler) {
            handlerCount++;
        }

        @Override
        public void start() {
            started = true;
        }

        @Override
        public void unpause() {
            unpaused = true;
        }

        @Override
        public void stop() {
            stopped = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }

        @Override
        public boolean isStopped() {
            return stoppedState;
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
