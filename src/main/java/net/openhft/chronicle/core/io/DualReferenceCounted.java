package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.StackTrace;

public class DualReferenceCounted implements MonitorReferenceCounted {
    private final ReferenceCountedTracer a, b;
    private volatile int refCount;
    private volatile AssertionError error;

    public DualReferenceCounted(ReferenceCountedTracer a, ReferenceCountedTracer b) {
        this.a = a;
        this.b = b;
        this.refCount = a.refCount();
    }

    @Override
    public void warnAndReleaseIfNotReleased() throws ClosedIllegalStateException {
        a.warnAndReleaseIfNotReleased();
    }

    @Override
    public void throwExceptionIfNotReleased() throws IllegalStateException {
        a.throwExceptionIfNotReleased();
    }

    @Override
    public StackTrace createdHere() {
        return a.createdHere();
    }

    @Override
    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        return a.reservedBy(owner);
    }

    @Override
    public synchronized void reserve(ReferenceOwner id) throws IllegalStateException {
        checkError();
        a.reserve(id);
        b.reserve(id);
        this.refCount = a.refCount();
        int bRefCount = b.refCount();
        if (this.refCount != bRefCount)
            throw error = new AssertionError(this.refCount + " != " + bRefCount + " , id= " + id);
    }

    private void checkError() {
        if (error != null)
            throw new AssertionError("Unable to use this resource due to previous error", error);
    }

    @Override
    public synchronized boolean tryReserve(ReferenceOwner id) throws IllegalStateException, IllegalArgumentException {
        checkError();
        boolean aa = a.tryReserve(id);
        boolean bb = b.tryReserve(id);
        assert aa == bb;
        this.refCount = a.refCount();
        if (refCount != b.refCount())
            throw error = new AssertionError(refCount + " != " + b.refCount() + " , id= " + id, error);

        return aa;
    }

    @Override
    public synchronized void release(ReferenceOwner id) throws IllegalStateException {
        checkError();
        a.release(id);
        b.release(id);
        this.refCount = a.refCount();
        int refCountB = b.refCount();
        if (this.refCount != refCountB)
            throw error = new AssertionError(this.refCount + " != " + refCountB + " , id= " + id, error);
    }

    @Override
    public synchronized void releaseLast(ReferenceOwner id) throws IllegalStateException {
        checkError();
        a.releaseLast(id);
        b.releaseLast(id);
        this.refCount = a.refCount();
        if (refCount != b.refCount())
            throw error = new AssertionError(refCount + " != " + b.refCount() + " , id= " + id, error);
    }

    @Override
    public int refCount() {
        return refCount;
    }

    @Override
    public void throwExceptionIfReleased() throws ClosedIllegalStateException {
        checkError();
        a.throwExceptionIfReleased();
    }

    @Override
    public synchronized void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        checkError();
        a.reserveTransfer(from, to);
        b.reserveTransfer(from, to);
        this.refCount = a.refCount();
        if (refCount != b.refCount())
            throw error = new AssertionError(refCount + " != " + b.refCount() + " , from= " + from + ", to=" + to);
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
