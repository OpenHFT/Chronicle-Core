package net.openhft.chronicle.core.util;

public interface Closeable extends java.io.Closeable {
    /**
     * Doesn't throw a checked exception.
     */
    void close();
}
