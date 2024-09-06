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
 */

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.InvalidMarshallableException;
import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.TimeProvider;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;

/**
 * A timer that can schedule tasks to be executed periodically or after a delay.
 * The timer operates using an event loop, allowing tasks to be performed in a non-blocking manner.
 * <p>
 * The {@code CancellableTimer} provides mechanisms to schedule tasks with fixed delays between executions,
 * as well as single execution after a specified delay. Tasks are scheduled on the provided {@link EventLoop}.
 * </p>
 */
public class CancellableTimer {

    @NotNull
    private final EventLoop eventLoop;
    @NotNull
    private final TimeProvider timeProvider;

    /**
     * Constructs a {@link CancellableTimer} with the given event loop and the default system time provider.
     *
     * @param eventLoop the event loop that the timer tasks will run on.
     */
    public CancellableTimer(@NotNull EventLoop eventLoop) {
        this(eventLoop, SystemTimeProvider.INSTANCE);
    }

    /**
     * Constructs a {@link CancellableTimer} with the given event loop and custom time provider.
     *
     * @param eventLoop    the event loop that the timer tasks will run on.
     * @param timeProvider the custom time provider to use for scheduling.
     */
    public CancellableTimer(@NotNull EventLoop eventLoop, @NotNull TimeProvider timeProvider) {
        this.eventLoop = eventLoop;
        this.timeProvider = timeProvider;
    }

    /**
     * Schedules a {@link VanillaEventHandler} to be executed periodically using the event loop thread.
     * The handler will be called at a fixed rate based on the specified initial delay and period.
     * The actual execution time is best-effort and may be delayed if the event loop is busy.
     *
     * @param eventHandler   the handler to be called periodically.
     * @param initialDelayMs the initial delay in milliseconds before the first execution.
     * @param periodMs       the period in milliseconds between subsequent executions.
     * @return a {@link Closeable} that, when closed, will cancel any remaining scheduled executions.
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
     * Schedules a {@link VanillaEventHandler} to be executed periodically using the event loop thread with a specified priority.
     * The handler will be called at a fixed rate based on the specified initial delay, period, and priority.
     * The actual execution time is best-effort and may be delayed if the event loop is busy.
     *
     * @param eventHandler   the handler to be called periodically.
     * @param initialDelayMs the initial delay in milliseconds before the first execution.
     * @param periodMs       the period in milliseconds between subsequent executions.
     * @param priority       the priority of the event handler.
     * @return a {@link Closeable} that, when closed, will cancel any remaining scheduled executions.
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
     * Schedules a {@link Runnable} handler to run once after a specified delay.
     *
     * @param eventHandler   the handler to be executed once after the delay.
     * @param initialDelayMs the delay in milliseconds before the handler is executed.
     * @return a {@link Closeable} that, when closed, will cancel the scheduled execution if it has not yet occurred.
     */
    public Closeable schedule(@NotNull Runnable eventHandler, long initialDelayMs) {
        final ScheduledEventHandler handler = new ScheduledEventHandler(timeProvider, () -> {
            eventHandler.run();
            throw new InvalidEventHandlerException("just runs once");
        }, initialDelayMs, 0);
        eventLoop.addHandler(handler);
        return handler;
    }

    /**
     * Internal class that represents a scheduled event handler. This class is used to wrap an event handler and
     * manage its execution based on the specified delay and period.
     */
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

        /**
         * Constructs a new {@link ScheduledEventHandler} with the specified time provider, event handler, initial delay, and period.
         * The handler priority is set to {@link HandlerPriority#TIMER}.
         *
         * @param timeProvider   the time provider used to get the current time.
         * @param eventHandler   the event handler to be executed.
         * @param initialDelayMs the initial delay in milliseconds before the first execution.
         * @param periodMs       the period in milliseconds between subsequent executions.
         */
        private ScheduledEventHandler(@NotNull TimeProvider timeProvider,
                                      @NotNull VanillaEventHandler eventHandler,
                                      long initialDelayMs,
                                      long periodMs) {
            this(timeProvider, eventHandler, initialDelayMs, periodMs, HandlerPriority.TIMER);
        }

        /**
         * Constructs a new {@link ScheduledEventHandler} with the specified time provider, event handler, initial delay, period, and priority.
         *
         * @param timeProvider   the time provider used to get the current time.
         * @param eventHandler   the event handler to be executed.
         * @param initialDelayMs the initial delay in milliseconds before the first execution.
         * @param periodMs       the period in milliseconds between subsequent executions.
         * @param priority       the priority of the event handler.
         */
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

        /**
         * Calculates the time to wait before the next execution based on whether this is the first execution or a subsequent one.
         *
         * @return the wait time in milliseconds.
         */
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

