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

package net.openhft.chronicle.core.internal.util;

import net.openhft.chronicle.core.util.ThreadConfinementAsserter;

/**
 * A basic implementation of the {@link ThreadConfinementAsserter} that ensures thread confinement.
 * <p>
 * This class tracks the first thread that accesses it and throws an {@link IllegalStateException} if any other thread
 * attempts to access it afterward. This is used to ensure that an object is confined to a single thread throughout
 * its lifecycle.
 * </p>
 */
class VanillaThreadConfinementAsserter implements ThreadConfinementAsserter {

    // The thread that initially accessed the object
    private volatile Thread initialThread;

    /**
     * Asserts that the current thread is the same as the thread that initially accessed the object.
     * <p>
     * If the current thread is different from the initial thread, this method throws an {@link IllegalStateException},
     * indicating a thread confinement violation.
     * </p>
     *
     * @throws IllegalStateException If the current thread is different from the thread that initially accessed the object.
     */
    @Override
    public void assertThreadConfined() {
        final Thread current = Thread.currentThread();
        Thread past = initialThread;
        // Lazily assign the initial thread to the current thread if not already set
        if (past == null) {
            synchronized (this) {
                if (initialThread == null) {
                    initialThread = current;
                }
            }
            past = current;
        }
        // If the current thread is different from the initial thread, throw an exception
        if (past != current) {
            throw new IllegalStateException("Thread " + current + " accessed a thread confined class that was already accessed by thread " + initialThread);
        }
    }

    /**
     * Returns a string representation of the {@link VanillaThreadConfinementAsserter}.
     * <p>
     * This method provides a simple string that shows the thread that initially accessed the object.
     * </p>
     *
     * @return A string representation of the object, including the initial thread.
     */
    @Override
    public String toString() {
        return "VanillaThreadConfinementAsserter{" +
                "initialThread=" + initialThread +
                '}';
    }
}
