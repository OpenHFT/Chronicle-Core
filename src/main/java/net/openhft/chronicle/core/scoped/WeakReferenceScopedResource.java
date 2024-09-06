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

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * A {@link ScopedResource} implementation that uses a weak reference to the contained resource.
 * <p>
 * This class manages a resource such that it is weakly referenced when not in active use,
 * allowing it to be garbage collected if no other strong references exist. While the resource
 * is "in use", a strong reference is maintained to prevent it from being collected prematurely.
 * This is useful for resources that are expensive to create but can be safely discarded
 * when memory is tight.
 *
 * @param <T> The type of the contained resource.
 */
public class WeakReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final Supplier<T> supplier; // Supplier to create a new resource if needed
    private WeakReference<T> ref; // Weak reference to the resource
    private T strongRef; // Strong reference to the resource while in use

    /**
     * Constructs a {@code WeakReferenceScopedResource} with the specified supplier.
     *
     * @param scopedThreadLocal The {@link ScopedThreadLocal} managing this resource.
     * @param supplier          A supplier to provide new instances of the resource.
     */
    public WeakReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, Supplier<T> supplier) {
        super(scopedThreadLocal);
        this.supplier = supplier;
    }

    /**
     * Prepares the resource for use by ensuring it is strongly referenced.
     * <p>
     * If the weak reference is null or has been cleared, this method will
     * create a new resource using the supplier and maintain a strong reference
     * to it while it is "in use".
     */
    @Override
    void preAcquire() {
        // Check if the weak reference is null or has been cleared
        if (ref == null || ((strongRef = ref.get()) == null)) {
            // Create a new resource and establish strong and weak references
            strongRef = supplier.get();
            ref = new WeakReference<>(strongRef);
        }
    }

    /**
     * Returns the strongly referenced resource while it is "in use".
     *
     * @return The resource currently held by this {@link WeakReferenceScopedResource}.
     */
    @Override
    public T get() {
        return strongRef;
    }

    /**
     * Releases the strong reference to the resource, making it eligible for garbage collection
     * if no other references exist.
     */
    @Override
    public void close() {
        strongRef = null; // Clear strong reference
        super.close(); // Return resource to the pool
    }

    /**
     * Closes the contained resource and clears the weak reference.
     * <p>
     * This method ensures that the resource is released and the weak reference is
     * cleared, making the resource eligible for garbage collection.
     */
    @Override
    public void closeResource() {
        if (ref != null) {
            Closeable.closeQuietly(ref.get()); // Close the resource quietly
            ref.clear(); // Clear the weak reference
            ref = null; // Remove the reference itself
        }
    }

    /**
     * Returns the class type of the contained resource, if available.
     *
     * @return The {@link Class} type of the resource, or {@code null} if the resource has been garbage collected.
     */
    @Override
    public @Nullable Class<?> getType() {
        T val = ref != null ? ref.get() : null; // Retrieve the resource from the weak reference
        return val != null ? val.getClass() : null; // Return the class type or null if not available
    }
}
