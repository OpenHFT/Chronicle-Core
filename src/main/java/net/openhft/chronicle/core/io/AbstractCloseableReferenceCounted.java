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
 * Represents a closeable resource with reference counting capabilities.
 * <p>
 * This abstract class extends {@link AbstractReferenceCounted} and implements {@link ManagedCloseable},
 * and is designed to manage a resource that requires reference counting to ensure proper cleanup
 * once it is no longer in use.
 *
 * <p>
 * Reference counting allows multiple users to share a single resource and ensures that the resource
 * is only closed when all references are released. Each user of the resource increments the reference
 * count upon acquiring the resource and decrements it upon releasing.
 *
 * <p>
 * This class integrates reference counting with the ability to close the resource. When the reference
 * count reaches zero, or if an explicit call to close is made, the resource transitions to the closed state
 * and cannot be used any further.
 */
public abstract class AbstractCloseableReferenceCounted
        extends AbstractReferenceCounted
        implements ManagedCloseable {

    /**
     * Indicates whether the resource is in the process of being closed.
     */
    private transient volatile boolean closing;

    /**
     * Indicates whether the resource is closed.
     */
    private transient volatile boolean closed;

    /**
     * Stack trace indicating where the resource was closed.
     */
    private transient volatile StackTrace closedHere;

    /**
     * Indicates whether the initial reference has been released.
     */
    private boolean initReleased;

    /**
     * Constructs a new {@code AbstractCloseableReferenceCounted} instance and adds the instance
     * to the {@link CloseableUtils} set for tracking.
     */
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
     * Transfers the reservation of the resource from one owner to another.
     *
     * @param from the current owner of the resource.
     * @param to   the new owner of the resource.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way.
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
     * @return {@code true} if the resource was successfully reserved; {@code false} otherwise
     * @throws ClosedIllegalStateException If the resource has been released or closed
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
     * Closes the resource in the background by marking it as closing and performing release operations.
     */
    @Override
    protected void backgroundPerformRelease() {
        setClosing();
        super.backgroundPerformRelease();
    }

    /**
     * Sets the resource as closing, indicating it is in the process of being released in the background.
     */
    protected void setClosing() {
        closing = true;
        setClosedHere(" closing here");
    }

    /**
     * Sets the location in the code where the resource was closed.
     *
     * @param message a message to be included in the stack trace.
     */
    private void setClosedHere(String s) {
        if (closedHere == null)
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
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    @Override
    public void throwExceptionIfClosed() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert AbstractCloseable.DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(true);
    }

    /**
     * Throws a {@link ClosedIllegalStateException} if the resource is currently closing or closed.
     *
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    private void throwExceptionIfClosed0() throws ClosedIllegalStateException {
        if (closing)
            throwClosing();
    }

    /**
     * Throws a {@link ClosedIllegalStateException} if the resource is closed or in the process of closing.
     *
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    private void throwClosing() throws ClosedIllegalStateException {
        throw new ClosedIllegalStateException(getClass().getName() + (closed ? " closed" : " closing"), closedHere);
    }

    /**
     * Throws an exception if the resource is closed while in a setter method.
     *
     * @throws ClosedIllegalStateException If the resource has been released or closed.
     */
    protected void throwExceptionIfClosedInSetter() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert AbstractCloseable.DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(false);
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
}
