/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * A utility class for managing values in a {@link ThreadLocal}.
 * <p>
 * This class provides helper methods for fetching values from {@link ThreadLocal},
 * taking care of initializing the value if not present.
 */
public final class ThreadLocalHelper {

    // Private constructor to prevent instantiation of utility  class
    private ThreadLocalHelper() {
    }

    /**
     * Retrieves a thread-local weakly-referenced value, or creates a new one using a supplier if none exists.
     *
     * @param threadLocal The ThreadLocal object to retrieve or create a value for.
     * @param supplier    The supplier to generate a new value if necessary.
     * @param <T>         The type of the value.
     * @return The retrieved or newly created value.
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
     * Retrieves a thread-local value, or creates a new one using a supplier if none exists.
     *
     * @param threadLocal The ThreadLocal object to retrieve or create a value for.
     * @param supplier    The supplier to generate a new value if necessary.
     * @param <T>         The type of the value.
     * @return The retrieved or newly created value.
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
     * Retrieves a thread-local weakly-referenced value, or creates a new one using a function if none exists.
     *
     * @param threadLocal The ThreadLocal object to retrieve or create a value for.
     * @param a           The input for the function to generate a new value if necessary.
     * @param function    The function to generate a new value if necessary.
     * @param <T>         The type of the value.
     * @param <A>         The type of the input to the function.
     * @return The retrieved or newly created value.
     */
    @NotNull
    public static <T, A> T getTL(@NotNull ThreadLocal<WeakReference<T>> threadLocal, A a, @NotNull Function<A, T> function) {
        return getTL(threadLocal, a, function, null, null);
    }

    /**
     * Retrieves a thread-local weakly-referenced value, or creates a new one using a function if none exists.
     * Allows for the registration of the newly created weak reference in a reference queue via a registrar consumer.
     *
     * @param threadLocal     The ThreadLocal object to retrieve or create a value for.
     * @param supplyingEntity The input for the function to generate a new value if necessary.
     * @param constructor     The function to generate a new value if necessary.
     * @param referenceQueue  The reference queue to register the weak reference in.
     * @param registrar       The consumer to use for registration of the weak reference.
     * @param <T>             The type of the value.
     * @param <A>             The type of the input to the function.
     * @return The retrieved or newly created value.
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
