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

package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.util.ThrowingConsumer;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * The CleaningThreadLocal class extends ThreadLocal and ensures that the resources held by
 * a CleaningThread are cleaned up if the thread dies.
 * <p>
 * Note that this class does not clean up resources if the ThreadLocal instance itself is discarded.
 *
 * @param <T> The type of resource this CleaningThreadLocal holds.
 */
public class CleaningThreadLocal<T> extends ThreadLocal<T> {
    private static final Set<CleaningThreadLocal<?>> cleaningThreadLocals = Collections.synchronizedSet(new LinkedHashSet<>());

    private final Supplier<T> supplier;
    private final Function<T, T> getWrapper;
    private final ThrowingConsumer<T, Exception> cleanup;
    private Map<Thread, Object> nonCleaningThreadValues = null;

    /**
     * Private constructor for CleaningThreadLocal.
     *
     * @param supplier The supplier that provides the resource.
     * @param cleanup  The consumer that cleans up the resource.
     */
    CleaningThreadLocal(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup) {
        this(supplier, cleanup, UnaryOperator.identity());
    }

    /**
     * Private constructor for CleaningThreadLocal.
     *
     * @param supplier   The supplier that provides the resource.
     * @param cleanup    The consumer that cleans up the resource.
     * @param getWrapper The function to apply when the get method is called.
     */
    CleaningThreadLocal(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup, UnaryOperator<T> getWrapper) {
        this.supplier = supplier;
        this.cleanup = cleanup;
        this.getWrapper = getWrapper;
        // only do this for testing.
        assert trackNonCleaningThreads();
    }

    /**
     * Creates a CleaningThreadLocal with a Closeable cleanup strategy.
     *
     * @param supplier The supplier that provides the resource.
     * @return A CleaningThreadLocal instance.
     */
    public static <T> CleaningThreadLocal<T> withCloseQuietly(Supplier<T> supplier) {
        return new CleaningThreadLocal<>(supplier, Closeable::closeQuietly);
    }

    /**
     * Creates a CleaningThreadLocal with a custom cleanup strategy.
     *
     * @param cleanup The consumer that cleans up the resource.
     * @return A CleaningThreadLocal instance.
     */
    public static <T> CleaningThreadLocal<T> withCleanup(ThrowingConsumer<T, Exception> cleanup) {
        return new CleaningThreadLocal<>(() -> null, cleanup);
    }

    /**
     * Creates a CleaningThreadLocal with a supplier and a custom cleanup strategy.
     *
     * @param supplier The supplier that provides the resource.
     * @param cleanup  The consumer that cleans up the resource.
     * @return A CleaningThreadLocal instance.
     */
    public static <T> CleaningThreadLocal<T> withCleanup(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup) {
        return new CleaningThreadLocal<>(supplier, cleanup);
    }

    /**
     * Creates a CleaningThreadLocal with a supplier, a custom cleanup strategy, and a function to apply when the get method is called.
     *
     * @param supplier   The supplier that provides the resource.
     * @param cleanup    The consumer that cleans up the resource.
     * @param getWrapper The function to apply when the get method is called.
     * @return A CleaningThreadLocal instance.
     */
    public static <T> CleaningThreadLocal<T> withCleanup(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup, Function<T, T> getWrapper) {
        return new CleaningThreadLocal<>(supplier, cleanup, getWrapper::apply);
    }

    /**
     * Cleans up resources held by threads that are no longer alive.
     */
    public static void cleanupNonCleaningThreads() {
        if (cleaningThreadLocals.isEmpty())
            return;

        synchronized (cleaningThreadLocals) {
            for (Iterator<CleaningThreadLocal<?>> iterator = cleaningThreadLocals.iterator(); iterator.hasNext(); ) {
                CleaningThreadLocal<?> nctl = iterator.next();
                final CleaningThreadLocal<?> nctl2 = nctl;
                for (Iterator<Map.Entry<Thread, Object>> iter = nctl.nonCleaningThreadValues.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry<Thread, Object> entry = iter.next();
                    if (!entry.getKey().isAlive()) {
                        ((CleaningThreadLocal<Object>) nctl2).cleanup(entry.getValue());
                        iter.remove();
                    }
                }
                if (nctl.nonCleaningThreadValues.isEmpty())
                    iterator.remove();
            }
        }
    }

    private boolean trackNonCleaningThreads() {
        cleaningThreadLocals.add(this);
        nonCleaningThreadValues = Collections.synchronizedMap(new LinkedHashMap<>());
        return true;
    }

    /**
     * Returns the initial value of this CleaningThreadLocal.
     * This method is called once per thread when the thread first uses the get() method.
     *
     * @return The initial value.
     */
    @Override
    protected T initialValue() {
        final T t = supplier.get();
        if (nonCleaningThreadValues != null) {
            Thread thread = Thread.currentThread();
            if (thread instanceof CleaningThread)
                return t;
            nonCleaningThreadValues.put(thread, t);
        }
        return t;
    }

    /**
     * Returns the value of this CleaningThreadLocal.
     *
     * @return The current value.
     */
    @Override
    public T get() {
        return getWrapper.apply(super.get());
    }

    /**
     * Sets the value of this CleaningThreadLocal and performs cleanup if necessary.
     *
     * @param value The new value to be set.
     */
    @Override
    public void set(T value) {
        final Thread thread = Thread.currentThread();
        if (thread instanceof CleaningThread) {
            CleaningThread.performCleanup(thread, this);
        } else if (nonCleaningThreadValues != null) {
            final T o = (T) nonCleaningThreadValues.put(thread, value);
            cleanup(o);
        }
        super.set(value);
    }

    /**
     * Removes the value for this CleaningThreadLocal from the current thread and performs cleanup.
     */
    @Override
    public void remove() {
        final Thread thread = Thread.currentThread();
        if (thread instanceof CleaningThread) {
            CleaningThread.performCleanup(thread, this);
        } else if (nonCleaningThreadValues != null) {
            final T o = (T) nonCleaningThreadValues.remove(thread);
            cleanup(o);
        }
        super.remove();
    }

    /**
     * Performs cleanup of the provided value. It can be safely called multiple times,
     * as cleanup will only be performed the first time for a given value.
     *
     * @param value The value to be cleaned up.
     */
    public synchronized void cleanup(T value) {
        try {
            ThrowingConsumer<T, Exception> lCleanup = this.cleanup;
            if (lCleanup != null && value != null)
                lCleanup.accept(value);
        } catch (Exception e) {
            Jvm.warn().on(getClass(), "Exception cleaning up " + value.getClass(), e);
        }
    }
}
