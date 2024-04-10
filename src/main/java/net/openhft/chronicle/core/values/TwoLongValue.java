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

import net.openhft.chronicle.core.io.ClosedIllegalStateException;
import net.openhft.chronicle.core.io.ThreadingIllegalStateException;

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
 * @since 10/10/13
 */
public interface TwoLongValue extends LongValue {

    /**
     * Retrieves the second long value.
     *
     * @return The second long value.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    long getValue2() throws IllegalStateException;

    /**
     * Sets the second long value.
     *
     * @param value2 The value to set.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void setValue2(long value2) throws IllegalStateException;

    /**
     * Retrieves the second long value using volatile semantics.
     * Ensures that subsequent reads and writes are not reordered beyond this point.
     *
     * @return The second long value.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    long getVolatileValue2() throws IllegalStateException;

    /**
     * Sets the second long value using volatile semantics.
     * Ensures that subsequent reads and writes are not reordered beyond this point.
     *
     * @param value The value to set.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void setVolatileValue2(long value) throws IllegalStateException;

    /**
     * Sets the second long value using ordered semantics.
     * Ensures that previous writes are not reordered beyond this point.
     *
     * @param value The value to set.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void setOrderedValue2(long value) throws IllegalStateException;

    /**
     * Adds the specified value to the second long value and returns the result.
     *
     * @param delta The value to add.
     * @return The result after addition.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    long addValue2(long delta) throws IllegalStateException;

    /**
     * Atomically adds the specified value to the second long value.
     * Ensures that the operation is done atomically and is visible to all threads.
     *
     * @param delta The value to add.
     * @return The result after the addition.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    long addAtomicValue2(long delta) throws IllegalStateException;

    /**
     * Atomically compares the current second long value with the expected value,
     * and if they match, updates the second value to the specified new value.
     *
     * @param expected The expected value.
     * @param value    The new value.
     * @return {@code true} if the swap was successful, otherwise {@code false}.
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    boolean compareAndSwapValue2(long expected, long value) throws IllegalStateException;
}
