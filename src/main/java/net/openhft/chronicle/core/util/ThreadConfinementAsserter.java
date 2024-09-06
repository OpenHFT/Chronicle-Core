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

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.util.ThreadConfinementLifecycle;

/**
 * Interface for asserting thread confinement, ensuring that certain methods are only called
 * from a single thread throughout their lifecycle. This is useful for detecting improper
 * multi-threaded access to objects that are not designed to be thread-safe.
 */
public interface ThreadConfinementAsserter {

    /**
     * Asserts that the current thread is the only thread that has ever called this method
     * on the implementing object. If another thread has called this method previously,
     * an {@link IllegalStateException} is thrown to indicate a breach of thread confinement.
     *
     * @throws IllegalStateException if this method is called from a different thread than the one
     *                               that originally called it.
     */
    void assertThreadConfined();

    /**
     * Creates and returns a new {@link ThreadConfinementAsserter} instance if assertions are enabled.
     * If assertions are disabled, this method returns a no-op asserter that does not enforce any
     * thread confinement checks.
     *
     * @return A new {@link ThreadConfinementAsserter} if assertions are enabled; otherwise, a no-op
     * asserter.
     */
    static ThreadConfinementAsserter create() {
        // Delegate to ThreadConfinementLifecycle to create an appropriate asserter
        return ThreadConfinementLifecycle.create();
    }

    /**
     * Creates and returns a new {@link ThreadConfinementAsserter} that is always enabled,
     * regardless of whether assertions are enabled or disabled in the JVM.
     *
     * @return A new enabled {@link ThreadConfinementAsserter} that always performs thread confinement checks.
     */
    static ThreadConfinementAsserter createEnabled() {
        // Delegate to ThreadConfinementLifecycle to create an always-enabled asserter
        return ThreadConfinementLifecycle.createEnabled();
    }

}
