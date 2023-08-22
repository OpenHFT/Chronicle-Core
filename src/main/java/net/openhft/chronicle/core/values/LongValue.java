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
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.core.io.ClosedIllegalStateException;
import net.openhft.chronicle.core.io.ThreadingIllegalStateException;

/**
 * The LongValue interface provides an abstraction for a mutable long value that supports atomic
 * and concurrent modifications. It extends Closeable to allow releasing resources when they are
 * no longer needed.
 * <p>
 * This interface offers various methods for getting and setting the long value, including
 * atomic operations like compare-and-swap and mechanisms to set values with volatile semantics.
 * <p>
 * Implementations of this interface should ensure thread-safety for concurrent modifications.
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
public interface LongValue extends Closeable {

    /**
     * Retrieves the current long value.
     *
     * @return the current long value.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    long getValue() throws IllegalStateException;

    /**
     * Sets the long value.
     *
     * @param value the new long value.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    void setValue(long value) throws IllegalStateException;

    /**
     * Retrieves the current long value with volatile semantics.
     *
     * @return the current long value.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    // TODO Move to subclass so it's not a default in x.25
    default long getVolatileValue() throws IllegalStateException {
        return getValue();
    }

    /**
     * Sets the long value with volatile semantics.
     *
     * @param value the new long value.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    // TODO Move to subclass so it's not a default in x.25
    default void setVolatileValue(long value) throws IllegalStateException {
        setValue(value);
    }

    /**
     * Retrieves the current long value with volatile semantics, or returns the given
     * closedValue if the underlying resource is not available.
     *
     * @param closedValue the value to return if the underlying resource is not available.
     * @return the current long value or the closedValue.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    // TODO Move to subclass so it's not a default in x.25
    default long getVolatileValue(long closedValue) throws IllegalStateException {
        if (isClosed())
            return closedValue;
        try {
            return getVolatileValue();
        } catch (Exception e) {
            return closedValue;
        }
    }

    /**
     * Sets the long value with ordered or lazy set semantics.
     *
     * @param value the new long value.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    // TODO Move to subclass so it's not a default in x.25
    default void setOrderedValue(long value) throws IllegalStateException {
        setVolatileValue(value);
    }

    /**
     * Adds the specified delta to the current long value.
     *
     * @param delta the value to be added.
     * @return the updated long value.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    long addValue(long delta) throws IllegalStateException;

    /**
     * Atomically adds the specified delta to the current long value.
     *
     * @param delta the value to be added.
     * @return the updated long value.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    // TODO Move to subclass so it's not a default in x.25
    default long addAtomicValue(long delta) throws IllegalStateException {
        return addValue(delta);
    }

    /**
     * Atomically sets the value to the given updated value if the current value is equal to the expected value.
     *
     * @param expected the expected value.
     * @param value    the new value.
     * @return true if successful, false otherwise.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    boolean compareAndSwapValue(long expected, long value) throws IllegalStateException;

    /**
     * Atomically sets the long value to the given value if it is greater than the current value.
     *
     * @param value the value to be set.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    // TODO Move to subclass so it's not a default in x.25
    default void setMaxValue(long value) throws IllegalStateException {
        for (; ; ) {
            long pos = getVolatileValue();
            if (pos >= value)
                break;
            if (compareAndSwapValue(pos, value))
                break;
            Jvm.nanoPause();
        }
    }

    /**
     * Atomically sets the long value to the given value if it is less than the current value.
     *
     * @param value the value to be set.
     * @throws ClosedIllegalStateException    if the resource has been released or closed.
     * @throws ThreadingIllegalStateException if this resource was accessed by multiple threads in an unsafe way
     */
    // TODO Move to subclass so it's not a default in x.25
    default void setMinValue(long value) throws IllegalStateException {
        for (; ; ) {
            long pos = getVolatileValue();
            if (pos <= value)
                break;
            if (compareAndSwapValue(pos, value))
                break;
            Jvm.nanoPause();
        }
    }

    /**
     * Checks if the LongValue instance is closed.
     *
     * @return true if the instance is closed, false otherwise.
     */
    @Override
    // TODO Move to subclass so it's not a default in x.25
    default boolean isClosed() {
        return false;
    }

    /**
     * Closes the LongValue instance and releases any resources associated with it.
     */
    @Override
    // TODO Move to subclass so it's not a default in x.25
    default void close() {
    }
}
