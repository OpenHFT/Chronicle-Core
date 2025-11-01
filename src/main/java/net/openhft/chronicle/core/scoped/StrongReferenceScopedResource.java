/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;

/**
 * A {@link ScopedResource} that always keeps a strong reference to its
 * resource. The object therefore remains reachable until
 * {@link #closeResource()} is called. In contrast to
 * {@link WeakReferenceScopedResource}, the reference is never cleared when the
 * scope ends.
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
