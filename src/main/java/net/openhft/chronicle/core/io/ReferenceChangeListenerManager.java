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

package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the reference change listeners for a {@link ReferenceCounted} object.
 * <p>
 * This class allows adding, removing, and notifying listeners about reference changes. It uses a
 * {@link CopyOnWriteArrayList} to manage listeners, ensuring thread safety during concurrent
 * modifications. This is useful for managing reference counts in a concurrent environment,
 * where listeners need to be informed of changes in ownership or lifecycle events.
 * </p>
 */
class ReferenceChangeListenerManager {

    // List of listeners for reference changes, thread-safe for concurrent modifications
    private final List<ReferenceChangeListener> referenceChangeListeners;

    // The ReferenceCounted object associated with this manager
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
     * Adds a ReferenceChangeListener to this manager.
     *
     * @param referenceChangeListener The ReferenceChangeListener to add.
     */
    void add(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.add(referenceChangeListener);
    }

    /**
     * Removes a ReferenceChangeListener from this manager.
     *
     * @param referenceChangeListener The ReferenceChangeListener to remove.
     */
    void remove(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.remove(referenceChangeListener);
    }

    /**
     * Notifies the reference change listeners about a reference being added.
     *
     * @param referenceOwner The owner of the added reference.
     */
    void notifyAdded(ReferenceOwner referenceOwner) {
        this.callReferenceChangeListeners(
                (listener, referenceCounted, lhs, rhs) -> listener.onReferenceAdded(referenceCounted, lhs),
                referenceOwner, null);
    }

    /**
     * Notifies the reference change listeners about a reference being removed.
     *
     * @param referenceOwner The owner of the removed reference, or null if not known.
     */
    void notifyRemoved(@Nullable ReferenceOwner referenceOwner) {
        this.callReferenceChangeListeners(
                (listener, referenceCounted, lhs, rhs) -> listener.onReferenceRemoved(referenceCounted, lhs),
                referenceOwner, null);
    }

    /**
     * Notifies the reference change listeners about a reference being transferred.
     *
     * @param from The owner from whom the reference was transferred.
     * @param to   The owner to whom the reference was transferred.
     */
    void notifyTransferred(ReferenceOwner from, ReferenceOwner to) {
        this.callReferenceChangeListeners(ReferenceChangeListener::onReferenceTransferred, from, to);
    }

    /**
     * Iterates over the list of reference change listeners and invokes the specified method on each one.
     * <p>
     * This method is used to notify listeners of reference changes such as additions, removals, or transfers.
     * It handles potential concurrent modifications gracefully by catching {@link ArrayIndexOutOfBoundsException}.
     * </p>
     *
     * @param listenerInvoker The functional interface that invokes the desired listener method.
     * @param lhs             The first owner involved in the reference change.
     * @param rhs             The second owner involved in the reference change, or {@code null} if not applicable.
     */
    private void callReferenceChangeListeners(ListenerInvoker listenerInvoker, ReferenceOwner lhs, ReferenceOwner rhs) {
        // Loop through all reference change listeners
        for (int i = 0; i < referenceChangeListeners.size(); i++) {
            ReferenceChangeListener listener;
            try {
                // Get the listener at the current index
                listener = referenceChangeListeners.get(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                // This exception can occur if a listener is removed concurrently during the iteration
                continue;
            }
            // Invoke the listener with the provided owners
            listenerInvoker.invokeListener(listener, owner, lhs, rhs);
        }
    }

    /**
     * Clears all the reference change listeners from this manager.
     * <p>
     * This method removes all listeners that have been added to this manager, effectively resetting its state.
     * </p>
     */
    public void clear() {
        referenceChangeListeners.clear();
    }

    /**
     * Functional interface used for invoking methods on {@link ReferenceChangeListener} objects.
     */
    private interface ListenerInvoker {
        void invokeListener(ReferenceChangeListener listener, ReferenceCounted referenceCounted, ReferenceOwner lhs, ReferenceOwner rhs);
    }
}
