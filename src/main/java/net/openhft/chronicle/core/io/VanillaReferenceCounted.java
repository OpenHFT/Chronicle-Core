package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;

import static net.openhft.chronicle.core.io.TracingReferenceCounted.asString;

public final class VanillaReferenceCounted implements MonitorReferenceCounted {

    private static final long VALUE;

    static {
        VALUE = UnsafeMemory.unsafeObjectFieldOffset(Jvm.getField(VanillaReferenceCounted.class, "value"));
    }

    private final Runnable onRelease;
    private final Class<?> type;
    // must be volatile
    @UsedViaReflection
    private volatile int value = 1;
    private volatile boolean released = false;
    private boolean unmonitored;
    private StackTrace releasedHere;

    VanillaReferenceCounted(final Runnable onRelease, Class<?> type) {
        this.onRelease = onRelease;
        this.type = type;
    }

    @Override
    public StackTrace createdHere() {
        return null;
    }

    @Override
    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        if (refCount() <= 0)
            throw new IllegalStateException(type.getName() + " no reservations for " + asString(owner));
        // otherwise, not sure.
        return true;
    }

    @Override
    public void reserve(ReferenceOwner id) throws ClosedIllegalStateException {
        for (; ; ) {

            int v = value;
            if (v <= 0) {
                throw newReleasedClosedIllegalStateException();
            }
            if (valueCompareAndSet(v, v + 1)) {
                break;
            }
        }
    }

    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException {
        throwExceptionIfReleased();
    }

    @Override
    public boolean tryReserve(ReferenceOwner id) {
        for (; ; ) {
            int v = value;
            if (v <= 0)
                return false;

            if (valueCompareAndSet(v, v + 1)) {
                return true;
            }
        }
    }

    private boolean valueCompareAndSet(int from, int to) {
        return UnsafeMemory.INSTANCE.compareAndSwapInt(this, VALUE, from, to);
    }

    @Override
    public void release(ReferenceOwner id) throws ClosedIllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                throw newReleasedClosedIllegalStateException();
            }
            int count = v - 1;
            if (valueCompareAndSet(v, count)) {
                if (count == 0) {
                    callOnRelease();
                }
                break;
            }
        }
    }

    public void callOnRelease() throws ClosedIllegalStateException {
        if (released && !Jvm.supportThread())
            throw new ClosedIllegalStateException(type.getName() + " already released", releasedHere);
        releasedHere = Jvm.isResourceTracing() ? new StackTrace("Released here") : null;
        released = true;
        onRelease.run();
    }

    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                if (Jvm.supportThread())
                    break;
                throw newReleasedClosedIllegalStateException();
            }
            if (v > 1) {
                throw new IllegalStateException(type.getName() + " not the last released");
            }
            if (valueCompareAndSet(1, 0)) {
                callOnRelease();
                break;
            }
        }
    }

    @Override
    public int refCount() {
        return value;
    }

    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public void throwExceptionIfNotReleased() throws IllegalStateException {
        if (refCount() > 0)
            throw new IllegalStateException(type.getName() + " still reserved, count=" + refCount());
    }

    @Override
    public void warnAndReleaseIfNotReleased() throws ClosedIllegalStateException {
        if (refCount() > 0) {
            if (!unmonitored && !AbstractCloseable.DISABLE_DISCARD_WARNING)
                Slf4jExceptionHandler.WARN.on(type, "Discarded without being released");
            callOnRelease();
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

    private ClosedIllegalStateException newReleasedClosedIllegalStateException() {
        return new ClosedIllegalStateException(type.getName() + " released");
    }

}