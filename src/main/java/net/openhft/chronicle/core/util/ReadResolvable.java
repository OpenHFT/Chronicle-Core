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

package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@FunctionalInterface
public interface ReadResolvable<T> {
    @SuppressWarnings("unchecked")
    static <T> T readResolve(Object o) {
        // Pattern matching
        if (o instanceof ReadResolvable) {
            return (T) ((ReadResolvable) o).readResolve();
        } else if (o instanceof Serializable) {
            return (T) ObjectUtils.readResolve(o);
        } else {
            return (T) o;
        }
    }

    /**
     * Post deserialization step
     *
     * @return the object to use instead.
     */
    @NotNull
    T readResolve();
}
