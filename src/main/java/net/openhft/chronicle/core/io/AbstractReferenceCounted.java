package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;
import net.openhft.chronicle.core.util.WeakIdentityHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

import static net.openhft.chronicle.core.io.AbstractCloseable.*;
import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;

public abstract class AbstractReferenceCounted implements ReferenceCountedTracer, ReferenceOwner {
    protected static final long WARN_NS = (long) (Jvm.getDouble("reference.warn.secs", 0.003) * 1e9);
    protected static final int WARN_COUNT = Integer.getInteger("reference.warn.count", Integer.MAX_VALUE);
    static volatile Set<AbstractReferenceCounted> REFERENCE_COUNTED_SET;
    private transient volatile Thread usedByThread;
    protected transient final MonitorReferenceCounted referenceCounted;
    private final int referenceId;

    protected AbstractReferenceCounted() {
        this(true);
    }

    protected AbstractReferenceCounted(boolean monitored) {
        Runnable performRelease = BG_RELEASER && canReleaseInBackground()
                ? this::backgroundPerformRelease
                : this::inThreadPerformRelease;
        referenceId = IOTools.counter(getClass()).incrementAndGet();
        referenceCounted = (MonitorReferenceCounted) ReferenceCountedTracer.onReleased(performRelease, referenceName(), getClass());
        referenceCounted.unmonitored(!monitored);
        Set<AbstractReferenceCounted> set = REFERENCE_COUNTED_SET;
        if (monitored && set != null)
            set.add(this);
    }

    public static void enableReferenceTracing() {
        enableCloseableTracing();
        REFERENCE_COUNTED_SET =
                Collections.newSetFromMap(
                        new WeakIdentityHashMap<>());
    }

    public static void disableReferenceTracing() {
        disableCloseableTracing();
        REFERENCE_COUNTED_SET = null;
    }

    public static void assertReferencesReleased() {
        Set<AbstractReferenceCounted> traceSet = REFERENCE_COUNTED_SET;
        if (traceSet == null) {
            Jvm.warn().on(AbstractReferenceCounted.class, "Reference tracing disabled");
            return;
        }

        assertCloseablesClosed();

        AssertionError openFiles = new AssertionError("Reference counted not released");
        synchronized (traceSet) {
            for (AbstractReferenceCounted key : traceSet) {
                if (key == null || key.refCount() == 0)
                    continue;

                try {
                    key.throwExceptionIfNotReleased();
                } catch (Exception e) {
                    openFiles.addSuppressed(e);
                }
            }
        }
        if (openFiles.getSuppressed().length > 0)
            throw openFiles;
    }

    public static void unmonitor(ReferenceCounted counted) {
        if (REFERENCE_COUNTED_SET != null)
            REFERENCE_COUNTED_SET.remove(counted);
        if (counted instanceof AbstractReferenceCounted)
            ((AbstractReferenceCounted) counted).referenceCounted.unmonitored(true);
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
        long start = System.nanoTime();
        try {
            performRelease();
        } catch (Exception e) {
            Jvm.warn().on(getClass(), e);
        }
        long time = System.nanoTime() - start;
        if (time >= WARN_NS)
            Slf4jExceptionHandler.PERF.on(getClass(), "Took " + time / 100_000 / 10.0 + " ms to performRelease");
    }

    protected boolean canReleaseInBackground() {
        return false;
    }

    protected abstract void performRelease() throws IllegalStateException;

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
        if (WARN_COUNT < Integer.MAX_VALUE && referenceCounted.refCount() >= WARN_COUNT)
            if ((referenceCounted.refCount() - WARN_COUNT) % 10 == 0)
                Jvm.warn().on(getClass(), "high reserve count for " + referenceName() + " was " + referenceCounted.refCount(), new StackTrace("reserved here"));
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

    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        return referenceCounted.reservedBy(owner);
    }

    protected boolean threadSafetyCheck(boolean isUsed) throws IllegalStateException {
        if (DISABLE_THREAD_SAFETY)
            return true;
        if (usedByThread == null && !isUsed)
            return true;

        Thread currentThread = Thread.currentThread();
        if (usedByThread == null || !usedByThread.isAlive()) {
            usedByThread = currentThread;
        } else if (usedByThread != currentThread) {
            throw new IllegalStateException(getClass().getName() + " component which is not thread safes used by " + usedByThread + " and " + currentThread);
        }
        return true;
    }

    public void clearUsedByThread() {
        usedByThread = null;
    }

    @Override
    @NotNull
    public String toString() {
        return referenceName();
    }
}