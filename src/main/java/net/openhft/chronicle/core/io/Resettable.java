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
package net.openhft.chronicle.core.io;

/**
 * An interface for components that support reuse by resetting their state.
 * Typical implementations are simple DTOs used in object pools.
 */
public interface Resettable {
    /**
     * Restore the object to its initial state. Called before returning an object
     * to a pool or when reusing the instance for another operation.
     */
    void reset();
}
