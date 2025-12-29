/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.values;

import net.openhft.chronicle.core.io.ClosedIllegalStateException;
import net.openhft.chronicle.core.io.ThreadingIllegalStateException;

import java.nio.BufferUnderflowException;

/**
 * This interface represents a reference to a boolean value. It provides methods to get and set the value.
 * Implementations of this interface are expected to handle the storage and retrieval of the boolean value.
 * <p>
 * Implementations may store the value in different formats or mediums. For example, the value could be stored
 * in binary format or text format, in memory or on disk.
 * <p>
 * The methods in this interface may throw an IllegalStateException if the underlying storage medium is not
 * in a state where the operation can be performed. For example, if the storage medium has been closed or
 * if it does not have enough capacity to store the value.
 * <p>
 * The getValue method may also throw a BufferUnderflowException if there is not enough data available to read
 * the value.
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
public interface BooleanValue {
    /**
     * Retrieves the current boolean value from the backing store.
     *
     * @return the boolean value
     * @throws BufferUnderflowException       If there is not enough data available to read the value
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    boolean getValue() throws IllegalStateException, BufferUnderflowException;

    /**
     * Sets the boolean value in the backing store.
     *
     * @param value the boolean value to set
     * @throws ClosedIllegalStateException    If the resource has been released or closed.
     * @throws ThreadingIllegalStateException If this resource was accessed by multiple threads in an unsafe way
     */
    void setValue(boolean value) throws IllegalStateException;
}
