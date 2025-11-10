//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.Nullable;

/**
 * Receives callbacks whenever the reference count of a {@link ReferenceCounted}
 * changes. Typical uses include tracking resource ownership and diagnosing
 * reference leaks during development.
 * <p>
 * Callbacks are invoked by {@link ReferenceCounted} implementations via
 * {@link ReferenceChangeListenerManager} in the order that listeners were
 * registered. Implementations must avoid long or blocking work as callbacks can
 * be triggered while the owning object is holding internal locks, risking
 * deadlock.
 */
public interface ReferenceChangeListener {

    /**
     * Invoked immediately after a reservation is made on the supplied
     * {@link ReferenceCounted} instance.
     * <p>
     * The call may occur while the owner object is synchronized. Keep the
     * implementation short and non-blocking to avoid deadlock.
     *
     * @param referenceCounted the resource whose reference count increased
     * @param referenceOwner   the owner of the new reference
     */
    default void onReferenceAdded(ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
    }

    /**
     * Invoked after a reservation is released from the supplied
     * {@link ReferenceCounted} instance.
     * <p>
     * As above, the call can happen whilst the owner is holding locks, so avoid
     * blocking operations.
     *
     * @param referenceCounted the resource whose reference count decreased
     * @param referenceOwner   the owner whose reference was removed or {@code null}
     *                         if unknown
     */
    default void onReferenceRemoved(@Nullable ReferenceCounted referenceCounted, ReferenceOwner referenceOwner) {
    }

    /**
     * Invoked after a reservation is moved from one owner to another on the
     * supplied {@link ReferenceCounted} instance.
     * <p>
     * Implementations should again avoid lengthy work as the call may be made
     * within the {@code ReferenceCounted}'s critical section.
     *
     * @param referenceCounted the resource whose reference was transferred
     * @param fromOwner        the previous owner of the reference
     * @param toOwner          the new owner of the reference
     */
    default void onReferenceTransferred(ReferenceCounted referenceCounted, ReferenceOwner fromOwner, ReferenceOwner toOwner) {
    }
}
