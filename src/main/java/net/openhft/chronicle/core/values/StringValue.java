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
 * @see net.openhft.chronicle.core.values.UnsetLongValue
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
