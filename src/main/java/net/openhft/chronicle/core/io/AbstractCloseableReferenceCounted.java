package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;

import java.util.Set;

import static net.openhft.chronicle.core.io.AbstractCloseable.CLOSEABLE_SET;

public abstract class AbstractCloseableReferenceCounted
        extends AbstractReferenceCounted
        implements CloseableTracer {

    private transient volatile boolean closed;
    private transient volatile StackTrace closedHere;
    private boolean initReleased;

    public AbstractCloseableReferenceCounted() {
        Set<CloseableTracer> set = CLOSEABLE_SET;
        if (set != null)
            set.add(this);
    }

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        throwExceptionIfClosed();

        super.reserve(id);
    }

    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        throwExceptionIfClosed();

        super.reserveTransfer(from, to);
        if (from == INIT) initReleased = true;
        if (to == INIT) initReleased = false;
    }

    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {
        super.release(id);
        if (id == INIT) initReleased = true;
    }

    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        super.releaseLast(id);
        if (id == INIT) initReleased = true;
    }

    @Override
    public boolean tryReserve(ReferenceOwner id) throws IllegalStateException, IllegalArgumentException {
        return !closed && super.tryReserve(id);
    }

    @Override
    public void close() {
        if (!initReleased)
            try {
                release(INIT);
            } catch (IllegalStateException e) {
                Jvm.warn().on(getClass(), "Failed to release LAST, closing anyway", e);
            }
        setClosed();
    }

    protected void setClosed() {
        closed = true;
        closedHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + " closed here") : null;
    }

    @Override
    public void throwExceptionIfClosed() throws IllegalStateException {
        if (closed)
            throw new ClosedIllegalStateException(getClass().getName() + " closed", closedHere);
        throwExceptionIfReleased();
        assert threadSafetyCheck(true);
    }

    // throws IllegalStateException
    protected void throwExceptionIfClosedInSetter() throws IllegalStateException {
        if (closed)
            throw new ClosedIllegalStateException(getClass().getName() + " closed", closedHere);
        throwExceptionIfReleased();
        assert threadSafetyCheck(false);
    }

    @Override
    public boolean isClosed() {
        return refCount() <= 0 || closed;
    }

}
