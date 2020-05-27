package net.openhft.chronicle.core.io;

public abstract class AbstractReferenceCounted implements ReferenceCounted, ReferenceOwner {
    private final ReferenceCounted referenceCounted = ReferenceCounted.onReleased(this::performRelease);

    protected abstract void performRelease();

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        referenceCounted.reserve(id);
    }

    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {
        referenceCounted.release(id);
    }

    @Override
    public void releaseLast(ReferenceOwner id) {
        referenceCounted.releaseLast(id);
    }

    @Override
    public boolean tryReserve(ReferenceOwner id) {
        return referenceCounted.tryReserve(id);
    }

    @Override
    public int refCount() {
        return referenceCounted.refCount();
    }

    @Override
    public void throwExceptionBadResourceOwner() {
        referenceCounted.throwExceptionBadResourceOwner();
    }
}