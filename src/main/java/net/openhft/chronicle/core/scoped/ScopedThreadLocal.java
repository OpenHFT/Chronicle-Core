/*
 * Copyright 2025 chronicle.software
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
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.threads.CleaningThreadLocal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.openhft.chronicle.core.Jvm.uncheckedCast;

/**
 * Bounded per-thread pool of {@link ScopedResource} instances.
 * <p>
 * Every thread owns its own stack of resources supplied by {@code supplier}. An
 * instance is taken from the stack when {@link #get()} is called and returned to
 * it when the scope closes. If the stack is full the newest resource is
 * discarded.
 * <p>
 * The stack is confined to a single thread and so is not synchronised. The
 * resources themselves may not be thread-safe and must only be used by one
 * thread at a time unless they implement their own safety rules.
 */
public class ScopedThreadLocal<T> implements ScopedResourcePool<T> {

    private final Supplier<T> supplier;
    private final Consumer<T> onAcquire;
    private final CleaningThreadLocal<SimpleStack> instancesTL;
    private final boolean useWeakReferences;

    /**
     * Creates a pool with the given supplier and capacity.
     *
     * @param supplier     provides new instances when the stack is empty
     * @param maxInstances maximum number of retained instances per thread
     */
    public ScopedThreadLocal(Supplier<T> supplier, int maxInstances) {
        this(supplier, ScopedThreadLocal::noOp, maxInstances);
    }

    /**
     * Creates a pool with an action run on every acquisition.
     *
     * @param supplier     provides new instances when the stack is empty
     * @param onAcquire    invoked each time an instance is retrieved from the pool
     * @param maxInstances maximum number of retained instances per thread
     */
    public ScopedThreadLocal(@NotNull Supplier<T> supplier, @NotNull Consumer<T> onAcquire, int maxInstances) {
        this(supplier, onAcquire, maxInstances, false);
    }

    /**
     * Creates a pool with fine-grained control over resource retention.
     *
     * @param supplier          provides new instances when the stack is empty
     * @param onAcquire         invoked each time an instance is retrieved from the pool
     * @param maxInstances      maximum number of retained instances per thread
     * @param useWeakReferences if {@code true} weak references allow garbage collection
     */
    public ScopedThreadLocal(@NotNull Supplier<T> supplier, @NotNull Consumer<T> onAcquire, int maxInstances, boolean useWeakReferences) {
        this.supplier = supplier;
        this.onAcquire = onAcquire;
        this.instancesTL = CleaningThreadLocal.withCloseQuietly(() -> new SimpleStack(maxInstances));
        this.useWeakReferences = useWeakReferences;
    }

    /**
     * Obtains a scoped handle to a pooled instance.
     *
     * @return the handle that must be closed to return the instance to this thread
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

    private AbstractScopedResource<T> createNewResource() {
        if (useWeakReferences)
            return new WeakReferenceScopedResource<>(this, supplier);
        else
            return new StrongReferenceScopedResource<>(this, supplier.get());
    }

    /**
     * Returns a resource to the current thread's stack.
     *
     * @param scopedResource resource being released
     */
    void returnResource(AbstractScopedResource<T> scopedResource) {
        final SimpleStack scopedThreadLocalResources = instancesTL.get();
        scopedThreadLocalResources.push(scopedResource);
    }

    /**
     * The default onAcquire function
     */
    @SuppressWarnings("EmptyMethod")
    private static <T> void noOp(T instance) {
        // Do nothing
    }

    /**
     * Simple array-based stack holding retained resources for one thread.
     * Not thread-safe and relies on {@link ThreadLocal} confinement.
     */
    class SimpleStack implements java.io.Closeable {

        private final AbstractScopedResource<T>[] instances;
        private boolean warnedAboutCapacity = false;
        private int headIndex = -1;

        SimpleStack(int maxInstances) {
            this.instances = uncheckedCast(Array.newInstance(AbstractScopedResource.class, maxInstances));
        }

        AbstractScopedResource<T> pop() {
            if (headIndex == -1) {
                throw new IllegalStateException("Can't pop an empty stack");
            }
            final AbstractScopedResource<T> instance = instances[headIndex];
            instances[headIndex] = null;
            --headIndex;
            return instance;
        }

        void push(AbstractScopedResource<T> instance) {
            if (headIndex < instances.length - 1) {
                instances[++headIndex] = instance;
            } else {
                // Only warn the first time
                if (!warnedAboutCapacity) {
                    @Nullable
                    Class<?> containedType = instances[instances.length - 1].getType();
                    String message = "Pool capacity exceeded, consider increasing maxInstances, maxInstances=" + instances.length + (containedType != null ? ", resourceType=" + containedType.getSimpleName() : "");
                    Jvm.warn().on(ScopedThreadLocal.class, message, Jvm.isResourceTracing() ? new StackTrace() : null);
                    warnedAboutCapacity = true;
                }
                replaceNewestInstance(instance).closeResource();
            }
        }

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

        boolean isEmpty() {
            return headIndex == -1;
        }

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
