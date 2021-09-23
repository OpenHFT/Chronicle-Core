package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.util.ThrowingConsumer;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This will clean up a resource if the CleaningThread holding it dies.
 * <p>
 * Note: this will not clean up the resource if the ThreadLocal itself is discarded.
 */
public class CleaningThreadLocal<T> extends ThreadLocal<T> {
    private static final Set<CleaningThreadLocal> cleaningThreadLocals = Collections.synchronizedSet(new LinkedHashSet<>());

    private final Supplier<T> supplier;
    private final Function<T, T> getWrapper;
    private final ThrowingConsumer<T, Exception> cleanup;
    private Map<Thread, Object> nonCleaningThreadValues = null;

    CleaningThreadLocal(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup) {
        this(supplier, cleanup, Function.identity());
    }

    CleaningThreadLocal(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup, Function<T, T> getWrapper) {
        this.supplier = supplier;
        this.cleanup = cleanup;
        this.getWrapper = getWrapper;
        // only do this for testing.
        assert trackNonCleaningThreads();
    }

    public static <T> CleaningThreadLocal<T> withCloseQuietly(Supplier<T> supplier) {
        return new CleaningThreadLocal<>(supplier, Closeable::closeQuietly);
    }

    public static <T> CleaningThreadLocal<T> withCleanup(ThrowingConsumer<T, Exception> cleanup) {
        return new CleaningThreadLocal<>(() -> null, cleanup);
    }

    public static <T> CleaningThreadLocal<T> withCleanup(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup) {
        return new CleaningThreadLocal<>(supplier, cleanup);
    }

    // Used in VanillaSessionHandler
    public static <T> CleaningThreadLocal<T> withCleanup(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup, Function<T, T> getWrapper) {
        return new CleaningThreadLocal<>(supplier, cleanup, getWrapper);
    }

    public static void cleanupNonCleaningThreads() {
        if (cleaningThreadLocals.isEmpty())
            return;

        synchronized (cleaningThreadLocals) {
            for (Iterator<CleaningThreadLocal> iterator = cleaningThreadLocals.iterator(); iterator.hasNext(); ) {
                CleaningThreadLocal<?> nctl = iterator.next();
                for (Iterator<Map.Entry<Thread, Object>> iter = nctl.nonCleaningThreadValues.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry<Thread, Object> entry = iter.next();
                    if (!entry.getKey().isAlive()) {
                        ((CleaningThreadLocal) nctl).cleanup(entry.getValue());
                        iter.remove();
                    }
                }
                if (nctl.nonCleaningThreadValues.isEmpty())
                    iterator.remove();
            }
        }
    }

    private boolean trackNonCleaningThreads() {
        cleaningThreadLocals.add(this);
        nonCleaningThreadValues = Collections.synchronizedMap(new LinkedHashMap<>());
        return true;
    }

    @Override
    protected T initialValue() {
        final T t = supplier.get();
        if (nonCleaningThreadValues != null) {
            Thread thread = Thread.currentThread();
            if (thread instanceof CleaningThread)
                return t;
            nonCleaningThreadValues.put(thread, t);
        }
        return t;
    }

    @Override
    public T get() {
        return getWrapper.apply(super.get());
    }

    @Override
    public void set(T value) {
        final Thread thread = Thread.currentThread();
        if (thread instanceof CleaningThread) {
            CleaningThread.performCleanup(thread, this);
        } else if (nonCleaningThreadValues != null) {
            final T o = (T) nonCleaningThreadValues.put(thread, value);
            cleanup(o);
        }
        super.set(value);
    }

    @Override
    public void remove() {
        final Thread thread = Thread.currentThread();
        if (thread instanceof CleaningThread) {
            CleaningThread.performCleanup(thread, this);
        } else if (nonCleaningThreadValues != null) {
            final T o = (T) nonCleaningThreadValues.remove(thread);
            cleanup(o);
        }
        super.remove();
    }

    /**
     * Cleanup. Can safely be called more than once - will only perform cleanup the first time.
     *
     * @param value value to clean up for
     */
    public synchronized void cleanup(T value) {
        try {
            ThrowingConsumer<T, Exception> cleanup = this.cleanup;
            if (cleanup != null && value != null)
                cleanup.accept(value);
        } catch (Exception e) {
            Jvm.warn().on(getClass(), "Exception cleaning up " + value.getClass(), e);
        }
    }
}
