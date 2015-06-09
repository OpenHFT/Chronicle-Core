package net.openhft.chronicle.core.util;

/**
 * Created by peter on 13/05/15.
 */
@FunctionalInterface
public interface ThrowingSupplier<O, T extends Throwable> {
    O get() throws T;
}
