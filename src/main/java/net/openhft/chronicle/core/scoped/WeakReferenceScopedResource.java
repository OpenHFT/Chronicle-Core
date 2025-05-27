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

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * A {@link ScopedResource} backed by a {@link WeakReference}. When the resource
 * is acquired a strong reference is taken and released again on
 * {@link #close()}. This allows the object to be reclaimed between usages. In
 * contrast {@link StrongReferenceScopedResource} retains a strong reference for
 * the lifetime of the wrapper.
 *
 * @param <T> the type of the contained resource
 */
public class WeakReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final Supplier<T> supplier;
    private WeakReference<T> ref;
    private T strongRef;

    public WeakReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, Supplier<T> supplier) {
        super(scopedThreadLocal);
        this.supplier = supplier;
    }

    /**
     * {@implSpec} Ensures a strong reference exists before the caller receives
     * the resource. If the previous instance was reclaimed, a new one is
     * obtained from the supplier.
     */
    @Override
    void preAcquire() {
        if (ref == null || ((strongRef = ref.get()) == null)) {
            strongRef = supplier.get();
            ref = new WeakReference<>(strongRef);
        }
    }

    @Override
    public T get() {
        return strongRef;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        strongRef = null;
        super.close();
    }

    @Override
    public void closeResource() {
        if (ref != null) {
            Closeable.closeQuietly(ref.get());
            ref.clear();
            ref = null;
        }
    }

    @Override
    public @Nullable Class<?> getType() {
        T val = ref != null ? ref.get() : null;
        return val != null ? val.getClass() : null;
    }
}
