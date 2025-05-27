/*
 * Copyright 2016-2025 chronicle.software
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
 * Unit of work that can be registered with {@link PriorityHook}.
 * <p>
 * Implementations should be effectively immutable. Registration may occur from
 * many threads but each hooklet is executed sequentially by the shutdown
 * thread.
 *
 * @apiNote Allocate unique priorities in the range {@code 0-100}. Smaller
 * values execute first and should be used for higher-level components.
 */
public abstract class Hooklet implements Comparable<Hooklet> {
    /**
     * Callback invoked by the shutdown thread.
     * Implementations should return quickly and avoid long blocking
     * operations.
     */
    public abstract void onShutdown();

    /**
     * Priority value used to order execution.
     * Lower values execute first. Suggested range is {@code 0-100}.
     *
     * @return integer value representing priority
     */
    public abstract int priority();

    /**
     * Used to detect duplicates when registering with {@link PriorityHook}.
     * The default implementation returns the runtime class of this instance.
     *
     * @return object used to identify the hooklet
     */
    protected Object identity() {
        return getClass();
    }

    /**
     * Factory method for simple hooks.
     *
     * @param priority value returned by {@link #priority()}
     * @param hook     action to run on shutdown
     * @return hooklet wrapping the given action
     * @throws NullPointerException if {@code hook} is {@code null}
     */
    public static Hooklet of(int priority, Runnable hook) {
        Objects.requireNonNull(hook);
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
     * Orders hooklets by priority then identity.
     *
     * @param other hooklet to compare
     * @return negative if this should run before {@code other}
     */
    @Override
    public int compareTo(@NotNull Hooklet other) {
        int delta = priority() - other.priority();

        if (delta == 0)
            return identity().hashCode() - other.identity().hashCode();

        return delta;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Hooklet))
            return false;

        final Hooklet that = (Hooklet) obj;

        return this.priority() == that.priority() &&
                this.identity().equals(that.identity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority(), identity());
    }

    @Override
    public String toString() {
        return "Hooklet{ priority: " + priority() + ", identity: " + identity() + " }";
    }
}
