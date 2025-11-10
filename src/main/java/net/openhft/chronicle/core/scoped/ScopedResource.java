//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.scoped;

import java.io.Closeable;

/**
 * A short-lived handle to a shared resource.
 * <p>
 * Instances are obtained from a {@link ScopedResourcePool} such as
 * {@link ScopedThreadLocal} and are expected to be used with the
 * try-with-resources idiom. The underlying resource is returned to the pool
 * when {@link #close()} completes. Clients must not retain a reference to the
 * resource after the scope ends.
 *
 * @param <T> the type of the resource contained
 * @see ScopedResourcePool
 * @see ScopedThreadLocal
 */
public interface ScopedResource<T> extends Closeable {

    /**
     * Get the contained resource
     *
     * @return The resource
     */
    T get();

    /**
     * Signifies the end of the scope and returns the resource to the pool for
     * use by other acquirers.
     * <p>
     * This method is idempotent; additional invocations have no effect.
     */
    @Override
    void close();
}
