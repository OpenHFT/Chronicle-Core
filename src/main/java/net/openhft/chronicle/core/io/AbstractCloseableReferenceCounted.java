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
        assert AbstractCloseable.DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(true);
    }

    private void throwExceptionIfClosed0() {
        if (closing)
            throwClosing();
    }

    private void throwClosing() {
        throw new ClosedIllegalStateException(getClass().getName() + (closed ? " closed" : " closing"), closedHere);
    }

    protected void throwExceptionIfClosedInSetter() throws IllegalStateException {
        throwExceptionIfClosed0();
        throwExceptionIfReleased();
        assert AbstractCloseable.DISABLE_SINGLE_THREADED_CHECK || threadSafetyCheck(false);
    }

    @Override
    public boolean isClosed() {
        return refCount() <= 0 || closed;
    }
}
