package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;

public class DualReferenceCounted implements MonitorReferenceCounted {
    private final MonitorReferenceCounted a;
    private final MonitorReferenceCounted b;
    private volatile int refCount;
    private volatile Throwable error;
    private int refCountB;

    public DualReferenceCounted(MonitorReferenceCounted a, MonitorReferenceCounted b) {
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
        try {
            a.reserve(id);
            b.reserve(id);
            this.refCount = a.refCount();
            this.refCountB = b.refCount();
            if (this.refCount != refCountB)
                throw new AssertionError(this.refCount + " != " + refCountB + " , id= " + id);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Throwable e) {
            throw Jvm.rethrow(error = e);
        }
    }

    private void checkError() {
        if (error != null)
            throw new AssertionError("Unable to use this resource due to previous error", error);
        int aRefCount = a.refCount();
        int bRefCount = b.refCount();
        if (aRefCount != bRefCount)
            throw Jvm.rethrow(error = new AssertionError(aRefCount + " != " + bRefCount, error));
    }

    @Override
    public synchronized boolean tryReserve(ReferenceOwner id) throws IllegalStateException, IllegalArgumentException {
        checkError();
        try {
            boolean aa = a.tryReserve(id);
            boolean bb = b.tryReserve(id);
            assert aa == bb;
            this.refCount = a.refCount();
            this.refCountB = b.refCount();
            if (this.refCount != refCountB)
                throw new AssertionError(this.refCount + " != " + refCountB + " , id= " + id);
            return aa;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Throwable e) {
            throw Jvm.rethrow(error = e);
        }

    }

    @Override
    public synchronized void release(ReferenceOwner id) throws IllegalStateException {
        checkError();

        try {
            a.release(id);
            b.release(id);
            this.refCount = a.refCount();
            this.refCountB = b.refCount();
            if (this.refCount != refCountB)
                throw new AssertionError(this.refCount + " != " + refCountB + " , id= " + id);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Throwable e) {
            throw Jvm.rethrow(error = e);
        }
    }

    @Override
    public synchronized void releaseLast(ReferenceOwner id) throws IllegalStateException {
        checkError();
        try {
            a.releaseLast(id);
            b.releaseLast(id);
            this.refCount = a.refCount();
            this.refCountB = b.refCount();
            if (this.refCount != refCountB)
                throw new AssertionError(this.refCount + " != " + refCountB + " , id= " + id);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Throwable e) {
            throw Jvm.rethrow(error = e);
        }
    }

    @Override
    public int refCount() {
        return refCount;
    }

    @Override
    public synchronized void throwExceptionIfReleased() throws ClosedIllegalStateException {
        checkError();
        a.throwExceptionIfReleased();
    }

    @Override
    public synchronized void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        checkError();
        try {
            a.reserveTransfer(from, to);
            b.reserveTransfer(from, to);
            this.refCount = a.refCount();
            this.refCountB = b.refCount();
            if (this.refCount != refCountB)
                throw new AssertionError(refCount + " != " + refCountB + " , from= " + from + ", to=" + to);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Throwable e) {
            throw Jvm.rethrow(error = e);
        }
    }

    @Override
    public int referenceId() {
        return a.referenceId();
    }

    @Override
    public String referenceName() {
        return a.referenceName();
    }

    @Override
    public void unmonitored(boolean unmonitored) {
        a.unmonitored(unmonitored);
        b.unmonitored(unmonitored);
    }

    @Override
    public boolean unmonitored() {
        return a.unmonitored();
    }
}
