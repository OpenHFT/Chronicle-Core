/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.internal.CloseableUtils;

/**
 * An abstract class representing a reference-counted closeable resource.
 * This class extends AbstractReferenceCounted and implements ManagedCloseable.
 */
public abstract class AbstractCloseableReferenceCounted
        extends AbstractReferenceCounted
        implements ManagedCloseable {

    private transient volatile boolean closing;
    private transient volatile boolean closed;
    private transient volatile StackTrace closedHere;
    private boolean initReleased;

    /**
     * Constructs a new AbstractCloseableReferenceCounted instance.
     * Adds the instance to the CloseableUtils set for tracking.
     */
    protected AbstractCloseableReferenceCounted() {
        CloseableUtils.add(this);
    }

    /**
     * Reserves the resource for the given id.
     *
     * @param id unique id for this reserve
     * @throws IllegalStateException if already released
     */
    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        throwExceptionIfClosed();

        super.reserve(id);
    }

    /**
     * Reserves the resource for the given id.
     *
     * @param from resource
     * @param to   resource
     * @throws IllegalStateException if resource is already released
     */
    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        throwExceptionIfClosed();

        super.reserveTransfer(from, to);
        if (from == INIT) initReleased = true;
        if (to == INIT) initReleased = false;
    }

    /**
     * Releases the resource for the given id.
     *
     * @param id unique id for this release
     * @throws IllegalStateException if resource is already released
     */
    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {
        super.release(id);
        if (id == INIT) initReleased = true;
    }

    /**
     * Releases the resource for the given id and checks the resource is now released.
     *
     * @param id unique id for this release
     * @throws IllegalStateException if resource is already released
     */
    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {
        super.releaseLast(id);
        if (id == INIT) initReleased = true;
    }

    /**
     * Tries to reserve the resource for the given id.
     *
     * @param id unique id for this reserve
     * @return true if reserved
     * @throws IllegalStateException if resource is already released
     */
    @Override
    public boolean tryReserve(ReferenceOwner id) throws IllegalStateException, IllegalArgumentException {
        return !closed && super.tryReserve(id);
    }

    /**
     * Closes the resource.
     */
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

    /**
     * Closes the resource in the background.
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
        closedHere = Jvm.isResourceTracing() ? new StackTrace(getClass().getName() + s) : null;
    }

    /**
     * Sets the resource as closed.
     */
    protected void setClosed() {
        closing = closed = true;
        setClosedHere(" closed here");
    }

    /**
     * Throws an exception if the resource is closed.
     *
     * @throws IllegalStateException if closed
     */
    @Override
    public void throwExceptionIfClosed() throws IllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert AbstractCloseable.DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(true);
    }

    private void throwExceptionIfClosed0() {
        if (closing)
            throwClosing();
    }

    private void throwClosing() {
        throw new ClosedIllegalStateException(getClass().getName() + (closed ? " closed" : " closing"), closedHere);
    }

    /**
     * Throws an exception if the resource is closed while in a setter method.
     *
     * @throws IllegalStateException if closed
     */
    protected void throwExceptionIfClosedInSetter() throws IllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert AbstractCloseable.DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(false);
    }

    /**
     * @return true if the resource is closed
     */
    @Override
    public boolean isClosed() {
        return refCount() <= 0 || closed;
    }
}
