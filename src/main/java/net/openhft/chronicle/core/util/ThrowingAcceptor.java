package net.openhft.chronicle.core.util;

@FunctionalInterface
public interface ThrowingAcceptor<I, T extends Throwable> {
    void accept(I in) throws T;
}
