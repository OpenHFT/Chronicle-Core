/*
 * Copyright 2016-2020 chronicle.software
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

import net.openhft.chronicle.core.Jvm;

/**
 * Represents a container for two {@code long} values, providing mechanisms for getting, setting, and
 * manipulating them with various memory ordering effects.
 * <p>
 * This interface extends {@link net.openhft.chronicle.core.values.LongValue}, which provides the base
 * functionality for a single {@code long} value. This interface adds support for a second {@code long} value.
 * <p>
 * Concrete implementations are responsible for managing the storage and retrieval mechanisms of these {@code long}
 * values.
 * <p>
 * Notable methods of this interface include:
 * <ul>
 *     <li>{@link #getValue2()} - Retrieves the second {@code long} value.</li>
 *     <li>{@link #setValue2(long)} - Sets the second {@code long} value.</li>
 *     <li>{@link #getVolatileValue2()} - Retrieves the second {@code long} value with volatile semantics.</li>
 *     <li>{@link #setVolatileValue2(long)} - Sets the second {@code long} value with volatile semantics.</li>
 *     <li>{@link #addValue2(long)} - Atomically adds the given amount to the second {@code long} value.</li>
 *     <li>{@link #compareAndSwapValue2(long, long)} - Atomically sets the second {@code long} value if it is equal to the expected value.</li>
 * </ul>
 * <p>
 * Additionally, the interface provides default implementations for setting the second value to the maximum or minimum
 * of the current value and a specified value, and for atomically setting and retrieving both values.
 * <p>
 * Implementations can also include additional behaviors or optimizations not specified in this interface.
 *
 * @author Peter Lawrey
 * @apiNote The {@link #getValue()} and {@link #setValue(long)} methods inherited from {@link LongValue} are applicable
 * to the first {@code long} value.
 * @implSpec Implementations must ensure that all methods are thread-safe and that changes to the values are correctly
 * synchronized across threads.
 * @see net.openhft.chronicle.core.values.LongValue
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
public interface TwoLongValue extends LongValue {

    /**
     * Retrieves the second long value.
     *
     * @return The second long value.
     * @throws IllegalStateException if the retrieval fails due to illegal state.
     */
    long getValue2() throws IllegalStateException;

    /**
     * Sets the second long value.
     *
     * @param value2 The value to set.
     * @throws IllegalStateException if the setting fails due to illegal state.
     */
    void setValue2(long value2) throws IllegalStateException;

    /**
     * Retrieves the second long value using volatile semantics.
     * Ensures that subsequent reads and writes are not reordered beyond this point.
     *
     * @return The second long value.
     * @throws IllegalStateException if the retrieval fails due to illegal state.
     */
    long getVolatileValue2() throws IllegalStateException;

    /**
     * Sets the second long value using volatile semantics.
     * Ensures that subsequent reads and writes are not reordered beyond this point.
     *
     * @param value The value to set.
     * @throws IllegalStateException if the setting fails due to illegal state.
     */
    void setVolatileValue2(long value) throws IllegalStateException;

    /**
     * Sets the second long value using ordered semantics.
     * Ensures that previous writes are not reordered beyond this point.
     *
     * @param value The value to set.
     * @throws IllegalStateException if the setting fails due to illegal state.
     */
    void setOrderedValue2(long value) throws IllegalStateException;

    /**
     * Adds the specified value to the second long value and returns the result.
     *
     * @param delta The value to add.
     * @return The result after addition.
     * @throws IllegalStateException if the addition fails due to illegal state.
     */
    long addValue2(long delta) throws IllegalStateException;

    /**
     * Atomically adds the specified value to the second long value.
     * Ensures that the operation is done atomically and is visible to all threads.
     *
     * @param delta The value to add.
     * @return The result after the addition.
     * @throws IllegalStateException if the addition fails due to illegal state.
     */
    long addAtomicValue2(long delta) throws IllegalStateException;

    /**
     * Atomically compares the current second long value with the expected value,
     * and if they match, updates the second value to the specified new value.
     *
     * @param expected The expected value.
     * @param value    The new value.
     * @return {@code true} if the swap was successful, otherwise {@code false}.
     * @throws IllegalStateException if the operation fails due to illegal state.
     */
    boolean compareAndSwapValue2(long expected, long value) throws IllegalStateException;

    /**
     * Sets the second long value to the maximum of the current value and the specified value.
     *
     * @param value The value to compare.
     * @throws IllegalStateException if the setting fails due to illegal state.
     */
    @Deprecated(/* to be moved in x.25 */)
    default void setMaxValue2(long value) throws IllegalStateException {
        for (; ; ) {
            long pos = getVolatileValue2();
            if (pos >= value)
                break;
            if (compareAndSwapValue2(pos, value))
                break;
            Jvm.nanoPause();
        }
    }

    /**
     * Sets the second long value to the minimum of the current value and the specified value.
     *
     * @param value The value to compare.
     * @throws IllegalStateException if the setting fails due to illegal state.
     */
    @Deprecated(/* to be moved in x.25 */)
    default void setMinValue2(long value) throws IllegalStateException {
        for (; ; ) {
            long pos = getVolatileValue2();
            if (pos <= value)
                break;
            if (compareAndSwapValue2(pos, value))
                break;
            Jvm.nanoPause();
        }
    }

    /**
     * Atomically sets both long values.
     *
     * @param value1 The first value to set.
     * @param value2 The second value to set.
     * @throws IllegalStateException if the setting fails due to illegal state.
     */
    @Deprecated(/* to be moved in x.25 */)
    default void setValues(long value1, long value2) throws IllegalStateException {
        setValue2(value2);
        setOrderedValue(value1);
    }

    /**
     * Retrieves both long values atomically.
     *
     * @param values An array where the values will be stored. The first value is stored at index 0,
     *               and the second value is stored at index 1.
     * @throws IllegalStateException if the retrieval fails due to illegal state.
     */
    @Deprecated(/* to be moved in x.25 */)
    default void getValues(long[] values) throws IllegalStateException {
        long value1 = getVolatileValue();
        long value2 = getValue2();
        while (true) {
            long value1b = getVolatileValue();
            long value2b = getValue2();
            if (value1 == value1b && value2 == value2b) {
                values[0] = value1;
                values[1] = value2;
                return;
            }
            value1 = value1b;
            value2 = value2b;
        }
    }
}
