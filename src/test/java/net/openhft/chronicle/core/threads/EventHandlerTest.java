/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

class EventHandlerTest {

    @DisplayName("Event loop should be called with correct event loop")
    @Test
    void eventLoopShouldBeCalledWithCorrectEventLoop() {
        RecordingEventHandler handler = new RecordingEventHandler();
        EventLoop eventLoop = new StubEventLoop();

        handler.eventLoop(eventLoop);

        Assertions.assertSame(eventLoop, handler.eventLoop(), "eventLoop should be recorded when invoked");
    }

    @DisplayName("Loop started should be called event")
    @Test
    void loopStartedShouldBeCalled() {
        RecordingEventHandler handler = new RecordingEventHandler();

        handler.loopStarted();

        Assertions.assertTrue(handler.wasLoopStarted(), "loopStarted flag should be set after invocation");
    }

    @DisplayName("Loop finished should be called event")
    @Test
    void loopFinishedShouldBeCalled() {
        RecordingEventHandler handler = new RecordingEventHandler();

        handler.loopFinished();

        Assertions.assertTrue(handler.wasLoopFinished(), "loopFinished flag should be set after invocation");
    }

    @DisplayName("Priority should return medium by default event")
    @Test
    void priorityShouldReturnMediumByDefault() {
        EventHandler handler = () -> false;

        Assertions.assertEquals(HandlerPriority.MEDIUM, handler.priority(), "default priority should be MEDIUM when not explicitly set");
    }

    @DisplayName("Close should be called if event handler is closeable")
    @Test
    void closeShouldBeCalledIfEventHandlerIsCloseable() throws IOException {
        RecordingEventHandler handler = new RecordingEventHandler();

        handler.loopFinished();
        handler.close();

        Assertions.assertTrue(handler.wasClosed(), "close should be invoked for closeable event handlers");
    }

    private static final class RecordingEventHandler implements EventHandler, Closeable {
        private EventLoop eventLoop;
        private boolean loopStarted;
        private boolean loopFinished;
        private boolean closed;

        @Override
        public boolean action() {
            return false;
        }

        @Override
        public void eventLoop(EventLoop eventLoop) {
            this.eventLoop = eventLoop;
            EventHandler.super.eventLoop(eventLoop);
        }

        @Override
        public void loopStarted() {
            loopStarted = true;
            EventHandler.super.loopStarted();
        }

        @Override
        public void loopFinished() {
            loopFinished = true;
            EventHandler.super.loopFinished();
        }

        @Override
        public void close() {
            closed = true;
        }

        EventLoop eventLoop() {
            return eventLoop;
        }

        boolean wasLoopStarted() {
            return loopStarted;
        }

        boolean wasLoopFinished() {
            return loopFinished;
        }

        boolean wasClosed() {
            return closed;
        }
    }

    private static final class StubEventLoop implements EventLoop {
        @Override
        public String name() {
            return "stub";
        }

        @Override
        public void addHandler(EventHandler handler) {
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
            return false;
        }

        @Override
        public boolean isStopped() {
            return true;
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }
}
