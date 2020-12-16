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
    // must be volatile
    @UsedViaReflection
    private volatile int value = 1;
    private volatile boolean released = false;
    private final Class type;

    VanillaReferenceCounted(final Runnable onRelease, Class type) {
        this.onRelease = onRelease;
        this.type = type;
    }

    @Override
    public StackTrace createdHere() {
        return null;
    }

    @Override
    public boolean reservedBy(ReferenceOwner owner) {
        if (refCount() <= 0)
            throw new IllegalStateException(type.getName() + " no reservations for " + asString(owner));
        // otherwise not sure.
        return true;
    }

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        for (; ; ) {

            int v = value;
            if (v <= 0) {
                throw new ClosedIllegalStateException(type.getName() + " released");
            }
            if (valueCompareAndSet(v, v + 1)) {
                break;
            }
        }
    }

    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
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
    public void release(ReferenceOwner id) throws IllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                throw new ClosedIllegalStateException(type.getName() + " released");
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

    public void callOnRelease() {
        if (released)
            throw new ClosedIllegalStateException(type.getName() + " already released");
        released = true;
        onRelease.run();
    }

    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                throw new ClosedIllegalStateException(type.getName() + " released");
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
    public void throwExceptionIfNotReleased() {
        if (refCount() > 0)
            throw new IllegalStateException(type.getName() + " still reserved, count=" + refCount());
    }

    @Override
    public void warnAndReleaseIfNotReleased() {
        if (refCount() > 0) {
            Slf4jExceptionHandler.WARN.on(type, "Discarded without being released");
            callOnRelease();
        }
    }
}