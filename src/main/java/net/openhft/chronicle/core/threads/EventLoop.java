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

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.NotNull;

/**
 * Created by peter.lawrey on 22/01/15.
 */
public interface EventLoop extends Closeable {

    void addHandler(boolean dontAttemptToRunImmediatelyInCurrentThread, @NotNull EventHandler handler);

    void addHandler(EventHandler handler);

    void start();

    void unpause();

    void stop();

    /**
     * @return {@code true} close has been called
     */
    boolean isClosed();

    /**
     * @return {@code true} if the main thread is running
     */
    boolean isAlive();
}
