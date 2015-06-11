package net.openhft.chronicle.core.util;

@FunctionalInterface
public interface ThrowingSupplier<O, T extends Throwable> {
    O get() throws T;
}
