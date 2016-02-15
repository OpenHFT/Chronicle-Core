/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    REPLICATION
}
