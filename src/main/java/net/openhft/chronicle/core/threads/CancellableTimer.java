/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.TimeProvider;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

public class CancellableTimer {

    @NotNull
    private final EventLoop eventLoop;
    @NotNull
    private final TimeProvider timeProvider;

    /**
     * @param eventLoop the event loop that the timer task is run on
     */
    public CancellableTimer(@NotNull EventLoop eventLoop) {
        this(eventLoop, SystemTimeProvider.INSTANCE);
    }

    public CancellableTimer(@NotNull EventLoop eventLoop, @NotNull TimeProvider timeProvider) {
        this.eventLoop = eventLoop;
        this.timeProvider = timeProvider;
    }

    /**
     * uses the event loop thread to call the event handler periodically, the time that the event is
     * called back is best-effort, but if the thread is busy that call back maybe delayed
     *
     * @param eventHandler   the handler to be called back
     * @param initialDelayMs how long in milliseconds to wait before being called back
     * @param periodMs       the poll interval of being called
     * @return a {@link Closeable} that when closed will abort any remaining scheduled calls
     */
    public Closeable scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                         long initialDelayMs,
                                         long periodMs) {
        final ScheduledEventHandler handler =
                new ScheduledEventHandler(timeProvider, eventHandler, initialDelayMs, periodMs, HandlerPriority.TIMER);
        eventLoop.addHandler(handler);
        return handler;
    }

    /**
     * uses the event loop thread to call the event handler periodically, the time that the event is
     * called back is best-effort, but if the thread is busy that call back maybe delayed
     *
     * @param eventHandler   the handler to be called back
     * @param initialDelayMs how long in milliseconds to wait before being called back
     * @param periodMs       the poll interval of being called
     * @param priority       the priority of the event handler
     * @return a {@link Closeable} that when closed will abort any remaining scheduled calls
     */
    public Closeable scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                         long initialDelayMs,
                                         long periodMs,
                                         HandlerPriority priority) {
        final ScheduledEventHandler handler =
                new ScheduledEventHandler(timeProvider, eventHandler, initialDelayMs, periodMs, priority);
        eventLoop.addHandler(handler);
        return handler;
    }

    /**
     * Schedule a handler to run once after a delay
     *
     * @param eventHandler   the handler to be called back
     * @param initialDelayMs how long in milliseconds to wait before being called back
     * @return a {@link Closeable} that when closed will abort any remaining scheduled calls
     */
    public Closeable schedule(@NotNull Runnable eventHandler, long initialDelayMs) {
        final ScheduledEventHandler handler = new ScheduledEventHandler(timeProvider, () -> {
            eventHandler.run();
            throw new InvalidEventHandlerException("just runs once");
        }, initialDelayMs, 0);
        eventLoop.addHandler(handler);
        return handler;
    }

    protected static final class ScheduledEventHandler implements EventHandler, Closeable {

        @NotNull
        private final TimeProvider timeProvider;
        @NotNull
        private final VanillaEventHandler eventHandler;
        private final long initialDelayMs;
        private final long periodMs;
        private volatile boolean closed;

        private boolean isFirstTime = true;
        private long lastTimeRan;

        private final HandlerPriority priority;

        private ScheduledEventHandler(@NotNull TimeProvider timeProvider,
                                      @NotNull VanillaEventHandler eventHandler,
                                      long initialDelayMs,
                                      long periodMs) {
            this(timeProvider, eventHandler, initialDelayMs, periodMs, HandlerPriority.TIMER);
        }

        private ScheduledEventHandler(@NotNull TimeProvider timeProvider,
                                      @NotNull VanillaEventHandler eventHandler,
                                      long initialDelayMs,
                                      long periodMs,
                                      HandlerPriority priority) {
            this.timeProvider = timeProvider;
            this.initialDelayMs = initialDelayMs;
            this.periodMs = periodMs;
            this.eventHandler = eventHandler;
            this.priority = priority;
            this.lastTimeRan = timeProvider.currentTimeMillis();
        }

        @Override
        public boolean action() throws InvalidEventHandlerException, InvalidMarshallableException {
            if (closed)
                throw InvalidEventHandlerException.reusable();

            long currentTime = timeProvider.currentTimeMillis();

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

