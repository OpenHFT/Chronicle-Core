/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class TracingReferenceCounted implements MonitorReferenceCounted {
    private final Map<ReferenceOwner, StackTrace> references = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<ReferenceOwner, StackTrace> releases = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Runnable onRelease;
    private final String uniqueId;
    private final Class<?> type;
    private final StackTrace createdHere;
    private final ReferenceChangeListenerManager referenceChangeListeners;
    private volatile StackTrace releasedHere;
    private boolean unmonitored;

    TracingReferenceCounted(final Runnable onRelease, String uniqueId, Class<?> type) {
        this.onRelease = onRelease;
        this.uniqueId = uniqueId;
        this.type = type;
        createdHere = stackTrace("init", INIT);
        references.put(INIT, createdHere);
        referenceChangeListeners = new ReferenceChangeListenerManager(this);
    }

    static String asString(Object id) {
        if (id == INIT) return "INIT";
        String s = id instanceof ReferenceOwner
                ? ((ReferenceOwner) id).referenceName()
                : id.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(id));
        if (id instanceof ReferenceCounted)
            s += " refCount=" + ((ReferenceCounted) id).refCount();
        try {
            if (id instanceof QueryCloseable)
                s += " closed=" + ((QueryCloseable) id).isClosed();
        } catch (NullPointerException ignored) {
            // not initialised
        }
        return s;
    }

    @Override
    public StackTrace createdHere() {
        return createdHere;
    }

    @Override
    public void addReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.add(referenceChangeListener);
    }

    @Override
    public void removeReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceChangeListeners.remove(referenceChangeListener);
    }

    @Deprecated
    @Override
    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        if (references.containsKey(owner))
            return true;
        StackTrace stackTrace = releases.get(owner);
        if (stackTrace == null)
            throw new IllegalStateException(type.getName() + " never reserved by " + asString(owner));
        throw new IllegalStateException(type.getName() + " no longer reserved by " + asString(owner), stackTrace);
    }

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        tryReserve(id, true);
    }

    @Override
    public boolean tryReserve(ReferenceOwner id) throws IllegalStateException, IllegalArgumentException {
        return tryReserve(id, false);
    }

    private boolean tryReserve(ReferenceOwner id, boolean must) throws IllegalStateException {
        if (id == this)
            throw new AssertionError(type.getName() + " the counter cannot reserve itself");
        boolean addedOne = false;
        synchronized (references) {
            if (references.isEmpty()) {
                if (must)
                    throw new ClosedIllegalStateException(type.getName() + " cannot reserve freed resource", createdHere);
                return false;
            }
            StackTrace stackTrace = references.get(id);
            if (stackTrace == null) {
                final StackTrace reference = references.putIfAbsent(id, stackTrace("reserve", id));
                if (reference == null) {
                    addedOne = true;
                }
            } else
                throw new IllegalStateException(type.getName() + " already reserved resource by " + asString(id) + " here", stackTrace);
        }
        // notify outside the synchronized block to avoid potential deadlock
        if (addedOne) {
            referenceChangeListeners.notifyAdded(id);
        }
        releases.remove(id);
        return true;
    }

    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {
        tryRelease(id, true);
    }

    /**
     * Try and release a reference
     *
     * @param id   The owner whose reference to release
     * @param must if true, throw an exception if the release was unsuccessful, if false, just return
     */
    private void tryRelease(ReferenceOwner id, boolean must) {
        boolean oneWasReleased = false;
        boolean lastWasReleased = false;
        synchronized (references) {
            if (references.remove(id) == null) {
                if (must) {
                    throwInvalidReleaseException(id);
                }
            } else {
                oneWasReleased = true;
            }
            releases.put(id, stackTrace("release", id));
            if (references.isEmpty()) {
                // prevent this being called more than once.
                lastWasReleased = true;
            }
        }
        // needs to be called outside synchronized block above to avoid deadlock.
        if (oneWasReleased) {
            referenceChangeListeners.notifyRemoved(id);
        }
        if (lastWasReleased) {
            if (releasedHere != null) {
                throw new IllegalStateException(type.getName() + " already released", releasedHere);
            }
            onRelease.run();
            releasedHere = new StackTrace(type.getName() + " released here");
        }
    }

    private void throwInvalidReleaseException(ReferenceOwner id) {
        StackTrace stackTrace = releases.get(id);
        if (stackTrace == null) {
            Throwable cause = createdHere;
            if (!references.isEmpty()) {
                StackTrace ste = references.values().iterator().next();
                cause = new IllegalStateException(type.getName() + " reserved by " + referencesAsString(), ste);
            }
            throw new IllegalStateException(type.getName() + " not reserved by " + asString(id), cause);
        } else {
            throw new ClosedIllegalStateException(type.getName() + " already released " + asString(id) + " location ", stackTrace);
        }
    }

    @NotNull
    public List<String> referencesAsString() {
        synchronized (references) {
            return references.keySet().stream()
                    .map(TracingReferenceCounted::asString)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        Exception e0 = null;
        try {
            release(id);
        } catch (Exception e) {
            e0 = e;
        }
        if (references.size() > 0) {
            IllegalStateException ise = new IllegalStateException(type.getName() + " still reserved " + referencesAsString(), createdHere);
            synchronized (references) {
                references.values().forEach(ise::addSuppressed);
            }
            if (e0 != null)
                ise.addSuppressed(e0);
            throw ise;
        }
        if (e0 != null)
            Jvm.rethrow(e0);
        // If all went well, clear the listeners
        referenceChangeListeners.clear();
    }

    @Override
    public int refCount() {
        return references.size();
    }

    @NotNull
    public String toString() {
        return uniqueId + " - " + referencesAsString();
    }

    @NotNull
    private StackTrace stackTrace(String oper, ReferenceOwner ro) {
        return new StackTrace(uniqueId + " "
                + Thread.currentThread().getName() + " "
                + oper + " "
                + asString(ro));
    }

    @Override
    public void throwExceptionIfNotReleased() throws IllegalStateException {
        synchronized (references) {
            if (references.isEmpty())
                return;
            IllegalStateException ise = new ClosedIllegalStateException(type.getName() + " retained reference closed");
            for (Map.Entry<ReferenceOwner, StackTrace> entry : references.entrySet()) {
                ReferenceOwner referenceOwner = entry.getKey();
                StackTrace reservedHere = entry.getValue();
                IllegalStateException ise2 = new IllegalStateException(type.getName() + " reserved by " + asString(referenceOwner), reservedHere);
                if (referenceOwner instanceof Closeable) {
                    try {
                        ((ManagedCloseable) referenceOwner).throwExceptionIfClosed();
                    } catch (IllegalStateException ise3) {
                        ise2.addSuppressed(ise3);
                    }
                } else if (referenceOwner instanceof AbstractReferenceCounted) {
                    try {
                        ((AbstractReferenceCounted) referenceOwner).throwExceptionIfReleased();
                    } catch (IllegalStateException ise3) {
                        ise2.addSuppressed(ise3);
                    }
                }
                ise.addSuppressed(ise2);
                if (referenceOwner instanceof AbstractCloseable) {
                    AbstractCloseable ac = (AbstractCloseable) referenceOwner;
                    try {
                        ac.throwExceptionIfClosed();

                    } catch (IllegalStateException e) {
                        ise.addSuppressed(e);
                    }
                } else if (referenceOwner instanceof ManagedCloseable) {
                    try {
                        ((ManagedCloseable) referenceOwner).throwExceptionIfClosed();

                    } catch (Throwable t) {
                        ise.addSuppressed(new ClosedIllegalStateException(type.getName() + " closed " + asString(referenceOwner), t));
                    }
                }
            }
            if (ise.getSuppressed().length > 0)
                throw ise;
        }
    }

    @Override
    public void throwExceptionIfReleased() throws ClosedIllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException(type.getName() + " released", releasedHere);
    }

    @Override
    public void warnAndReleaseIfNotReleased() {
        List<ReferenceOwner> remainingOwners = null;
        synchronized (references) {
            if (refCount() > 0) {
                if (!unmonitored && !AbstractCloseable.DISABLE_DISCARD_WARNING) {
                    Jvm.warn().on(type, "Discarded without being released by " + referencesAsString(), createdHere);
                }
                remainingOwners = new ArrayList<>(references.keySet());
            }
        }
        // Run outside the synchronized block to avoid risk of deadlock
        if (remainingOwners != null) {
            remainingOwners.forEach(ro -> this.tryRelease(ro, false));
        }
    }

    @Override
    public void unmonitored(boolean unmonitored) {
        this.unmonitored = unmonitored;
    }

    @Override
    public boolean unmonitored() {
        return unmonitored;
    }
}