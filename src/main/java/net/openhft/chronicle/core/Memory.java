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

package net.openhft.chronicle.core;

import net.openhft.chronicle.core.annotation.Positive;
import net.openhft.chronicle.core.util.Longs;
import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static java.util.Objects.nonNull;
import static net.openhft.chronicle.assertions.AssertUtil.SKIP_ASSERTIONS;
import static net.openhft.chronicle.core.util.Longs.assertIfEnabled;

/**
 * The Memory interface provides low-level memory access methods.
 */
public interface Memory {

    /**
     * Retrieves the amount of heap memory currently used by the application.
     *
     * @return The number of bytes used in the heap memory.
     */
    @Deprecated(/* remove in x.26 */)
    default long heapUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Ensures that all previous writes to memory are completed before any
     * subsequent memory access instruction.
     */
    void storeFence();

    /**
     * Ensures that all previous reads from memory are completed before any
     * subsequent memory access instruction.
     */
    void loadFence();

    /**
     * Sets the memory at the specified address with the given byte value.
     *
     * @param address The starting address of the memory block.
     * @param size    The size of the memory block in bytes.
     * @param b       The byte value to be set.
     */
    void setMemory(long address, long size, byte b);

    /**
     * Sets the memory at the specified offset within the given object with
     * the given byte value.
     *
     * @param o      The object containing the memory block.
     * @param offset The offset within the object where the memory block starts.
     * @param size   The size of the memory block in bytes.
     * @param b      The byte value to be set.
     */
    void setMemory(Object o, long offset, long size, byte b);

    /**
     * Frees the memory block at the specified address with the given size.
     *
     * @param address The starting address of the memory block.
     * @param size    The size of the memory block in bytes.
     */
    void freeMemory(long address, long size);

    /**
     * Allocates memory and returns the low level base address of the newly allocated
     * memory region.
     *
     * @param capacity to allocate
     * @return the low level base address of the newly allocated memory region
     * @throws IllegalArgumentException If the capacity is non-positive
     * @throws OutOfMemoryError         if there are not enough memory to allocate
     */
    long allocate(@Positive long capacity);

    /**
     * Retrieves the amount of native memory currently used by the application.
     *
     * @return The number of bytes used in the native memory.
     */
    long nativeMemoryUsed();

    /**
     * Writes a byte value to the memory at the specified address.
     *
     * @param address The address where the byte should be written.
     * @param i8      The byte value to be written.
     */
    void writeByte(long address, byte i8);

    /**
     * Writes a byte value to the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the byte should be written.
     * @param b      The byte value to be written.
     */
    void writeByte(Object object, long offset, byte b);

    /**
     * Reads a byte value from the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the byte should be read.
     * @return The byte value read from memory.
     */
    byte readByte(Object object, long offset);

    /**
     * Writes an array of bytes to the memory at the specified address.
     *
     * @param address The address where the bytes should be written.
     * @param b       The byte array containing the bytes to be written.
     * @param offset  The starting offset within the array.
     * @param length  The number of bytes to be written.
     * @throws IllegalArgumentException If the offset or length is invalid.
     */
    void writeBytes(long address, byte[] b, int offset, int length) throws IllegalArgumentException;

    /**
     * Reads an array of bytes from the memory at the specified address and stores it in the provided byte array.
     *
     * @param address The address where the bytes should be read from.
     * @param b       The byte array to store the read bytes.
     * @param offset  The starting offset within the byte array.
     * @param length  The number of bytes to be read.
     * @throws IllegalArgumentException If the offset or length is invalid.
     */
    void readBytes(long address, byte[] b, long offset, int length) throws IllegalArgumentException;

    /**
     * Reads a byte value from the memory at the specified address.
     *
     * @param address The address where the byte should be read from.
     * @return The byte value read from memory.
     */
    byte readByte(long address);

    /**
     * Writes a short value to the memory at the specified address.
     *
     * @param address The address where the short should be written.
     * @param i16     The short value to be written.
     */
    void writeShort(long address, short i16);

    /**
     * Writes a short value to the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the short should be written.
     * @param i16    The short value to be written.
     */
    void writeShort(Object object, long offset, short i16);

    /**
     * Reads a short value from the memory at the specified address.
     *
     * @param address The address where the short should be read from.
     * @return The short value read from memory.
     */
    short readShort(long address);

    /**
     * Reads a short value from the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the short should be read.
     * @return The short value read from memory.
     */
    short readShort(Object object, long offset);

    /**
     * Writes an int value to the memory at the specified address.
     *
     * @param address The address where the int should be written.
     * @param i32     The int value to be written.
     */
    void writeInt(long address, int i32);

    /**
     * Writes an int value to the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the int should be written.
     * @param i32    The int value to be written.
     */
    void writeInt(Object object, long offset, int i32);

    /**
     * Writes an int value to the memory at the specified offset within the given object in an ordered manner.
     *
     * @param offset The offset within the object where the int should be written.
     * @param i32    The int value to be written.
     */
    void writeOrderedInt(long offset, int i32);

    /**
     * Writes an int value to the memory at the specified offset within the given object in an ordered manner.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the int should be written.
     * @param i32    The int value to be written.
     */
    void writeOrderedInt(Object object, long offset, int i32);

    /**
     * Reads an int value from the memory at the specified address.
     *
     * @param address The address where the int should be read from.
     * @return The int value read from memory.
     */
    int readInt(long address);

    /**
     * Reads an int value from the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the int should be read.
     * @return The int value read from memory.
     */
    int readInt(Object object, long offset);

    /**
     * Writes a long value to the memory at the specified address.
     *
     * @param address The address where the long should be written.
     * @param i64     The long value to be written.
     */
    void writeLong(long address, long i64);

    /**
     * Writes a long value to the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the long should be written.
     * @param i64    The long value to be written.
     */
    void writeLong(Object object, long offset, long i64);

    /**
     * Reads a long value from the memory at the specified address.
     *
     * @param address The address where the long should be read from.
     * @return The long value read from memory.
     */
    long readLong(long address);

    /**
     * Reads a long value from the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the long should be read.
     * @return The long value read from memory.
     */
    long readLong(Object object, long offset);

    /**
     * Writes a float value to the memory at the specified address.
     *
     * @param address The address where the float should be written.
     * @param f       The float value to be written.
     */
    void writeFloat(long address, float f);

    /**
     * Writes a float value to the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the float should be written.
     * @param f      The float value to be written.
     */
    void writeFloat(Object object, long offset, float f);

    /**
     * Reads a float value from the memory at the specified address.
     *
     * @param address The address where the float should be read from.
     * @return The float value read from memory.
     */
    float readFloat(long address);

    /**
     * Reads a float value from the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the float should be read.
     * @return The float value read from memory.
     */
    float readFloat(Object object, long offset);

    /**
     * Writes a double value to the memory at the specified address.
     *
     * @param address The address where the double should be written.
     * @param d       The double value to be written.
     */
    void writeDouble(long address, double d);

    /**
     * Writes a double value to the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the double should be written.
     * @param d      The double value to be written.
     */
    void writeDouble(Object object, long offset, double d);

    /**
     * Reads a double value from the memory at the specified address.
     *
     * @param address The address where the double should be read from.
     * @return The double value read from memory.
     */
    double readDouble(long address);

    /**
     * Reads a double value from the memory at the specified offset within the given object.
     *
     * @param object The object containing the memory block.
     * @param offset The offset within the object where the double should be read.
     * @return The double value read from memory.
     */
    double readDouble(Object object, long offset);

    /**
     * Copies a range of bytes from the given byte array to the specified memory address.
     *
     * @param bytes   the source byte array
     * @param offset  the starting index in the byte array
     * @param address the destination memory address
     * @param length  the number of bytes to copy
     */
    void copyMemory(byte[] bytes, int offset, long address, int length);

    /**
     * Copies a range of memory from one address to another.
     *
     * @param fromAddress the source memory address
     * @param address     the destination memory address
     * @param length      the number of bytes to copy
     */
    void copyMemory(long fromAddress, long address, long length);

    /**
     * Deprecated method. Copies a range of bytes from the given byte array to the specified object at the given offset.
     *
     * @param bytes   the source byte array
     * @param offset  the starting index in the byte array
     * @param obj2    the destination object
     * @param offset2 the starting offset in the destination object
     * @param length  the number of bytes to copy
     * @deprecated This method is deprecated and will be removed in a future version (x.24).
     */
    @Deprecated(/* to be removed in x.26 */)
    default void copyMemory(byte[] src, int srcOffset, @Nullable Object dest, long destOffset, int length) {
        assert SKIP_ASSERTIONS || nonNull(src);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);

        if (dest instanceof byte[]) {
            copyMemory(src, srcOffset, (byte[]) dest, Math.toIntExact(destOffset - Unsafe.ARRAY_BYTE_BASE_OFFSET), length);
        } else {
            copyMemory(src, Unsafe.ARRAY_BYTE_BASE_OFFSET + srcOffset, dest, destOffset, length);
        }
    }

    /**
     * Copies a range of memory from the given object to the specified memory address.
     *
     * @param o         the source object
     * @param offset    the starting offset in the source object
     * @param toAddress the destination memory address
     * @param length    the number of bytes to copy
     */
    void copyMemory(Object o, long offset, long toAddress, int length);

    /**
     * Copies a range of memory from one object to another at the specified offsets.
     *
     * @param o       the source object
     * @param offset  the starting offset in the source object
     * @param o2      the destination object
     * @param offset2 the starting offset in the destination object
     * @param length  the number of bytes to copy
     */
    void copyMemory(Object o, long offset, Object o2, long offset2, int length);

    /**
     * Copies a range of memory from the given memory address to the specified object at the given offset.
     *
     * @param fromAddress the source memory address
     * @param obj2        the destination object
     * @param offset2     the starting offset in the destination object
     * @param length      the number of bytes to copy
     */
    void copyMemory(long fromAddress, Object obj2, long offset2, int length);

    /**
     * Returns the length in bytes required to encode an integer using stop-bit encoding.
     *
     * @param i the integer value
     * @return the number of bytes required to encode the integer
     */
    int stopBitLength(int i);

    /**
     * Returns the length in bytes required to encode a long integer using stop-bit encoding.
     *
     * @param l the long integer value
     * @return the number of bytes required to encode the long integer
     */
    int stopBitLength(long l);

    /**
     * Reads a partial value from the given byte array starting at the specified offset.
     *
     * @param bytes  the source byte array
     * @param offset the starting index in the byte array
     * @param length the number of bytes to read
     * @return the partial value read from the byte array
     */
    long partialRead(byte[] bytes, int offset, int length);

    /**
     * Reads a partial value from the memory at the given address.
     *
     * @param addr   the source memory address
     * @param length the number of bytes to read
     * @return the partial value read from the memory
     */
    long partialRead(long addr, int length);

    /**
     * Writes a partial value to the byte array at the specified offset.
     *
     * @param bytes  the destination byte array
     * @param offset the starting index in the byte array
     * @param value  the partial value to write
     * @param length the number of bytes to write
     */
    void partialWrite(byte[] bytes, int offset, long value, int length);

    /**
     * Writes a partial value to the memory at the given address.
     *
     * @param addr   the destination memory address
     * @param value  the partial value to write
     * @param length the number of bytes to write
     */
    void partialWrite(long addr, long value, int length);

    /**
     * Checks if the bytes in the specified byte array are 7-bit ASCII characters.
     *
     * @param bytes  the byte array
     * @param offset the starting index in the byte array
     * @param length the number of bytes to check
     * @return {@code true} if all bytes are 7-bit ASCII characters, {@code false} otherwise
     */
    boolean is7Bit(byte[] bytes, int offset, int length);

    /**
     * Checks if the characters in the specified char array are 7-bit ASCII characters.
     *
     * @param chars  the char array
     * @param offset the starting index in the char array
     * @param length the number of characters to check
     * @return {@code true} if all characters are 7-bit ASCII characters, {@code false} otherwise
     */
    boolean is7Bit(char[] chars, int offset, int length);

    /**
     * Checks if the bytes in the memory at the given address are 7-bit ASCII characters.
     *
     * @param address the memory address
     * @param length  the number of bytes to check
     * @return {@code true} if all bytes are 7-bit ASCII characters, {@code false} otherwise
     */
    boolean is7Bit(long address, int length);

    /**
     * Writes a long value to memory at the given address with ordered store semantics.
     *
     * @param address the memory address
     * @param i       the long value to write
     */
    void writeOrderedLong(long address, long i);

    /**
     * Writes a long value to the object at the given offset with ordered store semantics.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param i      the long value to write
     */
    void writeOrderedLong(Object object, long offset, long i);

    /**
     * Atomically sets the value at the specified address to the given value if it equals the expected value.
     * Throws an IllegalStateException If the value does not match the expected value.
     *
     * @param address  the memory address
     * @param offset   the offset in the memory address
     * @param expected the expected value
     * @param value    the new value
     * @throws IllegalStateException if the current value does not match the expected value
     */
    void testAndSetInt(long address, long offset, int expected, int value) throws IllegalStateException;

    /**
     * Atomically sets the value at the specified offset in the object to the given value if it equals the expected value.
     * Throws an IllegalStateException If the value does not match the expected value.
     *
     * @param object   the destination object
     * @param offset   the offset in the destination object
     * @param expected the expected value
     * @param value    the new value
     * @throws IllegalStateException if the current value does not match the expected value
     */
    void testAndSetInt(Object object, long offset, int expected, int value) throws IllegalStateException;

    /**
     * Compares the value at the specified memory address with the expected value and, if equal, sets it to the new value.
     * Returns {@code true} if the value was successfully swapped, {@code false} otherwise.
     *
     * @param address  the memory address
     * @param expected the expected value
     * @param value    the new value
     * @return {@code true} if the value was successfully swapped, {@code false} otherwise
     * @throws MisAlignedAssertionError if the memory address is misaligned for the operation
     */
    boolean compareAndSwapInt(long address, int expected, int value) throws MisAlignedAssertionError;

    /**
     * Compares the value at the specified offset in the object with the expected value and, if equal, sets it to the new value.
     * Returns {@code true} if the value was successfully swapped, {@code false} otherwise.
     *
     * @param object   the destination object
     * @param offset   the offset in the destination object
     * @param expected the expected value
     * @param value    the new value
     * @return {@code true} if the value was successfully swapped, {@code false} otherwise
     * @throws MisAlignedAssertionError if the offset is misaligned for the operation
     */
    boolean compareAndSwapInt(Object object, long offset, int expected, int value) throws MisAlignedAssertionError;

    /**
     * Compares the value at the specified memory address with the expected value and, if equal, sets it to the new value.
     * Returns {@code true} if the value was successfully swapped, {@code false} otherwise.
     *
     * @param address  the memory address
     * @param expected the expected value
     * @param value    the new value
     * @return {@code true} if the value was successfully swapped, {@code false} otherwise
     * @throws MisAlignedAssertionError if the memory address is misaligned for the operation
     */
    boolean compareAndSwapLong(long address, long expected, long value) throws MisAlignedAssertionError;

    /**
     * Compares the value at the specified offset in the object with the expected value and, if equal, sets it to the new value.
     * Returns {@code true} if the value was successfully swapped, {@code false} otherwise.
     *
     * @param object   the destination object
     * @param offset   the offset in the destination object
     * @param expected the expected value
     * @param value    the new value
     * @return {@code true} if the value was successfully swapped, {@code false} otherwise
     * @throws MisAlignedAssertionError if the offset is misaligned for the operation
     */
    boolean compareAndSwapLong(Object object, long offset, long expected, long value) throws MisAlignedAssertionError;

    /**
     * Atomically sets the value at the specified address to the given value and returns the previous value
     *
     * @param address the memory address
     * @param value   the new value
     * @return the previous value at the memory address
     * @throws MisAlignedAssertionError if the memory address is misaligned for the operation
     */
    int getAndSetInt(long address, int value) throws MisAlignedAssertionError;

    /**
     * Atomically sets the value at the specified offset in the object to the given value and returns the previous value.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param value  the new value
     * @return the previous value at the offset in the object
     * @throws MisAlignedAssertionError if the offset is misaligned for the operation
     */
    int getAndSetInt(Object object, long offset, int value) throws MisAlignedAssertionError;

    /**
     * Returns the page size of the underlying memory system.
     *
     * @return the page size
     */
    int pageSize();

    /**
     * Reads a volatile byte from the memory at the given address.
     *
     * @param address the memory address
     * @return the byte value read from the memory
     */
    byte readVolatileByte(long address);

    /**
     * Reads a volatile byte from the object at the given offset.
     *
     * @param object the source object
     * @param offset the offset in the object
     * @return the byte value read from the object
     */
    byte readVolatileByte(Object object, long offset);

    /**
     * Reads a volatile short from the memory at the given address.
     *
     * @param address the memory address
     * @return the short value read from the memory
     */
    short readVolatileShort(long address);

    /**
     * Reads a volatile short from the object at the given offset.
     *
     * @param object the source object
     * @param offset the offset in the object
     * @return the short value read from the object
     */
    short readVolatileShort(Object object, long offset);

    /**
     * Reads a volatile integer from the memory at the given address.
     *
     * @param address the memory address
     * @return the integer value read from the memory
     */
    int readVolatileInt(long address);

    /**
     * Reads a volatile integer from the object at the given offset.
     *
     * @param object the source object
     * @param offset the offset in the object
     * @return the integer value read from the object
     */
    int readVolatileInt(Object object, long offset);

    /**
     * Reads a volatile float from the memory at the given address.
     *
     * @param address the memory address
     * @return the float value read from the memory
     */
    float readVolatileFloat(long address);

    /**
     * Reads a volatile float from the object at the given offset.
     *
     * @param object the source object
     * @param offset the offset in the object
     * @return the float value read from the object
     */
    float readVolatileFloat(Object object, long offset);

    /**
     * Reads a volatile long from the memory at the given address.
     *
     * @param address the memory address
     * @return the long value read from the memory
     */
    long readVolatileLong(long address);

    /**
     * Reads a volatile long from the object at the given offset.
     *
     * @param object the source object
     * @param offset the offset in the object
     * @return the long value
     * <p>
     * read from the object
     */
    long readVolatileLong(Object object, long offset);

    /**
     * Reads a volatile double from the memory at the given address.
     *
     * @param address the memory address
     * @return the double value read from the memory
     */
    double readVolatileDouble(long address);

    /**
     * Reads a volatile double from the object at the given offset.
     *
     * @param object the source object
     * @param offset the offset in the object
     * @return the double value read from the object
     */
    double readVolatileDouble(Object object, long offset);

    /**
     * Writes a volatile byte to the memory at the given address.
     *
     * @param address the memory address
     * @param b       the byte value to write
     */
    void writeVolatileByte(long address, byte b);

    /**
     * Writes a volatile byte to the object at the given offset.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param b      the byte value to write
     */
    void writeVolatileByte(Object object, long offset, byte b);

    /**
     * Writes a volatile short to the memory at the given address.
     *
     * @param address the memory address
     * @param i16     the short value to write
     */
    void writeVolatileShort(long address, short i16);

    /**
     * Writes a volatile short to the object at the given offset.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param i16    the short value to write
     */
    void writeVolatileShort(Object object, long offset, short i16);

    /**
     * Writes a volatile integer to the memory at the given address.
     *
     * @param address the memory address
     * @param i32     the integer value to write
     */
    void writeVolatileInt(long address, int i32);

    /**
     * Writes a volatile integer to the object at the given offset.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param i32    the integer value to write
     */
    void writeVolatileInt(Object object, long offset, int i32);

    /**
     * Writes a volatile float to the memory at the given address.
     *
     * @param address the memory address
     * @param f       the float value to write
     */
    void writeVolatileFloat(long address, float f);

    /**
     * Writes a volatile float to the object at the given offset.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param f      the float value to write
     */
    void writeVolatileFloat(Object object, long offset, float f);

    /**
     * Writes a volatile long to the memory at the given address.
     *
     * @param address the memory address
     * @param i64     the long value to write
     */
    void writeVolatileLong(long address, long i64);

    /**
     * Writes a volatile long to the object at the given offset.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param i64    the long value to write
     */
    void writeVolatileLong(Object object, long offset, long i64);

    /**
     * Writes a volatile double to the memory at the given address.
     *
     * @param address the memory address
     * @param d       the double value to write
     */
    void writeVolatileDouble(long address, double d);

    /**
     * Writes a volatile double to the object at the given offset.
     *
     * @param object the destination object
     * @param offset the offset in the destination object
     * @param d      the double value to write
     */
    void writeVolatileDouble(Object object, long offset, double d);

    /**
     * Adds the specified increment to the value at the given memory address and returns the updated value.
     *
     * @param address   the memory address
     * @param increment the value to add
     * @return the updated value at the memory address
     * @throws MisAlignedAssertionError if the memory address is misaligned for the operation
     */
    int addInt(long address, int increment) throws MisAlignedAssertionError;

    /**
     * Adds the specified increment to the value at the given offset in the object and returns the updated value.
     *
     * @param object    the destination object
     * @param offset    the offset in the destination object
     * @param increment the value to add
     * @return the updated value at the offset in the object
     */
    int addInt(Object object, long offset, int increment);

    /**
     * Adds the specified increment to the value at the given memory address and returns the updated value.
     *
     * @param address   the memory address
     * @param increment the value to add
     * @return the updated value at the memory address
     * @throws MisAlignedAssertionError if the memory address is misaligned for the operation
     */
    long addLong(long address, long increment) throws MisAlignedAssertionError;

    /**
     * Adds the specified increment to the value at the given offset in the object and returns the updated value.
     *
     * @param object    the destination object
     * @param offset    the offset in the destination object
     * @param increment the value to add
     * @return the updated value at the offset in the object
     * @throws MisAlignedAssertionError if the offset is misaligned for the operation
     */
    long addLong(Object object, long offset, long increment) throws MisAlignedAssertionError;

    /**
     * Allocates and returns a new instance of the specified class.
     *
     * @param clazz the class to instantiate
     * @param <E>   the type of the class
     * @return a new instance of the specified class
     * @throws InstantiationException If the class cannot be instantiated
     */
    @NotNull <E> E allocateInstance(Class<? extends E> clazz) throws InstantiationException;

    /**
     * Returns the offset of the specified field in its containing object.
     *
     * @param field the field
     * @return the offset of the field
     */
    long getFieldOffset(Field field);

    /**
     * Sets the value of the specified field in the given object.
     *
     * @param o      the object
     * @param offset the offset of the field in the object
     * @param value  the new value of the field
     */
    void putObject(@NotNull Object o, long offset, Object value);

    /**
     * Returns the value of the specified field in the given object.
     *
     * @param o      the object
     * @param offset the offset of the field in the object
     * @param <T>    the type of the field
     * @return the value of the field
     */
    @NotNull <T> T getObject(@NotNull Object o, long offset);

    /**
     * Returns the base offset of the elements in the specified array type.
     *
     * @param type the array type
     * @return the base offset of the array type
     */
    int arrayBaseOffset(Class<?> type);

    /**
     * Returns the offset of the specified field in its containing object.
     *
     * @param field the field
     * @return the offset of the field
     */
    long objectFieldOffset(Field field);

    /**
     * @param type of primitive or a reference
     * @return the number of bytes this type uses.
     */
    static int sizeOf(Class<?> type) {
        if (type == void.class) return 0;
        return type == boolean.class || type == byte.class ? 1
                : type == short.class || type == char.class ? 2
                : type == int.class || type == float.class ? 4
                : type == long.class || type == double.class ? 8
                : Unsafe.ARRAY_OBJECT_INDEX_SCALE;
    }

    /**
     * Returns if the provided {@code addr} would provide access to
     * an int that would reside entirely within a distinct cache-line.
     *
     * @param addr to use
     * @return if int access would be confined to the same cache-line
     */
    boolean safeAlignedInt(long addr);

    /**
     * Returns if the provided {@code addr} would provide access to
     * a long that would reside entirely within a distinct cache-line.
     *
     * @param addr to use
     * @return if long access would be confined to the same cache-line
     */
    boolean safeAlignedLong(long addr);

    /**
     * Returns the memory address associated with the given ByteBuffer.
     *
     * @param bb the ByteBuffer
     * @return the memory address associated with the ByteBuffer
     */
    long address(ByteBuffer bb);
}
