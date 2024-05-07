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

public interface ThreadConfinementAsserter {

    /**
     * Asserts that this thread is the only thread that has ever called this
     * method.
     *
     * @throws IllegalStateException If another thread called this method previously.
     */
    void assertThreadConfined();

    /**
     * Creates and returns a new ThreadConfinementAsserter if assertions are enabled, otherwise
     * returns a no-op asserter.
     *
     * @return Creates and returns a new ThreadConfinementAsserter if assertions are enabled, otherwise
     * returns a no-op asserter
     */
    static ThreadConfinementAsserter create() {
        return ThreadConfinementLifecycle.create();
    }

    /**
     * Creates and returns a new ThreadConfinementAsserter that is always enabled.
     *
     * @return Creates and returns a new enabled ThreadConfinementAsserter
     */
    static ThreadConfinementAsserter createEnabled() {
        return ThreadConfinementLifecycle.createEnabled();
    }
}
