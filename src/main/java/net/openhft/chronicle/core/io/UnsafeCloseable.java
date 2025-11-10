//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.UnsafeMemory;
import sun.misc.Unsafe;

/**
 * Convenience base for memory backed resources using {@link Unsafe}.
 * Not thread-safe. The memory address is set via {@link #address(long)} and the
 * clean-up is performed in {@link #performClose()}.
 */
public abstract class UnsafeCloseable extends AbstractCloseable {

    protected long address;
    protected Unsafe unsafe = null;

    /**
     * Disable the single-threaded check because these resources are typically
     * accessed from multiple threads.
     */
    @SuppressWarnings("this-escape")
    protected UnsafeCloseable() {
        singleThreadedCheckDisabled(true);
    }

    /**
     * Assign the backing memory address.
     */
    protected void address(long address) {
        this.address = address;
        unsafe = UnsafeMemory.UNSAFE;
    }

    @Override
    protected void performClose() throws IllegalStateException {
        unsafe = null;
    }

    /**
     * Gets the long value stored at the memory address.
     *
     * @return The long value.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * @param value The long value to set.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * @return The volatile long value.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * @param value The volatile long value to set.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * @return The volatile long value or the default value if closed.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * @param value The ordered long value to set.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * @return The updated value.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * This method is equivalent to {@link #addLong(long)}.
     *
     * @param delta The value to add.
     * @return The updated value.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
     * @param expected The expected value.
     * @param value    The new value to set.
     * @return {@code true} if the swap was successful, {@code false} otherwise.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If used in a non thread safe way
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
