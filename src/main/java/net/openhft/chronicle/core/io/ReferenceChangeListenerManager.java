//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utility that invokes {@link ReferenceChangeListener} callbacks for a
 * particular {@link ReferenceCounted} instance. Listeners are stored in a
 * {@link CopyOnWriteArrayList}, so callbacks execute sequentially in the order
 * they were added. This avoids holding locks during notification and ensures
 * predictable ordering even when listeners are added or removed concurrently.
 * Typical usage is for a {@link ReferenceCounted} implementation to delegate to
 * this manager after each change to its reference count.
 */
class ReferenceChangeListenerManager {

    private final List<ReferenceChangeListener> referenceChangeListeners;
    private final ReferenceCounted owner;

    /**
     * Constructs a new ReferenceChangeListenerManager for the given ReferenceCounted owner.
     *
     * @param owner The ReferenceCounted owner to which this manager is associated.
     */
    public ReferenceChangeListenerManager(ReferenceCounted owner) {
        this.owner = owner;
        referenceChangeListeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Registers a {@link ReferenceChangeListener}. Listeners are kept in the
     * order added so that notifications occur predictably.
     *
     * @param referenceChangeListener the listener to add
     */
    void add(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.add(referenceChangeListener);
    }

    /**
     * Deregisters a {@link ReferenceChangeListener} previously added with
     * {@link #add(ReferenceChangeListener)}.
     *
     * @param referenceChangeListener the listener to remove
     */
    void remove(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.remove(referenceChangeListener);
    }

    /**
     * Invokes {@link ReferenceChangeListener#onReferenceAdded(ReferenceCounted,
     * ReferenceOwner)} on each registered listener. This should be called once
     * the underlying reference count has been incremented.
     *
     * @param referenceOwner the owner of the added reference
     */
    void notifyAdded(ReferenceOwner referenceOwner) {
        this.callReferenceChangeListeners(
                (listener, referenceCounted, lhs, rhs) -> listener.onReferenceAdded(referenceCounted, lhs),
                referenceOwner, null);
    }

    /**
     * Invokes {@link ReferenceChangeListener#onReferenceRemoved(ReferenceCounted,
     * ReferenceOwner)} on each registered listener. Call after the reference
     * count has been decremented.
     *
     * @param referenceOwner the owner of the removed reference, or {@code null} if unknown
     */
    void notifyRemoved(@Nullable ReferenceOwner referenceOwner) {
        this.callReferenceChangeListeners(
                (listener, referenceCounted, lhs, rhs) -> listener.onReferenceRemoved(referenceCounted, lhs),
                referenceOwner, null);
    }

    /**
     * Invokes {@link ReferenceChangeListener#onReferenceTransferred(ReferenceCounted,
     * ReferenceOwner, ReferenceOwner)} on each registered listener.
     *
     * @param from the owner from whom the reference was transferred
     * @param to   the owner to whom the reference was transferred
     */
    void notifyTransferred(ReferenceOwner from, ReferenceOwner to) {
        this.callReferenceChangeListeners(ReferenceChangeListener::onReferenceTransferred, from, to);
    }

    private void callReferenceChangeListeners(ListenerInvoker listenerInvoker, ReferenceOwner lhs, ReferenceOwner rhs) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < referenceChangeListeners.size(); i++) {
            ReferenceChangeListener listener;
            try {
                listener = referenceChangeListeners.get(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                // This can happen if a listener is removed concurrently during the iteration
                continue;
            }
            listenerInvoker.invokeListener(listener, owner, lhs, rhs);
        }
    }

    /**
     * Clears all the reference change listeners from this manager.
     */
    public void clear() {
        referenceChangeListeners.clear();
    }

    private interface ListenerInvoker {
        void invokeListener(ReferenceChangeListener listener, ReferenceCounted referenceCounted, ReferenceOwner lhs, ReferenceOwner rhs);
    }
}
