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

/**
 * This interface represents a reference to a double value. It provides methods to get, set, and add to the value.
 * Implementations of this interface are expected to handle the storage and retrieval of the double value.
 * <p>
 * Implementations may store the value in different formats or mediums. For example, the value could be stored
 * in binary format or text format, in memory or on disk.
 * <p>
 * The getValue and getVolatileValue methods retrieve the current double value. The difference between these methods
 * is that getVolatileValue uses volatile semantics, meaning it includes a memory barrier which ensures that
 * subsequent reads and writes are not reordered beyond this point.
 * <p>
 * The setValue and setOrderedValue methods set the double value. The difference between these methods is that
 * setOrderedValue uses ordered semantics, meaning it includes a memory barrier which ensures that previous
 * writes are not reordered beyond this point.
 * <p>
 * The addValue and addAtomicValue methods add a specified double to the current value and return the result.
 * The difference between these methods is that addAtomicValue uses atomic semantics, meaning the operation
 * is done atomically and is visible to all threads.
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
 * @since 10/10/13
 */
public interface DoubleValue {
    /**
     * Retrieves the double value.
     *
     * @return the double value
     */
    double getValue();

    /**
     * Sets the double value.
     *
     * @param value the double value to set
     */
    void setValue(double value);

    /**
     * Retrieves the double value using volatile semantics.
     *
     * @return the double value
     */
    double getVolatileValue();

    /**
     * Sets the double value using ordered semantics.
     *
     * @param value the double value to set
     */
    void setOrderedValue(double value);

    /**
     * Adds a specified double to the current value and returns the result.
     *
     * @param delta the double to add to the current value
     * @return the result of adding the specified double to the current value
     */
    double addValue(double delta);

    /**
     * Adds a specified double to the current value and returns the result using atomic semantics.
     *
     * @param delta the double to add to the current value
     * @return the result of adding the specified double to the current value
     */
    double addAtomicValue(double delta);
}
