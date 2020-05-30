package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public final class VanillaReferenceCounted implements ReferenceCounted {

    private static final long VALUE;

    static {
        VALUE = UNSAFE.objectFieldOffset(Jvm.getField(VanillaReferenceCounted.class, "value"));
    }

    private final Runnable onRelease;
    private final boolean releaseOnOne;
    @UsedViaReflection
    private volatile int value;

    VanillaReferenceCounted(final Runnable onRelease, boolean releaseOnOne) {
        this.onRelease = onRelease;
        this.releaseOnOne = releaseOnOne;
    }

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        for (; ; ) {

            int v = value;
            if (v <= 0) {
                throw new IllegalStateException("Released");
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
        return UnsafeMemory.UNSAFE.compareAndSwapInt(this, VALUE, from, to);
    }

    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                throw new IllegalStateException("Released");
            }
            int count = v - 1;
            if (valueCompareAndSet(v, count)) {
                if (count == 0)
                    onRelease.run();
                else if (releaseOnOne && count == 1)
                    releaseLast(INIT);
                break;
            }
        }
    }

    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        for (; ; ) {
            int v = value;
            if (v <= 0) {
                throw new IllegalStateException("Released");
            }
            if (v > 1) {
                throw new IllegalStateException("Not the last released");
            }
            if (valueCompareAndSet(1, 0)) {
                onRelease.run();
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
    public void throwExceptionBadResourceOwner() {
        // no tracing enabled.
    }
}