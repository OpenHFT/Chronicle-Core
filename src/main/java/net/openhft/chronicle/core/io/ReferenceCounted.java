package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

/**
 * A resource which is reference counted and freed when the refCount drop to 0.
 */
public interface ReferenceCounted extends ReferenceOwner {

    @NotNull
    static ReferenceCounted onReleased(final Runnable onRelease) {
        return onReleased(onRelease, false);
    }

    @NotNull
    static ReferenceCounted onReleased(final Runnable onRelease, boolean releaseOnOne) {
        return Jvm.isResourceTracing()
                ? new TracingReferenceCounted(onRelease, releaseOnOne)
                : new VanillaReferenceCounted(onRelease, releaseOnOne);
    }

    /**
     * Reserves a resource or throws an Exception.
     * <p>
     * Each invocation of this method increases the reference count by one.
     *
     * @param id unique id for this reserve
     * @throws IllegalStateException if the resource has already been freed.
     *                               I.e. its reference counter has as some point reached zero.
     */
    void reserve(ReferenceOwner id) throws IllegalStateException;

    default void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws IllegalStateException {
        reserve(to);
        release(from);
    }

    /**
     * Tries to reserve a resource and returns if the resource could
     * be successfully reserved.
     * <p>
     * Each invocation of this method increases the reference count by one.
     *
     * @param id unique id for this reserve
     * @throws IllegalStateException if the resource has already been freed.
     *                               I.e. its reference counter has as some point reached zero.
     */
    boolean tryReserve(ReferenceOwner id) throws IllegalStateException;

    /**
     * Releases a resource.
     * <p>
     * Each invocation of this method decreases the reference count by one.
     *
     * @param id unique id for the reserve to be released
     * @throws IllegalStateException if the resource has already been freed.
     *                               I.e. its reference counter has as some point reached zero.
     */
    void release(ReferenceOwner id) throws IllegalStateException;

    /**
     * Releases a resource and checks this is the last usage.
     * <p>
     * Each invocation of this method decreases the reference count by one.
     *
     * @param id unique id for the reserve to be released
     * @throws IllegalStateException if the resource has already been freed.
     *                               I.e. its reference counter has as some point reached zero.
     */
    void releaseLast(ReferenceOwner id) throws IllegalStateException;

    default void releaseLast() throws IllegalStateException {
        releaseLast(INIT);
    }

    /**
     * Returns the reference count for this resource.
     *
     * @return the reference count for this resource
     */
    int refCount();

    void throwExceptionBadResourceOwner() throws IllegalStateException;

    default void throwExceptionIfReleased() throws IllegalStateException {
        if (refCount() <= 0)
            throw new IllegalStateException("Released");
    }
}