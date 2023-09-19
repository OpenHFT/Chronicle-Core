package net.openhft.chronicle.core.scoped;

import org.jetbrains.annotations.Nullable;

abstract class AbstractScopedResource<T> implements ScopedResource<T> {

    private final long createdTimeNanos;
    private final ScopedThreadLocal<T> scopedThreadLocal;

    protected AbstractScopedResource(ScopedThreadLocal<T> scopedThreadLocal) {
        this.scopedThreadLocal = scopedThreadLocal;
        this.createdTimeNanos = System.nanoTime();
    }

    @Override
    public void close() {
        scopedThreadLocal.returnResource(this);
    }

    /**
     * Do anything that needs to be done before returning a resource to a caller
     */
    void preAcquire() {
        // Do nothing by default
    }

    /**
     * Close the contained resource and clear any references
     */
    abstract void closeResource();

    /**
     * The time this resource was created
     *
     * @return the {@link System#nanoTime()} of creation
     */
    public long getCreatedTimeNanos() {
        return createdTimeNanos;
    }

    /**
     * Get the type of object contained, may return null
     *
     * @return the type of object contained, or null if it can't be determined
     */
    @Nullable
    public abstract Class<?> getType();
}
