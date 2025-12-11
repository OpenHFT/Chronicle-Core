/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * {@link ScopedResource} backed by a {@link WeakReference}, allowing idle resources to be
 * reclaimed between uses. A strong reference is taken on acquire and dropped on {@link #close()}.
 * In contrast {@link StrongReferenceScopedResource} retains a strong reference throughout.
 *
 * @param <T> the type of the contained resource
 */
public class WeakReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final Supplier<T> supplier;
    private WeakReference<T> ref;
    private T strongRef;

    /**
     * Creates a weakly referenced scoped resource.
     *
     * @param scopedThreadLocal owner managing per-thread lifecycle
     * @param supplier          factory used when a new instance is required
     */
    public WeakReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, Supplier<T> supplier) {
        super(scopedThreadLocal);
        this.supplier = supplier;
    }

    /**
     * Ensure a strong reference exists before the caller receives the resource. If the previous
     * instance was reclaimed, a new one is obtained from the supplier.
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
