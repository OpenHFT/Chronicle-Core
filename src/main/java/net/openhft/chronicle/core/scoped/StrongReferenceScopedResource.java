/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;

/**
 * {@link ScopedResource} wrapper that retains a strong reference to the resource for its entire
 * lifetime. The resource is released only when {@link #closeResource()} is invoked; unlike
 * {@link WeakReferenceScopedResource}, the reference is never cleared on scope exit.
 *
 * @param <T> the type of the contained resource
 */
public class StrongReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final T resource;

    StrongReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, T resource) {
        super(scopedThreadLocal);
        this.resource = resource;
    }

    public T get() {
        return resource;
    }

    @Override
    public void closeResource() {
        closeQuietly(resource);
    }

    @Override
    public Class<?> getType() {
        return resource.getClass();
    }
}
