package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.util.ThrowingConsumer;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This will clean up a resource if the CleaningThread holding it dies.
 * <p>
 * Note: this will not clean up the resource if the ThreadLocal itself is discarded.
 */
public class CleaningThreadLocal<T> extends ThreadLocal<T> {
    private final Supplier<T> supplier;
    private final Function<T, T> getWrapper;
    private ThrowingConsumer<T, Exception> cleanup;

    CleaningThreadLocal(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup) {
        this(supplier, cleanup, Function.identity());
    }

    CleaningThreadLocal(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup, Function<T, T> getWrapper) {
        this.supplier = supplier;
        this.cleanup = cleanup;
        this.getWrapper = getWrapper;
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

    @Override
    protected T initialValue() {
        return supplier.get();
    }

    @Override
    public T get() {
        return getWrapper.apply(super.get());
    }

    /**
     * Cleanup. Can safely be called more than once - will only perform cleanup the first time.
     * @param value value to clean up for
     */
    public synchronized void cleanup(T value) {
        try {
            ThrowingConsumer<T, Exception> cleanup = this.cleanup;
            if (cleanup != null)
                cleanup.accept(value);
            this.cleanup = null;
        } catch (Exception e) {
            Jvm.warn().on(getClass(), "Exception cleaning up " + value.getClass(), e);
        }
    }
}
