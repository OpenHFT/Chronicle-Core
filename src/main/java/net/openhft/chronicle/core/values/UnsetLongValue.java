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

package net.openhft.chronicle.core.values;

/**
 * Represents a long value that ignores attempts to modify it, and always returns the same value.
 * <p>
 * This class is useful in scenarios where a LongValue is expected, but modifications should be ignored,
 * and a fixed or default value should be returned. This is akin to making the value 'immutable'.
 * <p>
 * The value returned by this class is set at construction and cannot be changed thereafter.
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
 */
public class UnsetLongValue implements LongValue {
    private final long value;

    /**
     * Constructs an UnsetLongValue with the specified value.
     *
     * @param value the value to be set
     */
    public UnsetLongValue(long value) {
        this.value = value;
    }

    /**
     * Retrieves the value set at construction.
     *
     * @return the long value
     * @throws IllegalStateException if any condition makes retrieval illegal (e.g., if underlying resources are closed)
     */
    @Override
    public long getValue() throws IllegalStateException {
        return value;
    }

    /**
     * This method does not change the value. It is here to fulfill the LongValue interface but has no effect.
     *
     * @param value the value to set (ignored)
     * @throws IllegalStateException if any condition makes setting illegal (e.g., if underlying resources are closed)
     */
    @Override
    public void setValue(long value) throws IllegalStateException {
        // ignored
    }

    /**
     * Retrieves the volatile value provided as a parameter since internal value modification is ignored.
     *
     * @param closedValue the volatile value to be retrieved
     * @return the closedValue parameter passed to this method
     * @throws IllegalStateException if any condition makes retrieval illegal (e.g., if underlying resources are closed)
     */
    @Override
    public long getVolatileValue(long closedValue) throws IllegalStateException {
        return closedValue;
    }

    /**
     * Ignores the addition and simply returns the value set at construction.
     *
     * @param delta the value to be added (ignored)
     * @return the long value set at construction
     * @throws IllegalStateException if any condition makes this operation illegal (e.g., if underlying resources are closed)
     */
    @Override
    public long addValue(long delta) throws IllegalStateException {
        return value;
    }

    /**
     * Ignores the compare and swap, and simply returns true.
     *
     * @param expected the expected value (ignored)
     * @param value    the new value (ignored)
     * @return true
     * @throws IllegalStateException if any condition makes this operation illegal (e.g., if underlying resources are closed)
     */
    @Override
    public boolean compareAndSwapValue(long expected, long value) throws IllegalStateException {
        return true;
    }
}
