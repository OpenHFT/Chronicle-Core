/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

/**
 * Enum representing different priority levels for event handlers in an event loop.
 * The priority determines how frequently and in what order the handlers are executed.
 * The {@link #alias()} method exposes the effective priority used by the scheduler
 * where an enum constant is an alias for another priority.
 */
public enum HandlerPriority {
    /**
     * Critical tasks executed in a tight loop.
     * Typical call frequency is on every scheduler cycle.
     */
    HIGH,
    /**
     * Tasks run about one quarter as often as {@link #HIGH}.
     * Typical call frequency is four times slower than {@link #HIGH}.
     */
    MEDIUM,
    /**
     * Timing-based tasks executed at regular intervals.
     * Typical interval is tens of milliseconds and the relative timing is resilient to pauses.
     */
    TIMER,
    /**
     * Tasks run only when there is nothing else to do.
     * Typical call frequency is when the event loop is otherwise idle.
     */
    DAEMON,
    /**
     * Background monitoring tasks.
     * Typical call frequency is about once per second.
     */
    MONITOR,
    /**
     * Tasks involving blocking operations executed on a cached thread pool.
     * Frequency depends on submitted blocking work.
     */
    BLOCKING,
    /**
     * Replication events processed on their own thread.
     *
     * <p> Alias for {@link #MEDIUM}; use {@link #alias()} for the effective priority.
     * Typical call frequency follows {@link #MEDIUM}.
     */
    REPLICATION {
        @Override
        public HandlerPriority alias() {
            return MEDIUM;
        }
    },
    /**
     * Timing based replication tasks.
     *
     * <p> Alias for {@link #TIMER}; use {@link #alias()} for the effective priority.
     * Typical call frequency follows {@link #TIMER}.
     */
    REPLICATION_TIMER {
        @Override
        public HandlerPriority alias() {
            return TIMER;
        }
    },
    /**
     * Tasks that can be performed concurrently and may block for some time.
     *
     * <p> Alias for {@link #MEDIUM}; use {@link #alias()} for the effective priority.
     * Typical call frequency follows {@link #MEDIUM}.
     */
    CONCURRENT {
        @Override
        public HandlerPriority alias() {
            return MEDIUM;
        }
    };

    /**
     * Returns the effective priority used by the scheduler.
     * Constants such as {@link #REPLICATION} are aliases for other priorities.
     *
     * @return the priority the scheduler employs
     */
    public HandlerPriority alias() {
        return this;
    }
}
