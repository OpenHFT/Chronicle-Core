/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;

public interface EventLoop extends Closeable {
    boolean DEBUG_ADDING_HANDLERS = Jvm.getBoolean("debug.adding.handlers");

    String name();

    /**
     * Add handler to event loop to be executed. Event loops should execute handlers in order of priority.
     * Handlers with same priority have no guarantee of execution order. Handlers will not be executed before
     * {@link #start()} has been called.
     *
     * @param handler handler
     */
    void addHandler(EventHandler handler);

    /**
     * Start the event loop
     */
    void start();

    /**
     * Typical implementation will unpause the event loop's Pauser
     */
    void unpause();

    /**
     * Notify event loop to stop executing handlers. It is not expected that event loops can then be restarted
     */
    void stop();

    /**
     * @return {@code true} close has been called
     */
    @Override
    boolean isClosed();

    /**
     * @return {@code true} if the main thread is running
     */
    boolean isAlive();

    /**
     * Wait until the event loop has terminated (after close has been called)
     */
    void awaitTermination();

    /**
     * @return true if called in an EventLoop.
     */
    static boolean inEventLoop() {
        return CleaningThread.inEventLoop(Thread.currentThread());
    }
}
