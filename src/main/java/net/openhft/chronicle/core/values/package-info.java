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
 * </ul>
 *
 * @since 10/10/13
 */
package net.openhft.chronicle.core.values;
