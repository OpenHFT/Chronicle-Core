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
 * This interface represents a reference to a float value. It provides methods to get, set, and add to the value.
 * Implementations of this interface are expected to handle the storage and retrieval of the float value.
 * <p>
 * Implementations may store the value in different formats or mediums. For example, the value could be stored
 * in binary format or text format, in memory or on disk.
 * <p>
 * The getValue and getVolatileValue methods retrieve the current float value. The difference between these methods
 * is that getVolatileValue uses volatile semantics, meaning it includes a memory barrier which ensures that
 * subsequent reads and writes are not reordered beyond this point.
 * <p>
 * The setValue and setOrderedValue methods set the float value. The difference between these methods is that
 * setOrderedValue uses ordered semantics, meaning it includes a memory barrier which ensures that previous
 * writes are not reordered beyond this point.
 * <p>
 * The addValue and addAtomicValue methods add a specified float to the current value and return the result.
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
 * @see net.openhft.chronicle.core.values.UnsetLongValue
 * @since 10/10/13
 */
public interface FloatValue {
    /**
     * Retrieves the float value.
     *
     * @return the float value
     */
    float getValue();

    /**
     * Sets the float value.
     *
     * @param value the float value to set
     */
    void setValue(float value);

    /**
     * Sets the float value using ordered semantics.
     *
     * @param value the float value to set
     */
    void setOrderedValue(float value);

    /**
     * Retrieves the float value using volatile semantics.
     *
     * @return the float value
     */
    float getVolatileValue();

    /**
     * Adds a specified float to the current value and returns the result.
     *
     * @param delta the float to add to the current value
     * @return the result of adding the specified float to the current value
     */
    float addValue(float delta);

    /**
     * Adds a specified float to the current value and returns the result using atomic semantics.
     *
     * @param delta the float to add to the current value
     * @return the result of adding the specified float to the current value
     */
    float addAtomicValue(float delta);
}
