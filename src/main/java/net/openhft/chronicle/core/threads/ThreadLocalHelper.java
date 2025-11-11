/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.threads;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility methods for caching values in a {@link ThreadLocal} without locking.
 * <p>
 * Each thread keeps its own instance so no synchronisation is required. Where
 * {@link WeakReference}s are used the value can be reclaimed once nothing else
 * refers to it, preventing user-level leaks if the thread outlives the value.
 */
public final class ThreadLocalHelper {

    // Private constructor to prevent instantiation of utility  class
    private ThreadLocalHelper() {
    }

    /**
     * Gets a thread-local value backed by a {@link WeakReference} without locks.
     * <p>
     * The value is created via the supplier when absent. Storing the weak
     * reference means it can be cleared when the caller drops the strong
     * reference, avoiding memory leaks if the thread persists.
     *
     * @param threadLocal ThreadLocal storing the weak reference
     * @param supplier    Supplier used to create the value on first access
     * @param <T>         Type of value held
     * @return Existing or newly created value
     */
    @NotNull
    public static <T> T getTL(@NotNull ThreadLocal<WeakReference<T>> threadLocal, @NotNull Supplier<T> supplier) {
        @Nullable WeakReference<T> ref = threadLocal.get();
        @Nullable T ret = null;
        if (ref != null) ret = ref.get();
        if (ret == null) {
            ret = supplier.get();
            ref = new WeakReference<>(ret);
            threadLocal.set(ref);
        }
        return ret;
    }

    /**
     * Gets a strong thread-local value without needing locks.
     * <p>
     * The supplier is called once per thread to create the value. The returned
     * object remains strongly referenced by the thread until removed, so use
     * {@link #getTL(ThreadLocal, Supplier)} if the value should be reclaimable.
     *
     * @param threadLocal ThreadLocal holding the value
     * @param supplier    Supplier used to create the value when absent
     * @param <T>         Type of value held
     * @return Existing or newly created value
     */
    @NotNull
    public static <T> T getSTL(@NotNull ThreadLocal<T> threadLocal, @NotNull Supplier<T> supplier) {
        @Nullable T ret = threadLocal.get();
        if (ret == null) {
            ret = supplier.get();
            threadLocal.set(ret);
        }
        return ret;
    }

    /**
     * Gets a thread-local value via a function using a {@link WeakReference}.
     * <p>
     * Each thread holds its own instance so no locking is needed. The supplier
     * is invoked only when the reference is absent or has been cleared.
     *
     * @param threadLocal ThreadLocal storing the weak reference
     * @param a           Input passed to the constructor function
     * @param function    Function used to create the value when required
     * @param <T>         Type of value held
     * @param <A>         Type of the supplied argument
     * @return Existing or newly created value
     */
    @NotNull
    public static <T, A> T getTL(@NotNull ThreadLocal<WeakReference<T>> threadLocal, A a, @NotNull Function<A, T> function) {
        return getTL(threadLocal, a, function, null, null);
    }

    /**
     * Gets a thread-local value via a function and records the weak reference.
     * <p>
     * When a new value is created the reference may be registered in the given
     * {@code referenceQueue} using the supplied {@code registrar}. This lets a
     * background cleaner notice when the value has been cleared and remove any
     * associated state outside the event loop.
     *
     * @param threadLocal     ThreadLocal holding the weak reference
     * @param supplyingEntity Argument passed to the constructor
     * @param constructor     Function to create the value if required
     * @param referenceQueue  Optional queue in which to enqueue the weak reference
     * @param registrar       Optional consumer used to register the reference
     * @param <T>             Type of value held
     * @param <A>             Type of the supplied argument
     * @return Existing or newly created value
     */
    @NotNull
    public static <T, A> T getTL(@NotNull final ThreadLocal<WeakReference<T>> threadLocal,
                                 @NotNull final A supplyingEntity,
                                 @NotNull final Function<A, T> constructor,
                                 @Nullable final ReferenceQueue<T> referenceQueue,
                                 @Nullable final Consumer<WeakReference<T>> registrar) {
        @Nullable WeakReference<T> ref = threadLocal.get();
        T result = null;
        if (ref != null)
            result = ref.get();
        if (result == null) {
            result = constructor.apply(supplyingEntity);
            if (referenceQueue != null && registrar != null) {
                ref = new WeakReference<>(result, referenceQueue);
                registrar.accept(ref);
            } else {
                ref = new WeakReference<>(result);
            }
            threadLocal.set(ref);
        }
        return result;
    }
}
