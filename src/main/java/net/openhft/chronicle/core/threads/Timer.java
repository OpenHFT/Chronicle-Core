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
 *
 */

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.time.SystemTimeProvider;
import net.openhft.chronicle.core.time.TimeProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Timer class used to schedule tasks to be executed periodically or after a certain delay.
 * The scheduling is performed on the provided event loop, and the execution time is best-effort.
 * This means that if the event loop thread is busy, the execution of the scheduled task may be delayed.
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
     * Uses the event loop thread to call the event handler periodically. The time that the event is
     * called back is best-effort, but if the thread is busy that call back may be delayed.
     *
     * @param eventHandler   The handler to be called back.
     * @param initialDelayMs How long in milliseconds to wait before being called back.
     * @param periodMs       The poll interval of being called.
     */
    public void scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                    long initialDelayMs,
                                    long periodMs) {
        cancellableTimer.scheduleAtFixedRate(eventHandler, initialDelayMs, periodMs);
    }

    /**
     * Uses the event loop thread to call the event handler periodically. The time that the event is
     * called back is best-effort, but if the thread is busy that call back may be delayed.
     *
     * @param eventHandler   The handler to be called back.
     * @param initialDelayMs How long in milliseconds to wait before being called back.
     * @param periodMs       The poll interval of being called.
     * @param priority       The priority of the event handler.
     */
    public void scheduleAtFixedRate(@NotNull VanillaEventHandler eventHandler,
                                    long initialDelayMs,
                                    long periodMs,
                                    HandlerPriority priority) {
        cancellableTimer.scheduleAtFixedRate(eventHandler, initialDelayMs, periodMs, priority);
    }

    /**
     * Schedule a handler to run once after a delay.
     *
     * @param eventHandler   The handler to be called back.
     * @param initialDelayMs How long in milliseconds to wait before being called back.
     */
    public void schedule(@NotNull Runnable eventHandler, long initialDelayMs) {
        cancellableTimer.schedule(eventHandler, initialDelayMs);
    }
}
