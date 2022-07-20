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
        final Set<AbstractReferenceCounted> set = referenceCountedSet;
        if (monitored && set != null) {
            synchronized (set) {
                set.add(this);
            }
        }
    }

    public static void enableReferenceTracing() {
        enableCloseableTracing();
        referenceCountedSet =
                Collections.newSetFromMap(
                        new WeakIdentityHashMap<>());
    }

    public static void disableReferenceTracing() {
        disableCloseableTracing();
        referenceCountedSet = null;
    }

    public static void assertReferencesReleased() {
        final Set<AbstractReferenceCounted> traceSet = referenceCountedSet;
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
        final Set<AbstractReferenceCounted> set = referenceCountedSet;
        if (counted instanceof AbstractReferenceCounted) {
            if (set != null) {
                synchronized (set) {
                    // The set contains <AbstractReferenceCounted> so, "counted" must be an instance of that
                    // for remove to have any effect.
                    set.remove(counted);
                }
            }
            ((AbstractReferenceCounted) counted).referenceCounted.unmonitored(true);
        }
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

    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        return referenceCounted.reservedBy(owner);
    }

    public void singleThreadedCheckDisabled(boolean singleThreadedCheckDisabled) {
        this.singleThreadedCheckDisabled = singleThreadedCheckDisabled;
    }

    protected boolean threadSafetyCheck(boolean isUsed) throws IllegalStateException {
        if (DISABLE_SINGLE_THREADED_CHECK || singleThreadedCheckDisabled)
            return true;
        if (usedByThread == null && !isUsed)
            return true;

        return threadSafetyCheck0();
    }

    private boolean threadSafetyCheck0() {
        Thread currentThread = Thread.currentThread();
        if (usedByThread == null || !usedByThread.isAlive()) {
            usedByThread = currentThread;
            usedByThreadHere = new StackTrace("Used here");
        } else if (usedByThread != currentThread) {
            final String message = getClass().getName() + " component which is not thread safe used by " + usedByThread + " and " + currentThread;
            throw new IllegalStateException(message, usedByThreadHere);
        }
        return true;
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
}