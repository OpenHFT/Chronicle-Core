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

package net.openhft.chronicle.core.scoped;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.threads.CleaningThreadLocal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A thread-local {@link ScopedResourcePool} implementation.
 * <p>
 * This class is used for managing small, tightly-scoped thread-local resource pools,
 * offering a safer alternative to thread-local singletons. It maintains a limited-depth
 * stack of instances that are local to each thread, which are allocated as they are
 * acquired and returned to the stack when the scopes close. If more instances are
 * acquired than the specified limit, a warning is logged and the extra instances are
 * made eligible for garbage collection.
 *
 * @param <T> The type of the resource being managed.
 */
public class ScopedThreadLocal<T> implements ScopedResourcePool<T> {

    private final Supplier<T> supplier; // Supplier for creating new instances.
    private final Consumer<T> onAcquire; // Consumer for performing actions on instance acquisition.
    private final CleaningThreadLocal<SimpleStack> instancesTL; // Thread-local stack of resources.
    private final boolean useWeakReferences; // Flag indicating whether to use weak references for resources.

    /**
     * Constructs a new ScopedThreadLocal with the given supplier and maximum number of instances.
     *
     * @param supplier     The supplier used to create new instances.
     * @param maxInstances The maximum number of instances that will be retained for re-use.
     */
    public ScopedThreadLocal(Supplier<T> supplier, int maxInstances) {
        this(supplier, ScopedThreadLocal::noOp, maxInstances);
    }

    /**
     * Constructs a new ScopedThreadLocal with the given supplier, acquisition action, and maximum number of instances.
     *
     * @param supplier     The supplier used to create new instances.
     * @param onAcquire    A function to run on each instance upon its acquisition.
     * @param maxInstances The maximum number of instances that will be retained for re-use.
     */
    public ScopedThreadLocal(@NotNull Supplier<T> supplier, @NotNull Consumer<T> onAcquire, int maxInstances) {
        this(supplier, onAcquire, maxInstances, false);
    }

    /**
     * Constructs a new ScopedThreadLocal with the given supplier, acquisition action, maximum number of instances, and
     * a flag indicating whether to use weak references for resources.
     *
     * @param supplier          The supplier used to create new instances.
     * @param onAcquire         A function to run on each instance upon its acquisition.
     * @param maxInstances      The maximum number of instances that will be retained for re-use.
     * @param useWeakReferences Whether to allow resources to be garbage collected when they're not in use.
     */
    public ScopedThreadLocal(@NotNull Supplier<T> supplier, @NotNull Consumer<T> onAcquire, int maxInstances, boolean useWeakReferences) {
        this.supplier = supplier;
        this.onAcquire = onAcquire;
        this.instancesTL = CleaningThreadLocal.withCloseQuietly(() -> new SimpleStack(maxInstances));
        this.useWeakReferences = useWeakReferences;
    }

    /**
     * Gets a scoped instance of the shared resource.
     * <p>
     * This method either retrieves an existing instance from the stack or creates a new one if the stack is empty.
     *
     * @return The {@link ScopedResource} to be closed once it is finished being used.
     */
    public ScopedResource<T> get() {
        final SimpleStack scopedThreadLocalResources = instancesTL.get();
        AbstractScopedResource<T> instance;
        if (scopedThreadLocalResources.isEmpty()) {
            instance = createNewResource();
        } else {
            instance = scopedThreadLocalResources.pop();
        }
        instance.preAcquire();
        onAcquire.accept(instance.get());
        return instance;
    }

    /**
     * Creates a new resource instance, using either strong or weak references based on configuration.
     *
     * @return A new instance of {@link AbstractScopedResource}.
     */
    private AbstractScopedResource<T> createNewResource() {
        if (useWeakReferences)
            return new WeakReferenceScopedResource<>(this, supplier);
        else
            return new StrongReferenceScopedResource<>(this, supplier.get());
    }

    /**
     * Returns a {@link ScopedResource} to the "pool".
     * <p>
     * This method is called when the resource is no longer needed, allowing it to be reused by other threads.
     *
     * @param scopedResource The resource to return.
     */
    void returnResource(AbstractScopedResource<T> scopedResource) {
        final SimpleStack scopedThreadLocalResources = instancesTL.get();
        scopedThreadLocalResources.push(scopedResource);
    }

    /**
     * The default onAcquire function which performs no action.
     */
    private static <T> void noOp(T instance) {
        // Do nothing
    }

    /**
     * A simple array-based stack for managing retained {@link ScopedResource} instances.
     */
    class SimpleStack implements java.io.Closeable {

        private final AbstractScopedResource<T>[] instances; // Array to hold the resource instances.
        private boolean warnedAboutCapacity = false; // Flag indicating whether a warning about capacity has been logged.
        private int headIndex = -1; // Index of the last added instance in the stack.

        /**
         * Constructs a SimpleStack with the specified maximum number of instances.
         *
         * @param maxInstances The maximum number of instances that this stack can hold.
         */
        SimpleStack(int maxInstances) {
            this.instances = (AbstractScopedResource<T>[]) Array.newInstance(AbstractScopedResource.class, maxInstances);
        }

        /**
         * Pops an instance from the stack.
         *
         * @return The most recently added instance.
         * @throws IllegalStateException if the stack is empty.
         */
        AbstractScopedResource<T> pop() {
            if (headIndex == -1) {
                throw new IllegalStateException("Can't pop an empty stack");
            }
            final AbstractScopedResource<T> instance = instances[headIndex];
            instances[headIndex] = null;
            --headIndex;
            return instance;
        }

        /**
         * Pushes an instance onto the stack.
         * <p>
         * If the stack is full, the newest instance is replaced and the excess instance is closed and discarded.
         *
         * @param instance The instance to push onto the stack.
         */
        void push(AbstractScopedResource<T> instance) {
            if (headIndex < instances.length - 1) {
                instances[++headIndex] = instance;
            } else {
                // Only warn the first time the capacity is exceeded
                if (!warnedAboutCapacity) {
                    @Nullable
                    Class<?> containedType = instances[instances.length - 1].getType();
                    Jvm.warn().on(ScopedThreadLocal.class,
                            "Pool capacity exceeded, consider increasing maxInstances, maxInstances=" + instances.length +
                            (containedType != null ? ", resourceType=" + containedType.getSimpleName() : ""));
                    warnedAboutCapacity = true;
                }
                replaceNewestInstance(instance).closeResource();
            }
        }

        /**
         * Replaces the newest instance in the stack with a returning instance.
         *
         * @param returningInstance The instance that is being returned.
         * @return The instance that was replaced and should be discarded.
         */
        private AbstractScopedResource<T> replaceNewestInstance(AbstractScopedResource<T> returningInstance) {
            long latestCreationTime = returningInstance.getCreatedTimeNanos();
            int latestCreationIndex = -1;
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].getCreatedTimeNanos() > latestCreationTime) {
                    latestCreationTime = instances[i].getCreatedTimeNanos();
                    latestCreationIndex = i;
                }
            }
            AbstractScopedResource<T> instanceBeingDiscarded = returningInstance;
            if (latestCreationIndex >= 0) {
                instanceBeingDiscarded = instances[latestCreationIndex];
                instances[latestCreationIndex] = returningInstance;
            }
            return instanceBeingDiscarded;
        }

        /**
         * Checks if the stack is empty.
         *
         * @return {@code true} if the stack is empty, {@code false} otherwise.
         */
        boolean isEmpty() {
            return headIndex == -1;
        }

        /**
         * Closes the stack by closing and nullifying all contained instances.
         *
         * @throws IllegalStateException if an error occurs during closing.
         */
        @Override
        public void close() throws IllegalStateException {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] != null) {
                    instances[i].closeResource();
                    instances[i] = null;
                }
            }
        }
    }
}
