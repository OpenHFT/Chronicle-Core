/*
 * Copyright 2016 higherfrequencytrading.com
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
 * Created by peter.lawrey on 22/01/15.
 */
public enum HandlerPriority {
    /**
     * Critical task run in a tight loop
     */
    HIGH,
    /**
     * Less critical tasks called 10% of the time
     */
    MEDIUM,
    /**
     * Timing based tasks called every give interval. Note: this task will not be called if the
     * thread pauses e.g. due a GC or when debugging is used.  This makes the timer more robust to
     * delays than using absolute time differences.
     */
    TIMER,
    /**
     * Run when there is nothing else to do
     */
    DAEMON,
    /**
     * Task run in a back ground thread periodically
     */
    MONITOR,
    /**
     * Task is a blocking operation, added to a cached thread pool
     */
    BLOCKING,

    /***
     * used for replication, ensures that the replication runs on its own thread
     */
    REPLICATION,

    /**
     * A task which can be performed concurrently especially as it might block for some time.
     */
    CONCURRENT;
}
