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
import net.openhft.chronicle.core.internal.ReferenceCountedUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;

public abstract class AbstractReferenceCounted implements ReferenceCountedTracer, ReferenceOwner, SingleThreadedChecked {
    protected static final long WARN_NS = (long) (Jvm.getDouble("reference.warn.secs", 0.003) * 1e9);
    protected static final int WARN_COUNT = Jvm.getInteger("reference.warn.count", Integer.MAX_VALUE);
    static volatile Set<AbstractReferenceCounted> referenceCountedSet;
    protected final transient MonitorReferenceCounted referenceCounted;
    private final int referenceId;
    private transient volatile Thread usedByThread;
    private transient volatile StackTrace usedByThreadHere;
    private boolean singleThreadedCheckDisabled;

    protected AbstractReferenceCounted() {
        this(true);
    }

    protected AbstractReferenceCounted(boolean monitored) {
        Runnable performRelease = BG_RELEASER && canReleaseInBackground()
                ? this::backgroundPerformRelease
                : this::inThreadPerformRelease;
        referenceId = IOTools.counter(getClass()).incrementAndGet();
        referenceCounted = (MonitorReferenceCounted) ReferenceCountedTracer.onReleased(performRelease, this::referenceName, getClass());
        referenceCounted.unmonitored(!monitored);
        if (monitored) {
            ReferenceCountedUtils.add(this);
        }
    }

    public static void enableReferenceTracing() {
        ReferenceCountedUtils.enableReferenceTracing();
    }

    public static void disableReferenceTracing() {
        ReferenceCountedUtils.disableReferenceTracing();
    }

    public static void assertReferencesReleased() {
        ReferenceCountedUtils.assertReferencesReleased();
    }

    public static void unmonitor(ReferenceCounted counted) {
        ReferenceCountedUtils.unmonitor(counted);
    }

    @Override
    public int referenceId() {
        return referenceId;
    }

    @Override
    public StackTrace createdHere() {
        return referenceCounted.createdHere();
    }

    public void throwExceptionIfNotReleased() throws IllegalStateException {
        referenceCounted.throwExceptionIfNotReleased();
    }

    protected void backgroundPerformRelease() {
        BackgroundResourceReleaser.release(this);
    }

    void inThreadPerformRelease() {
        try {
            performRelease();
        } catch (Exception e) {
            Jvm.warn().on(getClass(), e);
        }
    }

    protected boolean canReleaseInBackground() {
        return false;
    }

    protected abstract void performRelease() throws IllegalStateException;

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        if ((WARN_COUNT < Integer.MAX_VALUE && referenceCounted.refCount() >= WARN_COUNT) && (referenceCounted.refCount() - WARN_COUNT) % 10 == 0)
            Jvm.warn().on(getClass(), "high reserve count for " + referenceName() +
                    " was " + referenceCounted.refCount(), new StackTrace("reserved here"));
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
    public boolean tryReserve(ReferenceOwner id) throws IllegalStateException, IllegalArgumentException {
        return referenceCounted.tryReserve(id);
    }

    @Override
    public void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        referenceCounted.reserveTransfer(from, to);
    }

    @Override
    public int refCount() {
        return referenceCounted.refCount();
    }

    @Override
    public void throwExceptionIfReleased() throws ClosedIllegalStateException {
        referenceCounted.throwExceptionIfReleased();
    }

    @Override
    public void warnAndReleaseIfNotReleased() throws ClosedIllegalStateException {
        referenceCounted.warnAndReleaseIfNotReleased();
    }

    @Deprecated(/* To be removed in 2.25 */)
    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        return referenceCounted.reservedBy(owner);
    }

    @Override
    public void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled) {
        this.singleThreadedCheckDisabled = singleThreadedCheckDisabled;
    }

    @Override
    public void addReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceCounted.addReferenceChangeListener(referenceChangeListener);
    }

    @Override
    public void removeReferenceChangeListener(ReferenceChangeListener referenceChangeListener) {
        referenceCounted.removeReferenceChangeListener(referenceChangeListener);
    }

    protected boolean threadSafetyCheck(boolean isUsed) throws IllegalStateException {
        // most common check, and sometimes the only check
        if (DISABLE_SINGLE_THREADED_CHECK || singleThreadedCheckDisabled)
            return true;
        return threadSafetyCheck0(isUsed);
    }

    private boolean threadSafetyCheck0(boolean isUsed) {
        // not so common but very cheap
        if (usedByThread == null && !isUsed)
            return true;

        Thread currentThread = Thread.currentThread();
        // when checking safety, this is the common case
        if (usedByThread == currentThread)
            return true;

        // very rare, only one first use after reset or a failure
        return threadSafetyCheck2(currentThread);
    }

    private boolean threadSafetyCheck2(Thread currentThread) {
        if (usedByThread == null || !usedByThread.isAlive()) {
            usedByThread = currentThread;
            usedByThreadHere = new StackTrace("Used here");
            return true;

        } else {
            final String message = getClass().getName() + " component which is not thread safe used by " + usedByThread + " and " + currentThread;
            throw new IllegalStateException(message, usedByThreadHere);
        }
    }

    public void singleThreadedCheckReset() {
        usedByThread = null;
    }

    /**
     * @deprecated Use singleThreadedCheckReset() instead
     */
    @Deprecated(/* to be removed in x.25 */)
    public void clearUsedByThread() {
        usedByThread = null;
    }

    @Override
    @NotNull
    public String toString() {
        return referenceName();
    }

    public void referenceCountedUnmonitored(boolean unmonitored) {
        referenceCounted.unmonitored(unmonitored);
    }
}