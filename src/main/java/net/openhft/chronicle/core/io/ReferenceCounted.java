/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.io;

/**
 * Represents a resource that is reference counted. The resource is freed when the reference count drops to 0.
 * This can be used for efficiently managing resources, such as memory buffers or file handles,
 * by ensuring that they are not released until they are no longer in use.
 */
public interface ReferenceCounted extends ReferenceOwner {

    /**
     * Reserves the resource by incrementing its reference count by one.
     * <p>
     * It is required to reserve a resource before using it to prevent it from being freed.
     *
     * @param id The unique identifier representing the owner reserving the resource.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     *                                        I.e. its reference counter has as some point reached zero.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void reserve(ReferenceOwner id) throws ClosedIllegalStateException, ThreadingIllegalStateException;

    /**
     * Atomically transfers a reservation from one owner to another by incrementing the reference
     * count for the new owner and decrementing it for the old owner.
     *
     * @param from The unique identifier representing the owner releasing the reservation.
     * @param to   The unique identifier representing the owner acquiring the reservation.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    // TODO move implementation to sub-classes in x.24
    default void reserveTransfer(ReferenceOwner from, ReferenceOwner to) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        reserve(to);
        release(from);
    }

    /**
     * Attempts to reserve the resource and returns {@code true} if successful.
     * Unlike {@link #reserve(ReferenceOwner)}, this method will not throw an exception if the resource
     * is already freed.
     *
     * @param id The unique identifier representing the owner attempting to reserve the resource.
     * @return {@code true} if the resource was successfully reserved, {@code false} otherwise.
     * @throws IllegalArgumentException       If the reference owner is invalid.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    boolean tryReserve(ReferenceOwner id) throws ClosedIllegalStateException, IllegalArgumentException;

    /**
     * Releases the resource by decrementing its reference count by one.
     * When the reference count reaches zero, the resource is freed.
     *
     * @param id The unique identifier representing the owner releasing the resource.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     *                                        I.e. its reference counter has as some point reached zero.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void release(ReferenceOwner id) throws ClosedIllegalStateException;

    /**
     * Releases the resource and ensures that this release is the last usage of the resource.
     * The reference count is decremented and checked to be zero after this release.
     *
     * @param id The unique identifier representing the owner releasing the resource.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void releaseLast(ReferenceOwner id) throws ClosedIllegalStateException;

    /**
     * Releases the resource for the initial owner and ensures that this release is the last usage of the resource.
     * The reference count is decremented and checked to be zero after this release.
     *
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    default void releaseLast() throws ClosedIllegalStateException {
        releaseLast(INIT);
    }

    /**
     * Returns the current reference count of the resource. The reference count indicates
     * the number of owners currently holding reservations to the resource.
     *
     * @return The current reference count of the resource.
     */
    int refCount();

    /**
     * Adds a {@link ReferenceChangeListener} that will be notified whenever the reference count changes.
     * This can be used to monitor the usage of the resource and execute code when certain conditions are met.
     *
     * @param referenceChangeListener The listener that will receive notifications of reference count changes.
     */
    void addReferenceChangeListener(ReferenceChangeListener referenceChangeListener);

    /**
     * Removes a {@link ReferenceChangeListener} previously added via {@link #addReferenceChangeListener(ReferenceChangeListener)}.
     * <p>
     * Note: Object equality is used to determine which listener to remove, so be cautious if the listener
     * implements the equals method in a non-standard way.
     *
     * @param referenceChangeListener The listener to remove.
     */
    void removeReferenceChangeListener(ReferenceChangeListener referenceChangeListener);
}