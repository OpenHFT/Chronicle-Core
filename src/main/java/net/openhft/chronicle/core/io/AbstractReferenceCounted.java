package net.openhft.chronicle.core.io;

public abstract class AbstractReferenceCounted implements ReferenceCounted, ReferenceOwner, QueryCloseable {
    private final ReferenceCounted referenceCounted = ReferenceCounted.onReleased(this::performRelease);
    private final QueryCloseable queryCloseable;

    protected AbstractReferenceCounted() {
        this(QueryCloseables.NEVER_CLOSED);
    }

    protected AbstractReferenceCounted(QueryCloseable queryCloseable) {
        this.queryCloseable = queryCloseable;
    }

    protected abstract void performRelease();

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        queryCloseable.throwExceptionIfClosed();
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
        return !queryCloseable.isClosed() && referenceCounted.tryReserve(id);
    }

    @Override
    public int refCount() {
        return referenceCounted.refCount();
    }

    @Override
    public void throwExceptionBadResourceOwner() {
        referenceCounted.throwExceptionBadResourceOwner();
    }

    @Override
    public boolean isClosed() {
        return queryCloseable.isClosed();
    }

    @Override
    public void throwExceptionIfClosed() throws IllegalStateException {
        queryCloseable.throwExceptionIfClosed();
    }
}