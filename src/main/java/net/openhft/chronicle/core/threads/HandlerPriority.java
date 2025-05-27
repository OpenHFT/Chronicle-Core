/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     *
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
     */
    HIGH,
    /**
     * Tasks run about one quarter as often as {@link #HIGH}.
     * Typical call frequency is four times slower than {@link #HIGH}.
     *
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
     */
    MEDIUM,
    /**
     * Timing-based tasks executed at regular intervals.
     * Typical interval is tens of milliseconds and the relative timing is resilient to pauses.
     *
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
     */
    TIMER,
    /**
     * Tasks run only when there is nothing else to do.
     * Typical call frequency is when the event loop is otherwise idle.
     *
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
     */
    DAEMON,
    /**
     * Background monitoring tasks.
     * Typical call frequency is about once per second.
     *
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
     */
    MONITOR,
    /**
     * Tasks involving blocking operations executed on a cached thread pool.
     * Frequency depends on submitted blocking work.
     *
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
     */
    BLOCKING,
    /**
     * Replication events processed on their own thread.
     *
     * @apiNote Alias for {@link #MEDIUM}; use {@link #alias()} for the effective priority.
     * Typical call frequency follows {@link #MEDIUM}.
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
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
     * @apiNote Alias for {@link #TIMER}; use {@link #alias()} for the effective priority.
     * Typical call frequency follows {@link #TIMER}.
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
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
     * @apiNote Alias for {@link #MEDIUM}; use {@link #alias()} for the effective priority.
     * Typical call frequency follows {@link #MEDIUM}.
     * @see <a href="https://github.com/OpenHFT/Chronicle-Threads/wiki/Priorities">Chronicle Threads Priorities</a>
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
