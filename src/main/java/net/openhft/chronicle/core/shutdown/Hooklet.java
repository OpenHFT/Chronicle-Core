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
 * A shutdown hook that allows running in controlled order.
 */
public abstract class Hooklet implements Comparable<Hooklet> {
    /**
     * Accepts callback and priority to produce shutdown hook object.
     * <p>
     * Hook callback class is used to check for identity, see {@link #identity()}.
     *
     * @param priority See {@link #priority()}
     * @param hook     See {@link #onShutdown()}
     * @return Shutdown hook object. See {@link PriorityHook#addAndGet(Hooklet)}
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
     * The callback which will be invoked on shutdown.
     */
    public abstract void onShutdown();

    /**
     * Hooks with lesser priority will be called before hooks with greater priority.
     * <p>
     * It is advised to allocate a unique priority in the range of 0-100.
     * In general, more high level code needs to do its shutdown routines before lower level code.
     * An example priority layout is given below:
     * <p>
     * 0: Run before all hooks. For test/example use.
     * 1-49: Release of network resources and stopping distributed activity.
     * 50-89: Release of local resources and stopping data structures.
     * 90-99 Cleanup of file system resources such as temporary directories.
     * 100: Run after all hooks. For test/example use.
     */
    public abstract int priority();

    /**
     * Hooks are only called once but may be registered multiple times.
     * To determine if hook is already present, an object returned by this method is compared.
     * <p>
     * The default implementation returns this instance's class and should usually be sufficient.
     *
     * @return Identity object.
     */
    protected abstract Object identity();

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
