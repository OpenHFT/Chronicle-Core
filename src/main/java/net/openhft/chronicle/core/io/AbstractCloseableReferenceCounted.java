/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.internal.CloseableUtils;
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;

/**
 * Represents a closeable resource with reference counting, lifecycle tracking, and leak diagnostics.
 * <p>
 * The resource starts reserved by {@link ReferenceOwner#INIT}. Once all calls
 * to {@link #release(ReferenceOwner)} (or {@link #releaseLast(ReferenceOwner)})
 * have balanced earlier {@link #reserve(ReferenceOwner)} calls the resource is
 * closed. If {@link #canReleaseInBackground()} is {@code true} the cleanup is
 * delegated to {@link BackgroundResourceReleaser}.
 */
@SuppressWarnings({"java:S2065", "java:S3077"})
public abstract class AbstractCloseableReferenceCounted
        extends AbstractReferenceCounted
        implements ManagedCloseable {

    private transient volatile boolean closing;
    private transient volatile boolean closed;
    private transient volatile StackTrace closedHere;
    private boolean initReleased;

    /**
     * Constructs a new AbstractCloseableReferenceCounted instance and adds the instance
     * to the CloseableUtils set for tracking.
     */
    @SuppressWarnings("this-escape")
    protected AbstractCloseableReferenceCounted() {
        CloseableUtils.add(this);
    }

    /**
     * Attempts to reserve the resource for the given unique id.
     *
     * @param id the unique identifier representing the owner of this reserve.
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void reserve(ReferenceOwner id) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        throwExceptionIfClosed();

        super.reserve(id);
    }

    /**
     * Reserves the resource for the given id.
     *
     * @param from resource
     * @param to   resource
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
     */
    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        throwExceptionIfClosed();

        super.reserveTransfer(from, to);
        if (from == INIT) initReleased = true;
        if (to == INIT) initReleased = false;
    }

    /**
     * Releases the resource for the given id.
     *
     * @param id unique id for this release
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void release(ReferenceOwner id) throws ClosedIllegalStateException {
        super.release(id);
        if (id == INIT) initReleased = true;
    }

    /**
     * Releases the resource for the given id and checks the resource is now released.
     *
     * @param id unique id for this release
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void releaseLast(ReferenceOwner id) throws ClosedIllegalStateException {
        super.releaseLast(id);
        if (id == INIT) initReleased = true;
    }

    /**
     * Tries to reserve the resource for the given id.
     *
     * @param id unique id for this reserve
     * @return true if reserved
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public boolean tryReserve(ReferenceOwner id) throws ClosedIllegalStateException, IllegalArgumentException {
        return !closed && super.tryReserve(id);
    }

    /**
     * Closes the resource, ensuring it transitions to a closed state. If the resource
     * is being released in the background, it is marked as closing.
     */
    @Override
    public void close() {
        setClosing();
        if (!initReleased)
            try {
                release(INIT);
            } catch (IllegalStateException e) {
                Jvm.warn().on(getClass(), "Failed to release LAST, closing anyway", e);
            }
        setClosed();
    }

    /**
     * Closes the resource in the background and marks it as closing.
     */
    @Override
    protected void backgroundPerformRelease() {
        setClosing();
        super.backgroundPerformRelease();
    }

    /**
     * Sets the resource as closing in case it is being released in the background.
     */
    protected void setClosing() {
        closing = true;
        setClosedHere(" closing here");
    }

    private void setClosedHere(String s) {
        if (closedHere == null)
            closedHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + s) : null;
    }

    /**
     * Marks the resource as closed and records the close site.
     */
    protected void setClosed() {
        closing = closed = true;
        setClosedHere(" closed here");
    }

    /**
     * Throws an exception if the resource is closed.
     *
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void throwExceptionIfClosed() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(true);
    }

    private void throwExceptionIfClosed0() throws ClosedIllegalStateException {
        if (closing)
            throwClosing();
    }

    private void throwClosing() throws ClosedIllegalStateException {
        throw new ClosedIllegalStateException(getClass().getName() + (closed ? " closed" : " closing"), closedHere);
    }

    /**
     * Throws an exception if the resource is closed while in a setter method.
     *
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Deprecated(/* to be removed in 2027 */)
    protected void throwExceptionIfClosedInSetter() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(false);
    }

    /**
     * Indicates whether the resource is in a closed state.
     *
     * @return {@code true} if the resource is closed; {@code false} otherwise.
     */
    @Override
    public boolean isClosed() {
        return refCount() <= 0 || closed;
    }

    @Override
    public void unmonitor() {
        CloseableUtils.unmonitor(this);
        ReferenceCountedUtils.unmonitor(this);
    }
}
