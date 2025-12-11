/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
 * <p>Allocate unique priorities in the range {@code 0-100}. Smaller
 * values execute first and should be used for higher-level components.
 */
public abstract class Hooklet implements Comparable<Hooklet> {
    /**
     * Creates a hooklet instance. Subclasses should remain lightweight to minimise shutdown delays.
     */
    protected Hooklet() {
    }

    /**
     * Callback invoked by the shutdown thread.
     * Implementations should return quickly and avoid long blocking
     * operations.
     */
    public abstract void onShutdown();

    /**
     * Hooks with lesser priority will be called before hooks with greater priority.
     * <p>
     * It is advised to allocate an unique priority in the range of 0-100.
     * In general, more high level code needs to do its shutdown routines before lower level code.
     * An example priority layout is given below:
     * <p>
     * 0: Run before all hooks. For test/example use.
     * 1-49: Release of network resources and stopping distributed activity.
     * 50-89: Release of local resources and stopping data structures.
     * 90-99 Cleanup of file system resources such as temporary directories.
     * 100: Run after all hooks. For test/example use.
     *
     * @return priority for ordering hooks; lower runs earlier
     */
    public abstract int priority();

    /**
     * Hooks are only called once but may be registered multiple times.
     * To determine if hook is already present, an object returned by this method is compared.
     * <p>
     * The default implementation returns this instance's class and should usually be sufficient.
     *
     * @return object used to identify the hooklet
     */
    protected Object identity() {
        return getClass();
    }

    /**
     * Accepts callback and priority to produce shutdown hook object.
     * <p>
     * Hook callback class is used to check for identity, see {@link #identity()}.
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
