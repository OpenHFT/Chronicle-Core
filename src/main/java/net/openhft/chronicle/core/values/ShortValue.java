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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * Represents a reference to a 16-bit short value, providing methods for
 * accessing and modifying the value. Implementations of this interface
 * should handle the storage and retrieval of the short value, which may
 * be in memory or other storage mediums.
 * <p>
 * The interface provides basic operations such as getting and setting the
 * value, as well as an atomic addition operation.
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
public interface ShortValue {

    /**
     * Retrieves the current 16-bit short value.
     *
     * @return the current value as a short.
     * @throws IllegalStateException if the value is accessed after the underlying storage is released.
     * @throws BufferUnderflowException if there's not enough data available to read.
     */
    short getValue() throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the current 16-bit short value.
     *
     * @param value the new value to set as a short.
     * @throws IllegalStateException if the value is modified after the underlying storage is released.
     * @throws BufferOverflowException if there's not enough space to write the data.
     */
    void setValue(short value) throws IllegalStateException, BufferOverflowException;

    /**
     * Atomically adds a specified delta to the current value and returns the result.
     * This method performs the addition operation atomically, ensuring that it behaves
     * as a single, uninterruptible operation.
     *
     * @param delta the value to add to the current value as a short.
     * @return the updated value as a short.
     * @throws IllegalStateException if the value is modified after the underlying storage is released.
     * @throws BufferUnderflowException if there's not enough data available to read.
     * @throws BufferOverflowException if there's not enough space to write the data.
     */
    short addValue(short delta) throws IllegalStateException, BufferUnderflowException, BufferOverflowException;
}
