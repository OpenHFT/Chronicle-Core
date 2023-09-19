package net.openhft.chronicle.core.scoped;

import net.openhft.chronicle.core.io.Closeable;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * A weak-referenced {@link ScopedResource} when the resource is acquired, we create
 * a strong reference to prevent it being GC'd while it's "in use". Upon return, the
 * strong reference is cleared, leaving only the weak reference thus allowing the
 * resource to be GC'd.
 *
 * @param <T> The type of the contained resource
 */
public class WeakReferenceScopedResource<T> extends AbstractScopedResource<T> {

    private final Supplier<T> supplier;
    private WeakReference<T> ref;
    private T strongRef;

    public WeakReferenceScopedResource(ScopedThreadLocal<T> scopedThreadLocal, Supplier<T> supplier) {
        super(scopedThreadLocal);
        this.supplier = supplier;
    }

    /**
     * Before acquire we check that the reference is populated and populate it if not
     */
    @Override
    void preAcquire() {
        if (ref == null || ((strongRef = ref.get()) == null)) {
            strongRef = supplier.get();
            ref = new WeakReference<>(strongRef);
        }
    }

    @Override
    public T get() {
        return strongRef;
    }

    @Override
    public void close() {
        strongRef = null;
        super.close();
    }

    @Override
    public void closeResource() {
        if (ref != null) {
            Closeable.closeQuietly(ref.get());
            ref.clear();
            ref = null;
        }
    }

    @Override
    public @Nullable Class<?> getType() {
        T val = ref != null ? ref.get() : null;
        return val != null ? val.getClass() : null;
    }
}
