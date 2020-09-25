package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;
import net.openhft.chronicle.core.onoes.Slf4jExceptionHandler;
import net.openhft.chronicle.core.util.WeakIdentityHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

import static net.openhft.chronicle.core.io.BackgroundResourceReleaser.BG_RELEASER;

public abstract class AbstractReferenceCounted implements ReferenceCountedTracer, ReferenceOwner {
    static volatile Set<AbstractReferenceCounted> REFERENCE_COUNTED_SET;
    protected static final long WARN_NS = (long) (Jvm.getDouble("reference.warn.secs", 0.003) * 1e9);

    private transient volatile Thread usedByThread;
    private transient final ReferenceCountedTracer referenceCounted;
    private final int referenceId;

    protected AbstractReferenceCounted() {
        this(true);
    }

    protected AbstractReferenceCounted(boolean monitored) {
        Runnable performRelease = BG_RELEASER && performReleaseInBackground()
                ? this::backgroundPerformRelease
                : this::inThreadPerformRelease;
        referenceId = IOTools.counter(getClass()).incrementAndGet();
        referenceCounted = ReferenceCountedTracer.onReleased(performRelease, referenceName());

        Set<AbstractReferenceCounted> set = REFERENCE_COUNTED_SET;
        if (monitored && set != null)
            set.add(this);
    }

    public static void enableReferenceTracing() {
        AbstractCloseable.enableCloseableTracing();
        REFERENCE_COUNTED_SET =
                Collections.newSetFromMap(
                        new WeakIdentityHashMap<>());
    }

    public static void disableReferenceTracing() {
        AbstractCloseable.disableCloseableTracing();
        REFERENCE_COUNTED_SET = null;
    }

    public static void assertReferencesReleased() {
        Set<AbstractReferenceCounted> traceSet = REFERENCE_COUNTED_SET;
        if (traceSet == null) {
            Jvm.warn().on(AbstractReferenceCounted.class, "reference tracing disabled");
            return;
        }

        AbstractCloseable.assertCloseablesClosed();

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
    }

    @Override
    public int referenceId() {
        return referenceId;
    }

    @Override
    public StackTrace createdHere() {
        return referenceCounted.createdHere();
    }

    public void throwExceptionIfNotReleased() {
        referenceCounted.throwExceptionIfNotReleased();
    }

    void backgroundPerformRelease() {
        BackgroundResourceReleaser.release(this);
    }

    void inThreadPerformRelease() {
        long start = System.nanoTime();
        performRelease();
        long time = System.nanoTime() - start;
        if (time >= WARN_NS)
            Slf4jExceptionHandler.WARN.on(getClass(), "Took " + time / 100_000 / 10.0 + " ms to performRelease");
    }

    protected boolean performReleaseInBackground() {
        return false;
    }

    protected abstract void performRelease();

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {
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
    public void throwExceptionIfReleased() throws IllegalStateException {
        referenceCounted.throwExceptionIfReleased();
    }

    @Override
    public void warnAndReleaseIfNotReleased() {
        referenceCounted.warnAndReleaseIfNotReleased();
    }

    public boolean reservedBy(ReferenceOwner owner) {
        return referenceCounted.reservedBy(owner);
    }

    protected boolean threadSafetyCheck(boolean isUsed) {
        if (!AbstractCloseable.CHECK_THREAD_SAFETY)
            return true;
        if (usedByThread == null && !isUsed)
            return true;

        Thread currentThread = Thread.currentThread();
        if (usedByThread == null || !usedByThread.isAlive()) {
            usedByThread = currentThread;
        } else if (usedByThread != currentThread) {
            throw new IllegalStateException("Component which is not thread safes used by " + usedByThread + " and " + currentThread);
        }
        return true;
    }

    @Override
    @NotNull
    public String toString() {
        return referenceName();
    }
}