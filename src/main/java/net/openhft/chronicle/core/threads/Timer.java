/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public class Timer {

    @NotNull
    private final EventLoop eventLoop;

    /**
     * @param eventLoop the event loop that the timer task is run on
     */
    public Timer(@NotNull EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    /**
     * uses the event loop thread to call the event handler periodically, the time that the event is
     * called back is best efforts, but if the thread is busy that call back maybe delayed
     *
     * @param eventHandler   the handler to be called back
     * @param initialDelayMs how long in milliseconds to wait before being called back
     * @param periodMs       the poll interval of being called
     */
    public void scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                    long initialDelayMs,
                                    long periodMs) {
        eventLoop.addHandler(new ScheduledEventHandler(eventHandler, initialDelayMs, periodMs, HandlerPriority.TIMER));
    }

    public void scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                    long initialDelayMs,
                                    long periodMs,
                                    HandlerPriority priority ) {
        eventLoop.addHandler(new ScheduledEventHandler(eventHandler, initialDelayMs, periodMs, priority));
    }

    public void schedule(@NotNull Runnable eventHandler, long initialDelayMs) {
        eventLoop.addHandler(new ScheduledEventHandler(() -> {
            eventHandler.run();
            throw new InvalidEventHandlerException("just runs once");
        }, initialDelayMs, 0));
    }

    private static final class ScheduledEventHandler implements EventHandler, Closeable {

        @NotNull
        private final VanillaEventHandler eventHandler;
        private final long initialDelayMs;
        private final long periodMs;
        private volatile boolean closed;

        private boolean isFirstTime = true;
        private long lastTimeRan = System.currentTimeMillis();

        private final HandlerPriority priority;

        private ScheduledEventHandler(@NotNull VanillaEventHandler eventHandler,
                                      long initialDelayMs,
                                      long periodMs) {
            this(eventHandler, initialDelayMs, periodMs, HandlerPriority.TIMER);
        }

        private ScheduledEventHandler(@NotNull VanillaEventHandler eventHandler,
                                      long initialDelayMs,
                                      long periodMs,
                                      HandlerPriority priority) {
            this.initialDelayMs = initialDelayMs;
            this.periodMs = periodMs;
            this.eventHandler = eventHandler;
            this.priority = priority;
        }

        @Override
        public boolean action() throws InvalidEventHandlerException {
            if (closed)
                throw InvalidEventHandlerException.reusable();

            long currentTime = System.currentTimeMillis();

            if (lastTimeRan + waitTimeMs() > currentTime)
                return false;

            isFirstTime = false;
            lastTimeRan = currentTime;

            try {
                return eventHandler.action();
            } catch (RuntimeException e) {
                Jvm.warn().on(getClass(), "Unexpected runtime exception", e);
            }

            return false;
        }

        private long waitTimeMs() {
            if (!isFirstTime)
                return periodMs;

            return initialDelayMs;
        }

        @Override
        @NotNull
        public HandlerPriority priority() {
            return priority;
        }

        @Override
        public void close() {
            this.closed = true;
        }

        @Override
        public String toString() {
            return "ScheduledEventHandler<" + eventHandler + '>';
        }
    }
}

