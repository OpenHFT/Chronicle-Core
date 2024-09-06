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

package net.openhft.chronicle.core.shutdown;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A shutdown hook that allows running in a controlled order.
 * <p>
 * This abstract class represents a hook that can be registered to run during the shutdown process.
 * Each hook has a priority, which determines the order in which it will be executed relative to other hooks.
 * Lower priority hooks are executed before higher priority ones. The `Hooklet` also provides a mechanism
 * to ensure that hooks are only registered once, even if added multiple times.
 * </p>
 */
public abstract class Hooklet implements Comparable<Hooklet> {

    /**
     * The callback which will be invoked on shutdown.
     * <p>
     * Subclasses must implement this method to define the behavior that should occur when the hook is triggered during shutdown.
     * </p>
     */
    public abstract void onShutdown();

    /**
     * Returns the priority of the hook.
     * <p>
     * Hooks with a lower priority value will be executed before hooks with a higher priority value.
     * It is recommended to assign a unique priority in the range of 0-100. The priority helps in organizing
     * the shutdown process where higher-level code (e.g., network resource release) should be executed
     * before lower-level code (e.g., cleanup of temporary files).
     * </p>
     *
     * @return The priority of the hook.
     */
    public abstract int priority();

    /**
     * Provides an identity object to determine if the hook is already registered.
     * <p>
     * Hooks are only executed once but can be registered multiple times. To check if a hook is already present,
     * the object returned by this method is used for comparison. By default, the identity is the class of the instance,
     * but this can be overridden if a different comparison logic is needed.
     * </p>
     *
     * @return An identity object used to determine if the hook is already registered.
     */
    protected Object identity() {
        return getClass();
    }

    /**
     * Creates a new Hooklet instance with the specified priority and shutdown behavior.
     * <p>
     * This method is a convenient way to create Hooklet instances without needing to subclass it.
     * The hook's identity is determined by the class of the provided runnable.
     * </p>
     *
     * @param priority The priority of the hook. See {@link #priority()} for more details.
     * @param hook     The runnable to be executed on shutdown. See {@link #onShutdown()} for more details.
     * @return A new Hooklet instance configured with the provided priority and runnable.
     */
    public static Hooklet of(int priority, Runnable hook) {
        return new Hooklet() {
            @Override
            public void onShutdown() {
                hook.run();
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            protected Object identity() {
                return hook.getClass();
            }
        };
    }

    /**
     * Compares this hook with another hook for order based on priority.
     * <p>
     * If two hooks have the same priority, they are further compared by their identity hash codes.
     * </p>
     *
     * @param other The other hook to be compared.
     * @return A negative integer, zero, or a positive integer as this hook's priority
     * is less than, equal to, or greater than the specified hook's priority.
     */
    @Override
    public int compareTo(@NotNull Hooklet other) {
        int delta = priority() - other.priority();

        if (delta == 0) // If priorities are equal, compare by identity hash code
            return identity().hashCode() - other.identity().hashCode();

        return delta;
    }

    /**
     * Checks if this hook is equal to another object.
     * <p>
     * Two hooks are considered equal if they have the same priority and the same identity.
     * </p>
     *
     * @param obj The object to be compared with this hook.
     * @return true if the specified object is equal to this hook; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Hooklet))
            return false;

        final Hooklet that = (Hooklet) obj;

        return this.priority() == that.priority() &&
                this.identity().equals(that.identity());
    }

    /**
     * Returns the hash code value for this hook.
     * <p>
     * The hash code is computed based on the priority and identity of the hook.
     * </p>
     *
     * @return The hash code value for this hook.
     */
    @Override
    public int hashCode() {
        return Objects.hash(priority(), identity());
    }

    /**
     * Returns a string representation of the hook.
     * <p>
     * The string representation includes the hook's priority and identity.
     * </p>
     *
     * @return A string representation of the hook.
     */
    @Override
    public String toString() {
        return "Hooklet{ priority: " + priority() + ", identity: " + identity() + " }";
    }
}
