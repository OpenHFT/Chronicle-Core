package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.util.ThrowingConsumer;

import java.util.function.Supplier;

/**
 * This will clean up a resource if the CleaningThread holding it dies.
 * <p>
 * Note: this will not clean up the resource if the ThreadLocal itself is discarded.
 */
public class CleaningThreadLocal<T> extends ThreadLocal<T> {
    private final Supplier<T> supplier;
    private final ThrowingConsumer<T, Exception> cleanup;

    CleaningThreadLocal(Supplier<T> supplier, ThrowingConsumer<T, Exception> cleanup) {
        this.supplier = supplier;
        this.cleanup = cleanup;
    }

    public static <T> CleaningThreadLocal<T> withCloseQuietly() {
        return new CleaningThreadLocal<>(() -> null, Closeable::closeQuietly);
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

    @Override
    protected T initialValue() {
        return supplier.get();
    }

    public void cleanup(T value) {
        try {
            cleanup.accept(value);
        } catch (Exception e) {
            Jvm.warn().on(getClass(), "Exception cleaning up " + value.getClass(), e);
        }
    }
}
