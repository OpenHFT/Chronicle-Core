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
 * This interface extends {@link ReferenceCountedTracer} and provides methods for monitoring the reference counted object.
 */
public interface MonitorReferenceCounted extends ReferenceCountedTracer {

    /**
     * Sets the monitored state of the object.
     *
     * @param unmonitored {@code true} to set the object as unmonitored, {@code false} to set it as monitored.
     */
    void unmonitored(boolean unmonitored);

    /**
     * @return {@code true} if the object is unmonitored, {@code false} if it is monitored.
     */
    boolean unmonitored();
}

