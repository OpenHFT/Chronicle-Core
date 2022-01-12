package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;

import java.util.Set;

import static net.openhft.chronicle.core.io.AbstractCloseable.closeableSet;

public abstract class AbstractCloseableReferenceCounted
        extends AbstractReferenceCounted
        implements ManagedCloseable {

    private transient volatile boolean closing;
    private transient volatile boolean closed;
    private transient volatile StackTrace closedHere;
    private boolean initReleased;

    protected AbstractCloseableReferenceCounted() {
        final Set<Closeable> set = closeableSet;
        if (set != null)
            synchronized (set) {
                set.add(this);
            }
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

    @Override
    protected void backgroundPerformRelease() {
        setClosing();
        super.backgroundPerformRelease();
    }

    /**
     * Set closing in case it is being released in the background.
     */
    protected void setClosing() {
        closing = true;
        setClosedHere(" closing here");
    }

    private void setClosedHere(String s) {
        closedHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + s) : null;
    }

    /**
     * When actually closed.
     */
    protected void setClosed() {
        closing = closed = true;
        setClosedHere(" closed here");
    }

    @Override
    public void throwExceptionIfClosed() throws IllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert threadSafetyCheck(true);
    }

    private void throwExceptionIfClosed0() {
        if (closing)
            throw new ClosedIllegalStateException(getClass().getName() + (closed ? " closed" : " closing"), closedHere);
    }

    protected void throwExceptionIfClosedInSetter() throws IllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert threadSafetyCheck(false);
    }

    @Override
    public boolean isClosed() {
        return refCount() <= 0 || closed;
    }
}
