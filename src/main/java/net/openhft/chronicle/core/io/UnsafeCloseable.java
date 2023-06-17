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
 * Provides methods for manipulating long values stored at a specific memory address.
 */
public abstract class UnsafeCloseable extends AbstractCloseable {

    protected long address;
    protected Unsafe unsafe = null;

    /**
     * Constructs a new UnsafeCloseable instance.
     * Disables the single-threaded check for thread safety.
     */
    protected UnsafeCloseable() {
        singleThreadedCheckDisabled(true);
    }

    /**
     * Sets the memory address for this resource.
     *
     * @param address The memory address.
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
     * @throws IllegalStateException If the resource is closed.
     */
    public long getLong() throws IllegalStateException {
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
     * @throws IllegalStateException If the resource is closed.
     */
    public void setLong(long value) throws IllegalStateException {
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
     * @throws IllegalStateException If the resource is closed.
     */
    public long getVolatileLong() throws IllegalStateException {
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
     * @throws IllegalStateException If the resource is closed.
     */
    public void setVolatileLong(long value) throws IllegalStateException {
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
     */
    public long getVolatileLong(long closedLong) {
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
     * @throws IllegalStateException If the resource is closed.
     */
    public void setOrderedLong(long value) throws IllegalStateException {
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
     * @throws IllegalStateException If the resource is closed.
     */
    public long addLong(long delta) throws IllegalStateException {
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
     * @throws IllegalStateException If the resource is closed.
     */
    public long addAtomicLong(long delta) throws IllegalStateException {
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
     * @throws IllegalStateException If the resource is closed.
     */
    public boolean compareAndSwapLong(long expected, long value) throws IllegalStateException {
        try {
            return unsafe.compareAndSwapLong(null, address, expected, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }
}
