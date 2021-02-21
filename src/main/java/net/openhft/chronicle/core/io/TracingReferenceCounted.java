/*
 * Copyright (c) 2016-2020 chronicle.software
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TracingReferenceCounted implements MonitorReferenceCounted {
    private final Map<ReferenceOwner, StackTrace> references = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<ReferenceOwner, StackTrace> releases = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Runnable onRelease;
    private final String uniqueId;
    private final Class type;
    private final StackTrace createdHere;
    private volatile StackTrace releasedHere;

    TracingReferenceCounted(final Runnable onRelease, String uniqueId, Class type) {
        this.onRelease = onRelease;
        this.uniqueId = uniqueId;
        this.type = type;
        createdHere = stackTrace("init", INIT);
        references.put(INIT, createdHere);
    }

    static String asString(Object id) {
        if (id == INIT) return "INIT";
        String s = id instanceof ReferenceOwner
                ? ((ReferenceOwner) id).referenceName()
                : id.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(id));
        if (id instanceof ReferenceCounted)
            s += " refCount=" + ((ReferenceCounted) id).refCount();
        if (id instanceof Closeable)
            s += " closed=" + ((Closeable) id).isClosed();
        return s;
    }

    @Override
    public StackTrace createdHere() {
        return createdHere;
    }

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
//        if (Jvm.isDebug())
//            System.out.println(Thread.currentThread().getName() + " " + uniqueId + " - tryReserve " + asString(id));
        synchronized (references) {
            if (references.isEmpty()) {
                if (must)
                    throw new ClosedIllegalStateException(type.getName() + " cannot reserve freed resource", createdHere);
                return false;
            }
            StackTrace stackTrace = references.get(id);
            if (stackTrace == null)
                references.putIfAbsent(id, stackTrace("reserve", id));
            else
                throw new IllegalStateException(type.getName() + " already reserved resource by " + asString(id) + " here", stackTrace);
        }
        releases.remove(id);
        return true;
    }

    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {
//        if (Jvm.isDebug())
//            System.out.println(Thread.currentThread().getName() + " " + uniqueId + " - release " + asString(id));

        boolean doOnRelease = false;
        synchronized (references) {
            if (references.remove(id) == null) {
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
            releases.put(id, stackTrace("release", id));
            if (references.isEmpty()) {
                // prevent this being called more than once.
                doOnRelease = true;
            }
        }
        // needs to be called outside synchronized block above to avoid deadlock.
        if (doOnRelease) {
            if (releasedHere != null) {
                throw new IllegalStateException(type.getName() + " already released", releasedHere);
            }
            onRelease.run();
            releasedHere = new StackTrace(type.getName() + " released here");
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
        if (references.size() <= 1) {
            release(id);
        } else {
            Exception e0 = null;
            try {
                release(id);
            } catch (Exception e) {
                e0 = e;
            }
            IllegalStateException ise = new IllegalStateException(type.getName() + " still reserved " + referencesAsString(), createdHere);
            synchronized (references) {
                references.values().forEach(ise::addSuppressed);
            }
            if (e0 != null)
                ise.addSuppressed(e0);
            throw ise;
        }
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
                IllegalStateException ise2 = new IllegalStateException(type.getName() + "reserved by " + asString(referenceOwner), reservedHere);
                if (referenceOwner instanceof Closeable) {
                    try {
                        ((Closeable) referenceOwner).throwExceptionIfClosed();
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
                } else if (referenceOwner instanceof QueryCloseable) {
                    try {
                        ((QueryCloseable) referenceOwner).throwExceptionIfClosed();

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
        if (refCount() > 0) {
            if (!AbstractCloseable.DISABLE_DISCARD_WARNING) {
                ExceptionHandler warn = AbstractCloseable.STRICT_DISCARD_WARNING ? Jvm.warn() : Slf4jExceptionHandler.WARN;
                warn.on(type, "Discarded without being released by " + referencesAsString(), createdHere);
            }
            onRelease.run();
        }
    }
}