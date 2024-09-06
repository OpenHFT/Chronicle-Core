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

/**
 * Provides interfaces and classes for representing references to various primitive data types
 * and handling their storage and retrieval.
 *
 * <p>This package contains interfaces representing references to different types of values,
 * including boolean, byte, char, double, float, int, long, short, and String. Additionally, there
 * are interfaces for representing references to arrays of int and long values. Each interface
 * provides methods for getting and setting the respective value(s). There are also classes that
 * represent specific implementations of these interfaces.
 *
 * <p>Implementations of these interfaces can handle the storage and retrieval of the respective
 * values in various formats or mediums (e.g., in memory or on disk). The interfaces also define
 * methods for atomic and concurrent modifications, including get and set methods with volatile
 * semantics and atomic operations such as compare-and-swap.
 *
 * <p>The {@code MaxBytes} annotation is available for specifying the maximum size constraints on
 * variable-length data types in bytes or elements.
 *
 * <ul>
 *   <li>{@link net.openhft.chronicle.core.values.BooleanValue} - Represents a reference to a boolean value.
 *   <li>{@link net.openhft.chronicle.core.values.ByteValue} - Represents a reference to a byte value.
 *   <li>{@link net.openhft.chronicle.core.values.CharValue} - Represents a reference to a char value.
 *   <li>{@link net.openhft.chronicle.core.values.DoubleValue} - Represents a reference to a double value.
 *   <li>{@link net.openhft.chronicle.core.values.FloatValue} - Represents a reference to a float value.
 *   <li>{@link net.openhft.chronicle.core.values.IntArrayValues} - Represents a reference to an array of int values.
 *   <li>{@link net.openhft.chronicle.core.values.IntValue} - Represents a reference to a 32-bit integer value.
 *   <li>{@link net.openhft.chronicle.core.values.LongArrayValues} - Represents a reference to an array of long values.
 *   <li>{@link net.openhft.chronicle.core.values.LongValue} - Represents a reference to a long value.
 *   <li>{@link net.openhft.chronicle.core.values.ShortValue} - Represents a reference to a 16-bit short value.
 *   <li>{@link net.openhft.chronicle.core.values.StringValue} - Represents a reference to a String value.
 *   <li>{@link net.openhft.chronicle.core.values.TwoLongValue} - Represents a reference to two long values.
 *   <li>{@link net.openhft.chronicle.core.values.UnsetLongValue} - Represents a long value that ignores attempts to modify it.
 * </ul>
 *
 * @since 10/10/13
 */
package net.openhft.chronicle.core.values;
