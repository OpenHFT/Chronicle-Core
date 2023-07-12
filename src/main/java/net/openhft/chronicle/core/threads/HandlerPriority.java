/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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
 */
public enum HandlerPriority {
    /**
     * Represents critical tasks that need to be run in a tight loop.
     * These tasks have the highest priority and are executed very frequently.
     */
    HIGH,
    /**
     * Represents less critical tasks.
     * These tasks are invoked approximately 1/4 as often as tasks with HIGH priority.
     */
    MEDIUM,
    /**
     * Represents timing-based tasks which are called at regular intervals.
     * These tasks are not sensitive to thread pauses such as garbage collection or debugging,
     * making them more robust against delays compared to using absolute time differences.
     */
    TIMER,
    /**
     * Represents tasks that are run only when there is nothing else to do.
     * These tasks have a very low priority and are meant to be run in the background.
     */
    DAEMON,
    /**
     * Represents tasks that are run periodically in a background thread.
     * These tasks are used for monitoring purposes.
     */
    MONITOR,
    /**
     * Represents tasks that involve blocking operations.
     * These tasks are added to a cached thread pool to avoid blocking the main event loop.
     */
    BLOCKING,
    /**
     * Used for replication tasks to ensure that replication events run on a separate thread.
     * Alias for MEDIUM priority.
     */
    REPLICATION {
        @Override
        public HandlerPriority alias() {
            return MEDIUM;
        }
    },
    /**
     * Similar to TIMER, but specifically used for replication tasks.
     * Alias for TIMER priority.
     */
    REPLICATION_TIMER {
        @Override
        public HandlerPriority alias() {
            return TIMER;
        }
    },
    /**
     * Represents tasks that can be performed concurrently, especially as they might block for some time.
     * Alias for MEDIUM priority.
     */
    CONCURRENT {
        @Override
        public HandlerPriority alias() {
            return MEDIUM;
        }
    };

    /**
     * Returns the effective priority for the handler.
     * Some priority levels are aliases for other priorities (e.g., REPLICATION is an alias for MEDIUM).
     * This method returns the actual priority that should be used for scheduling.
     *
     * @return the effective HandlerPriority.
     */
    public HandlerPriority alias() {
        return this;
    }
}
