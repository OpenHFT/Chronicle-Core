package net.openhft.chronicle.core.scoped;

import java.io.Closeable;

/**
 * A scoped resource, it is drawn from a "pool" of sorts, and will be returned
 * to that pool when {@link #close()} is called.
 * <p>
 * Do not keep a reference to the contained resource beyond the scope.
 *
 * @param <T> The type of the resource contained
 */
public interface ScopedResource<T> extends Closeable {

    /**
     * Get the contained resource
     *
     * @return The resource
     */
    T get();

    /**
     * Signifies the end of the scope, will return the resource to the "pool" for use by other acquirers
     */
    @Override
    void close();
}
