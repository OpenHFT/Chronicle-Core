package net.openhft.chronicle.core.util;

/**
 * Created by peter on 19/05/15.
 */
public interface Closeable extends java.io.Closeable {
    /**
     * Doesn't throw a checked exception.
     */
    void close();
}
