//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.TimeProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Timer schedules tasks on an {@link EventLoop} for best-effort execution. If the
 * loop thread is busy the invocation is delayed but no backlog of missed runs is
 * executed.
 */
public class Timer {

    @NotNull
    private final CancellableTimer cancellableTimer;

    /**
     * Constructs a Timer with the default system time provider.
     *
     * @param eventLoop The event loop on which the timer tasks are scheduled and run.
     */
    public Timer(@NotNull EventLoop eventLoop) {
        this(eventLoop, SystemTimeProvider.INSTANCE);
    }

    /**
     * Constructs a Timer with a specified time provider.
     *
     * @param eventLoop    The event loop on which the timer tasks are scheduled and run.
     * @param timeProvider The time provider used to control the scheduling.
     */
    public Timer(@NotNull EventLoop eventLoop, @NotNull TimeProvider timeProvider) {
        this.cancellableTimer = new CancellableTimer(eventLoop, timeProvider);
    }

    /**
     * Schedules {@code eventHandler} to run at a fixed-rate on the event loop.
     * Timing is best-effort and any delay does not trigger multiple catch-up
     * invocations. The underlying call returns a {@link java.io.Closeable};
     * closing it aborts future executions.
     *
     * @param eventHandler The handler to be invoked.
     * @param initialDelay first wait in milliseconds before the handler runs.
     * @param period       interval in milliseconds between executions.
     */
    public void scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                    long initialDelay,
                                    long period) {
        cancellableTimer.scheduleAtFixedRate(eventHandler, initialDelay, period);
    }

    /**
     * Schedules {@code eventHandler} to run at a fixed-rate on the event loop.
     * Timing is best-effort and any delay does not trigger multiple catch-up
     * invocations. The underlying call returns a {@link java.io.Closeable};
     * closing it aborts future executions.
     *
     * @param eventHandler The handler to be invoked.
     * @param initialDelay first wait in milliseconds before the handler runs.
     * @param period       interval in milliseconds between executions.
     * @param priority       The priority of the event handler.
     */
    public void scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                    long initialDelay,
                                    long period,
                                    HandlerPriority priority) {
        cancellableTimer.scheduleAtFixedRate(eventHandler, initialDelay, period, priority);
    }

    /**
     * Schedule {@code eventHandler} to run once after {@code delay} milliseconds.
     * Internally an {@link InvalidEventHandlerException} is thrown after the
     * execution to remove the handler.
     *
     * @param eventHandler The handler to be invoked once.
     * @param delay        how long in milliseconds to wait before the handler runs.
     */
    public void schedule(@NotNull Runnable eventHandler, long delay) {
        cancellableTimer.schedule(eventHandler, delay);
    }
}
