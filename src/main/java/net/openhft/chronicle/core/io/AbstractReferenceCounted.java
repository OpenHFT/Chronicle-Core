package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;

import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;

public abstract class AbstractReferenceCounted implements ReferenceCounted, ReferenceOwner, QueryCloseable {
    private final ReferenceCounted referenceCounted;
    private final QueryCloseable queryCloseable;

    protected AbstractReferenceCounted() {
        this(QueryCloseables.NEVER_CLOSED);
    }

    protected AbstractReferenceCounted(QueryCloseable queryCloseable) {
        this.queryCloseable = queryCloseable;
        Runnable performRelease = BG_RELEASER && performReleaseInBackground()
                ? this::backgroundPerformRelease
                : this::inThreadPerformRelease;
        referenceCounted = ReferenceCounted.onReleased(performRelease);
    }

    void backgroundPerformRelease() {
        BackgroundResourceReleaser.release(this);
    }

    void inThreadPerformRelease() {
        long start = System.nanoTime();
        performRelease();
        long time = System.nanoTime() - start;
        if (time >= 2_000_000)
            Jvm.warn().on(getClass(), "Took " + time / 100_000 / 10.0 + " ms to performRelease");
    }

    protected boolean performReleaseInBackground() {
        return false;
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
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        referenceCounted.releaseLast(id);
    }

    @Override
    public boolean tryReserve(ReferenceOwner id) throws IllegalStateException {
        return !queryCloseable.isClosed() && referenceCounted.tryReserve(id);
    }

    @Override
    public int refCount() {
        return referenceCounted.refCount();
    }

    @Override
    public void throwExceptionBadResourceOwner() throws IllegalStateException {
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