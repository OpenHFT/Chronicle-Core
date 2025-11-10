//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
     * Ensures a strong reference exists before the caller receives
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
