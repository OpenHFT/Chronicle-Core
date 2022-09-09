package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class ReferenceChangeListenerManager {

    private final List<ReferenceChangeListener> referenceChangeListeners;
    private final ReferenceCounted owner;

    public ReferenceChangeListenerManager(ReferenceCounted owner) {
        this.owner = owner;
        referenceChangeListeners = new CopyOnWriteArrayList<>();
    }

    void add(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.add(referenceChangeListener);
    }

    void remove(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.remove(referenceChangeListener);
    }

    void notifyAdded(ReferenceOwner referenceOwner) {
        this.callReferenceChangeListeners(
                (listener, referenceCounted, lhs, rhs) -> listener.onReferenceAdded(referenceCounted, lhs),
                referenceOwner, null);
    }

    void notifyRemoved(@Nullable ReferenceOwner referenceOwner) {
        this.callReferenceChangeListeners(
                (listener, referenceCounted, lhs, rhs) -> listener.onReferenceRemoved(referenceCounted, lhs),
                referenceOwner, null);
    }

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

    public void clear() {
        referenceChangeListeners.clear();
    }

    private interface ListenerInvoker {
        void invokeListener(ReferenceChangeListener listener, ReferenceCounted referenceCounted, ReferenceOwner lhs, ReferenceOwner rhs);
    }
}
