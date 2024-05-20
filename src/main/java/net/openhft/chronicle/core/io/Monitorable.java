/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package net.openhft.chronicle.core.io;

/**
 * Interface for objects that can be monitored and unmonitored.
 * <p>
 * This is useful for managing resources that need to be tracked and potentially cleaned up
 * when they are no longer needed.
 */
public interface Monitorable {

    /**
     * Stops monitoring the resource.
     * <p>
     * Implementations of this method should ensure that the resource and any resources it uses
     * are no longer being tracked for any purpose such as cleanup, resource management, or debugging.
     * This is particularly important for resources that are explicitly managed to avoid leaks.
     */
    void unmonitor();

    /**
     * Stops the monitoring of the specified object.
     *
     * @param t The object to stop monitoring
     */
    static void unmonitor(final Object t) {
        if (t instanceof Monitorable)
            ((Monitorable) t).unmonitor();
    }
}
