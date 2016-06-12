package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.Jvm;

import java.util.function.Supplier;

/**
 * Represents a supplier of results which might throw an Exception
 * <p/>
 * <p>There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 * <p/>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #get()}.
 *
 * @param <V> the type of results supplied by this supplier
 * @param <T> the type of exception thrown by this supplier
 */
@FunctionalInterface
public interface ThrowingSupplier<V, T extends Throwable> {

    static <V, T extends Throwable> Supplier<V> asSupplier(ThrowingSupplier<V, T> throwingSupplier) {
        return () -> {
            try {
                return throwingSupplier.get();

            } catch (Throwable t) {
                throw Jvm.rethrow(t);
            }
        };
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    V get() throws T;
}