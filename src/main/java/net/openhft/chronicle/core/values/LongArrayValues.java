/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.values;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.ClosedIllegalStateException;
import net.openhft.chronicle.core.io.ThreadingIllegalStateException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * This interface represents a reference to an array of long values. It provides methods to get, set, and manipulate the values.
 * Implementations of this interface are expected to handle the storage and retrieval of the long array.
 * <p>
 * Implementations may store the array in different formats or mediums. For example, the array could be stored
 * in binary format or text format, in memory or on disk.
 * <p>
 * The getValueAt and getVolatileValueAt methods retrieve the long value at a specified index. The difference between these methods
 * is that getVolatileValueAt uses volatile semantics, meaning it includes a memory barrier which ensures that
 * subsequent reads and writes are not reordered beyond this point.
 * <p>
 * The setValueAt and setOrderedValueAt methods set the long value at a specified index. The difference between these methods is that
 * setOrderedValueAt uses ordered semantics, meaning it includes a memory barrier which ensures that previous
 * writes are not reordered beyond this point.
 * <p>
 * The compareAndSet method atomically sets the value at a specified index to the given updated value if the current value equals the expected value.
 * <p>
 * The bindValueAt method binds a LongValue to a specified index in the array.
 *
 * @see net.openhft.chronicle.core.values.BooleanValue
 * @see net.openhft.chronicle.core.values.ByteValue
 * @see net.openhft.chronicle.core.values.CharValue
 * @see net.openhft.chronicle.core.values.DoubleValue
 * @see net.openhft.chronicle.core.values.FloatValue
 * @see net.openhft.chronicle.core.values.IntValue
 * @see net.openhft.chronicle.core.values.LongValue
 * @see net.openhft.chronicle.core.values.ShortValue
 * @see net.openhft.chronicle.core.values.StringValue
 * @see net.openhft.chronicle.core.values.LongArrayValues
 * @see net.openhft.chronicle.core.values.IntArrayValues
 * @see net.openhft.chronicle.core.values.UnsetLongValue
 * @since 10/10/13
 */
public interface LongArrayValues extends Closeable {
    /**
     * Retrieves the capacity of the array.
     *
     * @return the capacity of the array
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    long getCapacity() throws IllegalStateException;

    /**
     * Retrieves the number of used elements in the array.
     *
     * @return the number of used elements in the array
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferUnderflowException       If the array is empty
     */
    long getUsed() throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the number of used elements in the array.
     *
     * @param used the number of used elements in the array
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferUnderflowException       If the array is empty
     */
    void setUsed(long used) throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the maximum number of used elements in the array.
     *
     * @param usedAtLeast the minimum number of used elements in the array
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferUnderflowException       If the array is empty
     */
    void setMaxUsed(long usedAtLeast) throws IllegalStateException, BufferUnderflowException;

    /**
     * Retrieves the long value at a specified index.
     *
     * @param index the index of the long value to retrieve
     * @return the long value at the specified index
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferUnderflowException       If the index is out of bounds
     */
    long getValueAt(long index) throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the long value at a specified index.
     *
     * @param index the index at which to set the long value
     * @param value the long value to set
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferOverflowException        If the index is out of bounds
     */
    void setValueAt(long index, long value) throws IllegalStateException, BufferOverflowException;

    /**
     * Retrieves the long value at a specified index using volatile semantics.
     *
     * @param index the index of the long value to retrieve
     * @return the long value at the specified index
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferUnderflowException       If the index is out of bounds
     */
    long getVolatileValueAt(long index) throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the long value at a specified index using ordered semantics.
     *
     * @param index the index at which to set the long value
     * @param value the long value to set
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferOverflowException        If the index is out of bounds
     */
    void setOrderedValueAt(long index, long value) throws IllegalStateException, BufferOverflowException;

    /**
     * Atomically sets the value at a specified index to the given updated value if the current value equals the expected value.
     *
     * @param index    the index at which to set the long value
     * @param expected the expected current value
     * @param value    the new value
     * @return true if successful, false otherwise
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferOverflowException        If the index is out of bounds
     */
    boolean compareAndSet(long index, long expected, long value) throws IllegalStateException, BufferOverflowException;

    /**
     * Binds a LongValue to a specified index in the array.
     *
     * @param index the index at which to bind the LongValue
     * @param value the LongValue to bind
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     * @throws BufferOverflowException        If the index is out of bounds
     */
    void bindValueAt(long index, LongValue value) throws IllegalStateException, BufferOverflowException;

    /**
     * Calculates the size in bytes of an array with the specified capacity.
     *
     * @param capacity the capacity of the array
     * @return the size in bytes of the array
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    long sizeInBytes(long capacity) throws IllegalStateException;

    /**
     * Checks if the array is null.
     *
     * @return true if the array is null, false otherwise
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    boolean isNull() throws IllegalStateException;

    /**
     * Resets the array, clearing all values.
     *
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void reset() throws IllegalStateException;
}
