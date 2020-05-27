/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TracingReferenceCounted implements ReferenceCounted {
    private final Map<ReferenceOwner, StackTrace> references = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<ReferenceOwner, StackTrace> releases = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Runnable onRelease;
    private final StackTrace init;
    private final boolean releaseOnOne;

    TracingReferenceCounted(final Runnable onRelease, boolean releaseOnOne) {
        this.onRelease = onRelease;
        init = stackTrace("init", INIT);
        references.put(INIT, init);
        this.releaseOnOne = releaseOnOne;
    }

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        if (id == this)
            throw new IllegalArgumentException("The counter cannot reserve itself");
        if (Jvm.isDebug())
            System.out.println(Thread.currentThread().getName() + " " + uniqueId() + " - reserve " + asString(id));
        synchronized (references) {
            if (references.isEmpty()) {
                throw new IllegalStateException("Cannot reserve freed resource", init);
            }
            StackTrace stackTrace = references.get(id);
            if (stackTrace == null)
                references.putIfAbsent(id, stackTrace("reserve", id));
            else
                throw new IllegalStateException("Already reserved resource by " + id + " here", stackTrace);
        }
        releases.remove(id);
    }

    private String asString(ReferenceOwner id) {
        return id == INIT ? "INIT"
                : id instanceof VanillaReferenceOwner
                ? id.toString()
                : id.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(id));
    }

    @Override
    public boolean tryReserve(ReferenceOwner id) {
        if (Jvm.isDebug())
            System.out.println(Thread.currentThread().getName() + " " + uniqueId() + " - tryReserve " + asString(id));
        synchronized (references) {
            if (references.isEmpty()) {
                return false;
            }
            references.put(id, stackTrace("reserve", id));
        }
        releases.remove(id);
        return true;
    }

    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {
        if (Jvm.isDebug())
            System.out.println(Thread.currentThread().getName() + " " + uniqueId() + " - release " + asString(id));
        synchronized (references) {
            if (releaseOnOne && id == INIT && references.containsKey(INIT) && references.size() > 1) {
                throw new IllegalStateException("INIT has to be the last release for releaseOnOne");
            }
            if (references.remove(id) == null) {
                StackTrace stackTrace = releases.get(id);
                if (stackTrace == null) {
                    Throwable cause = init;
                    if (!references.isEmpty()) {
                        StackTrace ste = references.values().iterator().next();
                        cause = new IllegalStateException("Reserved by " + referencesAsString(), ste);
                    }
                    throw new IllegalStateException("Not reserved by " + asString(id), cause);
                } else {
                    throw new IllegalStateException("Already released " + asString(id) + " location ", stackTrace);
                }
            }
            releases.put(id, stackTrace("release", id));
            if (references.isEmpty()) {
                // prevent this being called more than once.
                onRelease.run();
            } else if (releaseOnOne && references.size() == 1) {
                releaseLast(INIT);
            }
        }
    }

    @NotNull
    public List<String> referencesAsString() {
        return references.keySet().stream().map(this::asString).collect(Collectors.toList());
    }

    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        synchronized (references) {
            if (references.size() <= 1) {
                release(id);
            } else {
                try {
                    release(id);
                } catch (Exception ignored) {
                }
                IllegalStateException ise = new IllegalStateException("Still reserved " + referencesAsString(), init);
                references.values().forEach(ise::addSuppressed);
                throw ise;
            }
        }
    }

    @Override
    public int refCount() {
        return references.size();
    }

    @NotNull
    public String toString() {
        return uniqueId() + " - " + referencesAsString();
    }

    private String uniqueId() {
        // somewhat unique
        return Integer.toHexString(System.identityHashCode(this));
    }

    @NotNull
    private StackTrace stackTrace(String oper, ReferenceOwner ro) {
        return new StackTrace(toString() + " "
                + Thread.currentThread().getName() + " "
                + oper + " "
                + asString(ro));
    }

    @Override
    public void throwExceptionBadResourceOwner() throws IllegalStateException {
        IllegalStateException ise = new IllegalStateException("Retained reference closed");

        for (ReferenceOwner referenceOwner : references.keySet()) {
            if (referenceOwner instanceof AbstractCloseable) {
                AbstractCloseable ac = (AbstractCloseable) referenceOwner;
                try {
                    ac.throwExceptionIfClosed();
                } catch (IllegalStateException e) {
                    ise.addSuppressed(e);
                }
            } else if (referenceOwner instanceof QueryCloseable) {
                try {
                    boolean closed = ((QueryCloseable) referenceOwner).isClosed();
                    if (closed)
                        ise.addSuppressed(new IllegalStateException("Closed " + asString(referenceOwner)));
                } catch (Throwable t) {
                    ise.addSuppressed(new IllegalStateException("Closed unknown " + asString(referenceOwner), t));
                }
            } else {
                try {
                    Method isClosed = referenceOwner.getClass().getDeclaredMethod("isClosed");
                    boolean closed = (Boolean) isClosed.invoke(referenceOwner);
                    if (closed)
                        ise.addSuppressed(new IllegalStateException("Closed " + asString(referenceOwner)));
                } catch (Exception e) {
                    ise.addSuppressed(new IllegalStateException("Closed status unknown " + asString(referenceOwner), e));
                }
            }
        }
        if (ise.getSuppressed().length > 0)
            throw ise;
    }
}