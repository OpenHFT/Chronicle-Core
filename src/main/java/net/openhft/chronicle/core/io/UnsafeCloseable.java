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

import net.openhft.chronicle.core.UnsafeMemory;
import sun.misc.Unsafe;

/**
 * An abstract base class for resources that use the {@link Unsafe} class for low-level memory operations.
 * <p>
 * This class provides methods for manipulating long values stored at a specific memory address using
 * the {@link Unsafe} API. It extends {@link AbstractCloseable} to handle resource closure and cleanup,
 * ensuring proper resource management.
 * </p>
 */
public abstract class UnsafeCloseable extends AbstractCloseable {

    // Memory address managed by this resource
    protected long address;

    // Unsafe instance for performing low-level memory operations
    protected Unsafe unsafe = null;

    /**
     * Constructs a new {@code UnsafeCloseable} instance.
     * <p>
     * This constructor disables the single-threaded check for thread safety by default.
     * </p>
     */
    protected UnsafeCloseable() {
        singleThreadedCheckDisabled(true);
    }

    /**
     * Sets the memory address for this resource and initializes the unsafe instance.
     *
     * @param address The memory address.
     */
    protected void address(long address) {
        this.address = address;
        unsafe = UnsafeMemory.UNSAFE;
    }

    /**
     * Performs cleanup actions when the resource is closed.
     * <p>
     * This method clears the {@code unsafe} instance to release the associated resources.
     * </p>
     *
     * @throws IllegalStateException If an error occurs during resource closure.
     */
    @Override
    protected void performClose() throws IllegalStateException {
        unsafe = null;
    }

    /**
     * Gets the long value stored at the memory address.
     *
     * @return The long value stored at the memory address.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public long getLong() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            return unsafe.getLong(address);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    /**
     * Sets the long value at the memory address.
     *
     * @param value The long value to set at the memory address.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public void setLong(long value) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            unsafe.putLong(address, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    /**
     * Gets the volatile long value stored at the memory address.
     *
     * @return The volatile long value stored at the memory address.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public long getVolatileLong() throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            return unsafe.getLongVolatile(null, address);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    /**
     * Sets the volatile long value at the memory address.
     *
     * @param value The volatile long value to set at the memory address.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public void setVolatileLong(long value) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            unsafe.putLongVolatile(null, address, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    /**
     * Gets the volatile long value stored at the memory address, or a default value if the resource is closed.
     *
     * @param closedLong The default value to return if the resource is closed.
     * @return The volatile long value stored at the memory address, or the default value if closed.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     */
    public long getVolatileLong(long closedLong) throws ClosedIllegalStateException {
        if (isClosed())
            return closedLong;
        try {
            return getVolatileLong();
        } catch (Exception e) {
            return closedLong;
        }
    }

    /**
     * Sets the ordered long value at the memory address.
     *
     * @param value The ordered long value to set at the memory address.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public void setOrderedLong(long value) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            unsafe.putOrderedLong(null, address, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    /**
     * Adds the specified value to the long value stored at the memory address and returns the updated value.
     *
     * @param delta The value to add.
     * @return The updated long value after adding the specified delta.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public long addLong(long delta) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            return unsafe.getAndAddLong(null, address, delta) + delta;
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    /**
     * Adds the specified value to the long value stored at the memory address and returns the updated value.
     * <p>
     * This method is equivalent to {@link #addLong(long)}.
     * </p>
     *
     * @param delta The value to add.
     * @return The updated long value after adding the specified delta.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public long addAtomicLong(long delta) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            return addLong(delta);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    /**
     * Compares the value at the memory address with the expected value and, if they match, sets the value to a new value.
     *
     * @param expected The expected value currently stored at the memory address.
     * @param value    The new value to set if the expected value matches the current value.
     * @return {@code true} if the swap was successful, {@code false} otherwise.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If the method is used in a non-thread-safe way.
     */
    public boolean compareAndSwapLong(long expected, long value) throws ClosedIllegalStateException, ThreadingIllegalStateException {
        try {
            return unsafe.compareAndSwapLong(null, address, expected, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }
}
