/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package net.openhft.chronicle.core.util;

/**
 * A functional interface representing a {@link Runnable} that can throw a checked exception.
 * This interface is useful for lambda expressions and method references that throw checked exceptions,
 * allowing them to be used in contexts that expect a {@link Runnable} or other functional interfaces
 * that do not normally allow checked exceptions.
 *
 * @param <T> The type of the checked exception that may be thrown by the operation.
 */
@FunctionalInterface
public interface ThrowingRunnable<T extends Throwable> {

    /**
     * Executes the operation, potentially throwing a checked exception.
     *
     * @throws T The checked exception that may be thrown during execution.
     */
    void run() throws T;
}
