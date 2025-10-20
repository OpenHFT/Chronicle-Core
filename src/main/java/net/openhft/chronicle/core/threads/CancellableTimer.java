/*
 * Copyright 2016-2025 chronicle.software
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
 */


package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.TimeProvider;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * A timer that schedules tasks on an {@link EventLoop}. Execution is best-effort;
 * missed times do not accumulate a backlog.
 *
 * <p>Example usage:
 * <pre>{@code
 * try (Closeable c = timer.scheduleAtFixedRate(handler, 0L, 1000L)) {
 *     // work while the handler runs
 * }
 * }</pre>
 */
public class CancellableTimer {

    @NotNull
    private final EventLoop eventLoop;
    @NotNull
    private final TimeProvider timeProvider;

    /**
     * Constructs a CancellableTimer with the given event loop and system time provider.
     *
     * @param eventLoop The event loop that the timer tasks will run on.
     */
    public CancellableTimer(@NotNull EventLoop eventLoop) {
        this(eventLoop, SystemTimeProvider.INSTANCE);
    }

    /**
     * Constructs a CancellableTimer with the given event loop and custom time provider.
     *
     * @param eventLoop    The event loop that the timer tasks will run on.
     * @param timeProvider The custom time provider to use for scheduling.
     */
    public CancellableTimer(@NotNull EventLoop eventLoop, @NotNull TimeProvider timeProvider) {
        this.eventLoop = eventLoop;
        this.timeProvider = timeProvider;
    }

    /**
     * Schedules {@code eventHandler} for fixed-rate execution on the event loop.
     * Timing is best-effort; missed runs are not queued.
     *
     * @param eventHandler The handler to be invoked.
     * @param initialDelay first wait in milliseconds before the handler runs.
     * @param period       interval in milliseconds between executions.
     * @return A {@link Closeable} that when closed aborts future executions.
     */
    public Closeable scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                         long initialDelay,
                                         long period) {
        final ScheduledEventHandler handler =
                new ScheduledEventHandler(timeProvider, eventHandler, initialDelay, period, HandlerPriority.TIMER);
        eventLoop.addHandler(handler);
        return handler;
    }

    /**
     * Schedules {@code eventHandler} for fixed-rate execution on the event loop.
     * Timing is best-effort; missed runs are not queued.
     *
     * @param eventHandler The handler to be invoked.
     * @param initialDelay first wait in milliseconds before the handler runs.
     * @param period       interval in milliseconds between executions.
     * @param priority     The priority of the event handler.
     * @return A {@link Closeable} that when closed aborts future executions.
     */
    public Closeable scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                         long initialDelay,
                                         long period,
                                         HandlerPriority priority) {
        final ScheduledEventHandler handler =
                new ScheduledEventHandler(timeProvider, eventHandler, initialDelay, period, priority);
        eventLoop.addHandler(handler);
        return handler;
    }

    /**
     * Schedule {@code eventHandler} to run once after {@code delay} milliseconds.
     * After the task executes an {@link InvalidEventHandlerException} is thrown
     * internally to remove it from the event loop.
     *
     * @param eventHandler The handler to be invoked once.
     * @param delay        how long in milliseconds to wait before the handler runs.
     * @return A {@link Closeable} that when closed aborts future executions.
     */
    public Closeable schedule(@NotNull Runnable eventHandler, long delay) {
        final ScheduledEventHandler handler = new ScheduledEventHandler(timeProvider, () -> {
            eventHandler.run();
            throw new InvalidEventHandlerException("just runs once");
        }, delay, 0);
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
