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
 * Represents a reference to a 32-bit integer value, providing various methods
 * for accessing and modifying the value.  * Implementations may store the value in different formats or mediums. For example, the value could be stored
 *  * in binary format or text format, in memory or on disk.
 * <p>
 * The interface provides basic operations such as getting and setting the
 * value, as well as atomic and ordered operations that incorporate memory
 * barriers and atomicity guarantees.
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
 * @author Peter Lawrey
 * @since 10/10/13
 */
public interface IntValue extends Closeable {

    /**
     * Retrieves the current 32-bit integer value.
     *
     * @return the current value
     * @throws IllegalStateException if the value is accessed after being closed
     * @throws BufferUnderflowException if there's not enough data available to read
     */
    int getValue() throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the 32-bit integer value.
     *
     * @param value the new value to set
     * @throws IllegalStateException if the value is modified after being closed
     * @throws BufferOverflowException if there's not enough space to write the data
     */
    void setValue(int value) throws IllegalStateException, BufferOverflowException;

    /**
     * Retrieves the current 32-bit integer value using volatile semantics,
     * which include a memory barrier ensuring that subsequent reads and writes
     * are not reordered beyond this point.
     *
     * @return the current value
     * @throws IllegalStateException if the value is accessed after being closed
     * @throws BufferUnderflowException if there's not enough data available to read
     */
    int getVolatileValue() throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the 32-bit integer value using ordered semantics, which includes
     * a memory barrier ensuring that previous writes are not reordered beyond
     * this point.
     *
     * @param value the new value to set
     * @throws IllegalStateException if the value is modified after being closed
     * @throws BufferOverflowException if there's not enough space to write the data
     */
    void setOrderedValue(int value) throws IllegalStateException, BufferOverflowException;

    /**
     * Atomically adds a specified delta to the current value and returns the result.
     *
     * @param delta the value to add to the current value
     * @return the updated value
     * @throws IllegalStateException if the value is modified after being closed
     * @throws BufferUnderflowException if there's not enough data available to read
     */
    int addValue(int delta) throws IllegalStateException, BufferUnderflowException;

    /**
     * Atomically adds a specified delta to the current value and returns the result.
     * This operation is performed atomically and is visible to all threads.
     *
     * @param delta the value to add to the current value
     * @return the updated value
     * @throws IllegalStateException if the value is modified after being closed
     * @throws BufferUnderflowException if there's not enough data available to read
     */
    int addAtomicValue(int delta) throws IllegalStateException, BufferUnderflowException;

    /**
     * Atomically sets the value to the given updated value if the current value
     * is equal to the expected value.
     *
     * @param expected the value expected to be present
     * @param value the new value to set if the expected value is found
     * @return true if successful, false otherwise
     * @throws IllegalStateException if the value is modified after being closed
     * @throws BufferOverflowException if there's not enough space to write the data
     */
    boolean compareAndSwapValue(int expected, int value) throws IllegalStateException, BufferOverflowException;

    @Override
    default boolean isClosed() {
        return false;
    }

    default void close() {
    }
}
