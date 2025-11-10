//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.values;

import org.jetbrains.annotations.NotNull;

/**
 * The StringValue interface represents a reference to a String value. It provides methods to
 * retrieve and set the String value. Implementations of this interface may handle the storage
 * and retrieval of the String value in various formats or mediums.
 *
 * <p>The {@code getValue} method retrieves the String value and the {@code setValue} method
 * sets the String value. The {@code getUsingValue} method retrieves the String value into a
 * supplied {@code StringBuilder} instance, which can be beneficial in scenarios where minimizing
 * object allocations is desirable.
 *
 * <p>The {@code setValue} method allows constraining the length of the CharSequence via the {@code @MaxBytes}
 * annotation. This can be useful for enforcing memory constraints on the stored value.
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
 */
public interface StringValue {

    /**
     * Retrieves the String value.
     *
     * @return The current String value.
     */
    @NotNull
    String getValue();

    /**
     * Sets the String value from a CharSequence. The maximum number of encoded bytes allowed
     * can be constrained using the {@code @MaxBytes} annotation.
     *
     * @param value The CharSequence from which the String value is set.
     */
    void setValue(@MaxBytes CharSequence value);

    /**
     * Retrieves the String value into a supplied {@code StringBuilder} instance. This is useful
     * in scenarios where minimizing object allocations is desirable.
     *
     * @param stringBuilder The {@code StringBuilder} instance to populate with the String value.
     * @return A {@code StringBuilder} containing the current String value.
     */
    @NotNull
    StringBuilder getUsingValue(StringBuilder stringBuilder);
}
