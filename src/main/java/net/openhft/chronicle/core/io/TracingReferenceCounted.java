/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.internal.CloseableUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.openhft.chronicle.core.internal.CloseableUtils.asString;

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

    @Override
    public void reserve(ReferenceOwner id) throws ClosedIllegalStateException {
        tryReserve(id, true);
    }

    @Override
    public boolean tryReserve(ReferenceOwner id) throws ClosedIllegalStateException, IllegalArgumentException {
        return tryReserve(id, false);
    }

    private boolean tryReserve(ReferenceOwner id, boolean must) throws ClosedIllegalStateException {
        if (id == this)
            throw new AssertionError(type.getName() + " the counter cannot reserve itself");
        synchronized (references) {
            if (references.isEmpty()) {
                if (must)
                    throw new ClosedIllegalStateException(type.getName() + " cannot reserve freed resource", createdHere);
                return false;
            }
            StackTrace stackTrace = references.get(id);
            if (stackTrace == null)
                references.put(id, stackTrace("reserve", id));
            else
                throw new IllegalStateException(type.getName() + " already reserved resource by " + asString(id) + " here", stackTrace);
        }
        // notify outside the synchronized block to avoid potential deadlock
        referenceChangeListeners.notifyAdded(id);
        releases.remove(id);
        return true;
    }

    @Override
    public void release(ReferenceOwner id) throws ClosedIllegalStateException {

        boolean doOnRelease = false;
        synchronized (references) {
            if (references.remove(id) == null) {
                throw throwInvalidReleaseException(id);
            }
            releases.put(id, stackTrace("release", id));
            if (references.isEmpty()) {
                // prevent this being called more than once.
                doOnRelease = true;
            }
        }
        // needs to be called outside synchronized block above to avoid deadlock.
        referenceChangeListeners.notifyRemoved(id);
        if (doOnRelease) {
            if (releasedHere != null) {
                throw new IllegalStateException(type.getName() + " already released", releasedHere);
            }
            onRelease.run();
            releasedHere = new StackTrace(type.getName() + " released here");
        }
    }

    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException {
        synchronized (references) {
            final StackTrace stackTrace = references.get(to);
            if (stackTrace != null) {
                throw new IllegalStateException(type.getName() + " already reserved resource by " + asString(to) + " here", stackTrace);
            }
            if (references.remove(from) == null) {
                throw throwInvalidReleaseException(from);
            }
            releases.put(from, stackTrace("reserveTransfer", from));
            references.put(to, stackTrace("reserveTransfer", to));
            releases.remove(to);
        }
        referenceChangeListeners.notifyTransferred(from, to);
    }

    private IllegalStateException throwInvalidReleaseException(ReferenceOwner id) throws ClosedIllegalStateException {
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
                    .map(CloseableUtils::asString)
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
            IllegalStateException ise = new IllegalStateException(type.getName() + " retained reference closed");

            for (Map.Entry<ReferenceOwner, StackTrace> entry : references.entrySet()) {
                ReferenceOwner referenceOwner = entry.getKey();
                IllegalStateException ise2 = generateIllegalStateException(referenceOwner, entry.getValue());
                ise.addSuppressed(ise2);

                if (referenceOwner instanceof AbstractCloseable) {
                    addCloseableSuppressed(ise, (AbstractCloseable) referenceOwner);
                } else if (referenceOwner instanceof ManagedCloseable) {
                    addManagedCloseableSuppressed(ise, (ManagedCloseable) referenceOwner);
                }
            }
            if (ise.getSuppressed().length > 0)
                throw ise;
        }
    }

    private IllegalStateException generateIllegalStateException(ReferenceOwner referenceOwner, StackTrace reservedHere) {
        IllegalStateException ise2 = new IllegalStateException(type.getName() + " reserved by " + asString(referenceOwner), reservedHere);
        if (referenceOwner instanceof Closeable) {
            try {
                ((ManagedCloseable) referenceOwner).throwExceptionIfClosed();
            } catch (Throwable ise3) {
                ise2.addSuppressed(ise3);
            }
        } else if (referenceOwner instanceof AbstractReferenceCounted) {
            try {
                ((AbstractReferenceCounted) referenceOwner).throwExceptionIfReleased();
            } catch (Throwable ise3) {
                ise2.addSuppressed(ise3);
            }
        }
        return ise2;
    }

    private void addCloseableSuppressed(Exception ise, AbstractCloseable ac) {
        try {
            ac.throwExceptionIfClosed();
        } catch (Throwable e) {
            ise.addSuppressed(e);
        }
    }

    private void addManagedCloseableSuppressed(Exception ise, ManagedCloseable mc) {
        try {
            mc.throwExceptionIfClosed();
        } catch (Throwable t) {
            ise.addSuppressed(new ClosedIllegalStateException(type.getName() + " closed " + asString(mc), t));
        }
    }

    @Override
    public void throwExceptionIfReleased() throws ClosedIllegalStateException {
        if (refCount() <= 0)
            throw new ClosedIllegalStateException(type.getName() + " released", releasedHere);
    }

    @Override
    public void warnAndReleaseIfNotReleased() {
        boolean runOnRelease = false;
        synchronized (references) {
            if (refCount() > 0) {
                if (!unmonitored && !AbstractCloseable.DISABLE_DISCARD_WARNING) {
                    Jvm.warn().on(type, "Discarded without being released by " + referencesAsString(), createdHere);
                }
                references.clear();
                runOnRelease = true;
            }
        }
        // Run outside the synchronized block to avoid risk of deadlock
        if (runOnRelease) {
            onRelease.run();
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
