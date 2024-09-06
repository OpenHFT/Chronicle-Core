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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for managing the lifecycle of {@link ThreadConfinementAsserter} instances.
 * <p>
 * This class provides methods for creating thread confinement asserters, which help ensure that objects are used
 * within a single thread or as defined. It includes support for enabling or disabling assertions based on whether
 * JVM assertions are enabled at runtime.
 * </p>
 * <p>
 * This class is not intended to be instantiated and provides only static utility methods.
 * </p>
 */
public final class ThreadConfinementLifecycle {

    // Whether JVM assertions are enabled
    private static final boolean ASSERTIONS_ENABLE = assertionsEnable();

    // Private constructor to prevent instantiation
    private ThreadConfinementLifecycle() {}

    /**
     * Creates a {@link ThreadConfinementAsserter} based on the status of JVM assertions.
     * <p>
     * If assertions are enabled, a {@link VanillaThreadConfinementAsserter} is returned. Otherwise,
     * a no-op {@link NopThreadConfinementAsserter} is returned.
     * </p>
     *
     * @return A thread confinement asserter, either active or no-op based on assertions.
     */
    public static ThreadConfinementAsserter create() {
        return create(ASSERTIONS_ENABLE);
    }

    /**
     * Creates an enabled {@link ThreadConfinementAsserter}, regardless of whether JVM assertions are enabled.
     * <p>
     * This method always returns an active thread confinement asserter ({@link VanillaThreadConfinementAsserter}),
     * ensuring that thread confinement assertions are checked.
     * </p>
     *
     * @return An active thread confinement asserter.
     */
    public static ThreadConfinementAsserter createEnabled() {
        return create(true);
    }

    /**
     * Internal method to create a {@link ThreadConfinementAsserter} based on the provided active flag.
     * <p>
     * If {@code active} is true, a {@link VanillaThreadConfinementAsserter} is created to actively assert
     * thread confinement. Otherwise, a {@link NopThreadConfinementAsserter} is created, which does nothing.
     * </p>
     *
     * @param active Whether to create an active or no-op thread confinement asserter.
     * @return A thread confinement asserter, either active or no-op based on the active flag.
     */
    static ThreadConfinementAsserter create(boolean active) {
        return active
                ? new VanillaThreadConfinementAsserter()
                : NopThreadConfinementAsserter.INSTANCE;
    }

    /**
     * Determines if JVM assertions are enabled.
     * <p>
     * This method uses a simple assertion test to check if JVM assertions are active. If assertions are enabled,
     * the test will set an {@link AtomicBoolean} flag to true, which is returned by this method.
     * </p>
     *
     * @return {@code true} if JVM assertions are enabled, {@code false} otherwise.
     */
    static boolean assertionsEnable() {
        final AtomicBoolean ae = new AtomicBoolean();
        assert testAssert(ae); // Assertion test
        return ae.get();
    }

    /**
     * Helper method for testing JVM assertions.
     * <p>
     * This method is used in conjunction with the {@code assert} statement to detect if assertions are enabled.
     * </p>
     *
     * @param ae An atomic boolean that will be set to true if assertions are enabled.
     * @return Always returns {@code true}.
     */
    private static boolean testAssert(final AtomicBoolean ae) {
        ae.set(true);
        return true;
    }

}
