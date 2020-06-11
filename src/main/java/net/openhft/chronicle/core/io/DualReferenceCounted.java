package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.StackTrace;

public class DualReferenceCounted implements ReferenceCountedTracer {
    private final ReferenceCountedTracer a, b;

    public DualReferenceCounted(ReferenceCountedTracer a, ReferenceCountedTracer b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public void warnAndReleaseIfNotReleased() {
        a.warnAndReleaseIfNotReleased();
    }

    @Override
    public void throwExceptionIfNotReleased() {
        a.throwExceptionIfNotReleased();
    }

    @Override
    public StackTrace createdHere() {
        return a.createdHere();
    }

    @Override
    public boolean reservedBy(ReferenceOwner owner) {
        return a.reservedBy(owner);
    }

    @Override
    public synchronized void reserve(ReferenceOwner id) throws IllegalStateException {
        a.reserve(id);
        b.reserve(id);
        if (a.refCount() != b.refCount())
            throw new AssertionError(a.refCount() + " != " + b.refCount());
    }

    @Override
    public synchronized boolean tryReserve(ReferenceOwner id) throws IllegalStateException {
        boolean aa = a.tryReserve(id);
        boolean bb = b.tryReserve(id);
        assert aa == bb;
        if (a.refCount() != b.refCount())
            throw new AssertionError(a.refCount() + " != " + b.refCount());

        return aa;
    }

    @Override
    public synchronized void release(ReferenceOwner id) throws IllegalStateException {
        a.release(id);
        b.release(id);
        if (a.refCount() != b.refCount())
            throw new AssertionError(a.refCount() + " != " + b.refCount());
    }

    @Override
    public synchronized void releaseLast(ReferenceOwner id) throws IllegalStateException {
        a.releaseLast(id);
        b.releaseLast(id);
        if (a.refCount() != b.refCount())
            throw new AssertionError(a.refCount() + " != " + b.refCount());
    }

    @Override
    public synchronized int refCount() {
        if (a.refCount() != b.refCount())
            throw new AssertionError(a.refCount() + " != " + b.refCount());
        return a.refCount();
    }

    @Override
    public void throwExceptionIfReleased() throws IllegalStateException {
        a.throwExceptionIfReleased();
    }

    @Override
    public synchronized void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        a.reserveTransfer(from, to);
        b.reserveTransfer(from, to);
        if (a.refCount() != b.refCount())
            throw new AssertionError(a.refCount() + " != " + b.refCount());

    }

    @Override
    public int referenceId() {
        return a.referenceId();
    }

    @Override
    public String referenceName() {
        return a.referenceName();
    }
}
