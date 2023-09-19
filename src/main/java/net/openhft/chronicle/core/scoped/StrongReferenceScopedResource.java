package net.openhft.chronicle.core.scoped;

import static net.openhft.chronicle.core.io.Closeable.closeQuietly;

/**
 * A {@link ScopedResource} that will always retain a strong reference to the
 * contained resource, even when not "in use"
 *
 * @param <T> The type of the contained resource
 */
public class StrongReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final T resource;

    StrongReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, T resource) {
        super(scopedThreadLocal);
        this.resource = resource;
    }

    public T get() {
        return resource;
    }

    @Override
    public void closeResource() {
        closeQuietly(resource);
    }

    @Override
    public Class<?> getType() {
        return resource.getClass();
    }
}

