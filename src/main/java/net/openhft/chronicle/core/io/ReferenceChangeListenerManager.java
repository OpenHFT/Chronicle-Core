package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the reference change listeners for a {@link ReferenceCounted} object.
 * It allows adding, removing, and notifying the listeners about reference changes.
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
