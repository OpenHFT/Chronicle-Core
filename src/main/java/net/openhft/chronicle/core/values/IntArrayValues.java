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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * This interface represents a reference to an array of int values. It provides methods to get, set, and manipulate the values.
 * Implementations of this interface are expected to handle the storage and retrieval of the int array.
 * <p>
 * Implementations may store the array in different formats or mediums. For example, the array could be stored
 * in binary format or text format, in memory or on disk.
 * <p>
 * The getValueAt and getVolatileValueAt methods retrieve the int value at a specified index. The difference between these methods
 * is that getVolatileValueAt uses volatile semantics, meaning it includes a memory barrier which ensures that
 * subsequent reads and writes are not reordered beyond this point.
 * <p>
 * The setValueAt and setOrderedValueAt methods set the int value at a specified index. The difference between these methods is that
 * setOrderedValueAt uses ordered semantics, meaning it includes a memory barrier which ensures that previous
 * writes are not reordered beyond this point.
 * <p>
 * The compareAndSet method atomically sets the value at a specified index to the given updated value if the current value equals the expected value.
 * <p>
 * The bindValueAt method binds an IntValue to a specified index in the array.
 *
 * @author Peter Lawrey
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
public interface IntArrayValues extends Closeable {
    /**
     * Retrieves the capacity of the array.
     *
     * @return the capacity of the array
     * @throws IllegalStateException if the array is closed
     */
    long getCapacity() throws IllegalStateException;

    /**
     * Retrieves the number of used elements in the array.
     *
     * @return the number of used elements in the array
     * @throws IllegalStateException    if the array is closed
     * @throws BufferUnderflowException if the array is empty
     */
    long getUsed() throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the maximum number of used elements in the array.
     *
     * @param usedAtLeast the minimum number of used elements in the array
     * @throws IllegalStateException    if the array is closed
     * @throws BufferUnderflowException if the array is empty
     */
    void setMaxUsed(long usedAtLeast) throws IllegalStateException, BufferUnderflowException;

    /**
     * Retrieves the int value at a specified index.
     *
     * @param index the index of the int value to retrieve
     * @return the int value at the specified index
     * @throws IllegalStateException    if the array is closed
     * @throws BufferUnderflowException if the index is out of bounds
     */
    int getValueAt(long index) throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the int value at a specified index.
     *
     * @param index the index at which to set the int value
     * @param value the int value to set
     * @throws IllegalStateException   if the array is closed
     * @throws BufferOverflowException if the index is out of bounds
     */
    void setValueAt(long index, int value) throws IllegalStateException, BufferOverflowException;

    /**
     * Retrieves the int value at a specified index using volatile semantics.
     *
     * @param index the index of the int value to retrieve
     * @return the int value at the specified index
     * @throws IllegalStateException    if the array is closed
     * @throws BufferUnderflowException if the index is out of bounds
     */
    int getVolatileValueAt(long index) throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the int value at a specified index using ordered semantics.
     *
     * @param index the index at which to set the int value
     * @param value the int value to set
     * @throws IllegalStateException   if the array is closed
     * @throws BufferOverflowException if the index is out of bounds
     */
    void setOrderedValueAt(long index, int value) throws IllegalStateException, BufferOverflowException;

    /**
     * Atomically sets the value at a specified index to the given updated value if the current value equals the expected value.
     *
     * @param index    the index at which to set the int value
     * @param expected the expected current value
     * @param value    the new value
     * @return true if successful, false otherwise
     * @throws IllegalStateException   if the array is closed
     * @throws BufferOverflowException if the index is out of bounds
     */
    boolean compareAndSet(long index, int expected, int value) throws IllegalStateException, BufferOverflowException;

    /**
     * Binds an IntValue to a specified index in the array.
     *
     * @param index the index at which to bind the IntValue
     * @param value the IntValue to bind
     * @throws IllegalStateException    if the array is closed
     * @throws BufferOverflowException  if the index is out of bounds
     * @throws IllegalArgumentException if the value is not valid
     */
    void bindValueAt(long index, IntValue value) throws IllegalStateException, BufferOverflowException, IllegalArgumentException;

    /**
     * Calculates the size in bytes of an array with the specified capacity.
     *
     * @param capacity the capacity of the array
     * @return the size in bytes of the array
     * @throws IllegalStateException if the array is closed
     */
    long sizeInBytes(long capacity) throws IllegalStateException;

    /**
     * Checks if the array is null.
     *
     * @return true if the array is null, false otherwise
     * @throws IllegalStateException if the array is closed
     */
    boolean isNull() throws IllegalStateException;

    /**
     * Resets the array, clearing all values.
     *
     * @throws IllegalStateException if the array is closed
     */
    void reset() throws IllegalStateException;
}
