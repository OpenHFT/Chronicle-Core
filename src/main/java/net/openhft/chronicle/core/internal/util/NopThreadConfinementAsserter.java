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
 * A no-operation (NOP) implementation of {@link ThreadConfinementAsserter}.
 * <p>
 * This class is used in scenarios where thread confinement assertions are not required.
 * The {@link #assertThreadConfined()} method does nothing, effectively acting as a placeholder
 * or default implementation when thread confinement checks are unnecessary.
 * </p>
 * <p>
 * This implementation follows the Singleton design pattern and uses an enum for ensuring a single instance
 * (as enum singletons are inherently thread-safe and serialization-proof).
 * </p>
 */
enum NopThreadConfinementAsserter implements ThreadConfinementAsserter {

    /**
     * The single instance of this no-op asserter.
     */
    INSTANCE;

    /**
     * No-op implementation of the thread confinement assertion.
     * <p>
     * This method is a placeholder and performs no actual thread confinement checks. It is used
     * when thread confinement assertions are not needed or are deliberately ignored.
     * </p>
     */
    @Override
    public void assertThreadConfined() {
        // Do nothing
    }
}
