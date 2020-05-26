/*
 * Copyright 2016-2020 Chronicle Software
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

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.NotNull;

public interface EventLoop extends Closeable {
    boolean DEBUG_ADDING_HANDLERS = Boolean.getBoolean("debug.adding.handlers");

    String name();

    @Deprecated
    default void addHandler(boolean dontAttemptToRunImmediatelyInCurrentThread, @NotNull EventHandler handler) {
        throw new UnsupportedOperationException("dontAttemptToRunImmediatelyInCurrentThread is always true now");
    }

    void addHandler(EventHandler handler);

    void start();

    void unpause();

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
}
