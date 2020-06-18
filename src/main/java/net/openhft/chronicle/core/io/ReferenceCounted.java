package net.openhft.chronicle.core.io;

/**
 * A resource which is reference counted and freed when the refCount drop to 0.
 */
public interface ReferenceCounted extends ReferenceOwner {

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
     * Best effort check the owner has reserved it. Returns true if not sure.
     *
     * @param owner to check
     * @return false if the owner definitely doesn't own it.
     */
    boolean reservedBy(ReferenceOwner owner);

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
}