/*
 * Copyright 2016-2020 chronicle.software
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
 * An interface representing a DTO (Data Transfer Object) or component that can be reset to its initial state.
 * Implementations of this interface provide a {@code reset()} method to reset the state of the object.
 */
public interface Resettable {
    /**
     * Resets the state of the object to its initial state.
     * Implementations should restore the object's internal fields or properties
     * to their default values or the values set during initialization.
     */
    void reset();
}
