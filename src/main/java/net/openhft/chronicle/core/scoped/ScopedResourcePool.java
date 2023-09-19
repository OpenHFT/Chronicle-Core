package net.openhft.chronicle.core.scoped;

/**
 * A pool of resources, whereby a lease is taken on an instance
 * by calling {@link #get()} and that lease is relinquished when
 * {@link ScopedResource#close()} is called.
 * <p>
 * Example of use:
 * <pre>{@code
 *   try (ScopedResource<Wire> sharedWire = sharedWireScopedThreadLocal.get()) {
 *     Wire wire = sharedWire.get();
 *     // ... do something with the wire
 *   } // it is returned for use by inner scopes here
 * }</pre>
 *
 * @param <T> The type of object contained in the pool
 */
public interface ScopedResourcePool<T> {

    /**
     * Get a scoped instance of the shared resource
     *
     * @return the {@link ScopedResource}, to be closed once it is finished being used
     */
    ScopedResource<T> get();
}
