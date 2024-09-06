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

import net.openhft.chronicle.core.internal.Bootstrap;
import net.openhft.chronicle.core.internal.util.DirectBufferUtil;
import net.openhft.chronicle.core.util.Ints;
import net.openhft.chronicle.core.util.Longs;
import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.nonNull;
import static net.openhft.chronicle.assertions.AssertUtil.SKIP_ASSERTIONS;
import static net.openhft.chronicle.core.util.Ints.assertIfEnabled;
import static net.openhft.chronicle.core.util.Longs.assertIfEnabled;
import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

@SuppressWarnings("unchecked")
/**
 * UnsafeMemory is a class that provides efficient, low-level operations for direct memory manipulation. It
 * serves as a wrapper around the sun.misc.Unsafe API, providing a more user-friendly interface for common
 * operations like reading, writing, and performing atomic operations on memory.
 * <p>
 * Most methods in this class are available in both standard and volatile versions, ensuring proper
 * synchronization across different threads. These volatile versions are especially important in multi-threaded
 * environments where visibility of changes across threads is necessary for correct application behavior.
 * <p>
 * Caution is advised when using this class. As it provides direct access to memory, misuse can lead to elusive
 * bugs, system instability, or crashes. Proper handling of memory addresses is of paramount importance when
 * using this class.
 * <p>
 * Additionally, this class is equipped to handle changes in the internal implementation of the String class
 * across different Java versions. From Java 9 onwards, Strings are internally stored as bytes rather than chars.
 * Therefore, appropriate methods have been introduced to deal with these implementation-specific details,
 * thereby offering a uniform interface for memory operations irrespective of the Java version.
 *
 * @see UnsafeMemory.ARMMemory
 */
public class UnsafeMemory implements Memory {

    /**
     * A constant for the Unsafe object.
     */
    @NotNull
    public static final Unsafe UNSAFE;

    /**
     * Singleton instance of UnsafeMemory for use in memory operations.
     */
    public static final UnsafeMemory INSTANCE;

    /**
     * Alias for the singleton instance of UnsafeMemory.
     */
    public static final UnsafeMemory MEMORY;

    // see java.nio.Bits.copyMemory
    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safepoint polling
    // during a large copy
    static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    // TODO support big endian
    /**
     * Indicates if the current platform is little-endian.
     */
    public static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    // Create a local copy of type long (instead of int) to optimize performance
    private static final long ARRAY_BYTE_BASE_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;
    private static final long ARRAY_CHAR_BASE_OFFSET = Unsafe.ARRAY_CHAR_BASE_OFFSET;

    private static final String CANNOT_CHANGE_AT = "Cannot change at ";
    private static final String WAS = " was ";
    private static final String EXPECTED = " expected ";

    static {
        try {
            // Access the Unsafe object by reflection.
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (@NotNull NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new AssertionError(e);
        }
        // Initialize INSTANCE based on the architecture or Java version.
        INSTANCE = Bootstrap.isArm0() ? new ARMMemory() : new UnsafeMemory();
        MEMORY = INSTANCE;
    }

    /**
     * Tracks the amount of native memory used.
     */
    private final AtomicLong nativeMemoryUsed = new AtomicLong();

    /**
     * Interface for copying memory from one object to another using memory addresses.
     */
    private final ObjectToAddress copyMemoryObjectToAddress;

    /**
     * Creates a new instance of UnsafeMemory.
     */
    public UnsafeMemory() {
        // Initialize the memory copying mechanism based on Java version or architecture.
        copyMemoryObjectToAddress = (Bootstrap.isJava9Plus() || Bootstrap.isArm0()) ?
                (src, srcOffset, dest, length) -> copyMemoryLoop(src, srcOffset, null, dest, length) :
                (src, srcOffset, dest, length) -> copyMemory0(src, srcOffset, null, dest, length);
    }

    /**
     * Retry the operation to read a volatile integer at a memory address until a consistent value is read.
     *
     * @param address the memory address.
     * @param value   the expected value.
     * @return the consistent value read.
     */
    private static int retryReadVolatileInt(long address, int value) {
        // Ensure the address is valid when assertions are enabled.
        assert SKIP_ASSERTIONS || address != 0;

        // Read the volatile integer from the specified memory address.
        int value2 = UNSAFE.getIntVolatile(null, address);

        // Loop until the read value matches the expected value.
        while (value2 != value) {
            // Log a warning if the value does not match and is not a special value.
            if (value != 0 && value != 0x80000000)
                Jvm.warn().on(UnsafeMemory.class, "Int@" + Long.toHexString(address) + " (" + (address & 63) + ") " +
                        "was " + Integer.toHexString(value) +
                        " is now " + Integer.toHexString(value2));

            // Update the expected value and re-read.
            value = value2;
            value2 = UNSAFE.getIntVolatile(null, address);
        }

        // Return the consistent value.
        return value;
    }

    /**
     * Retry the operation to read a volatile long at a memory address until a consistent value is read.
     *
     * @param address the memory address.
     * @param value   the expected value.
     * @return the consistent value read.
     */
    private static long retryReadVolatileLong(long address, long value) {
        assert SKIP_ASSERTIONS || address != 0;
        long value2 = UNSAFE.getLongVolatile(null, address);
        while (value2 != value) {
            if (value != 0)
                Jvm.warn().on(UnsafeMemory.class, "please add padding() when using concurrent writers, " +
                        "Long@" +
                        Long.toHexString(address) + " (" + (address & 63) + ") " +
                        "was " + Long.toHexString(value) +
                        " is now " + Long.toHexString(value2));
            value = value2;
            value2 = UNSAFE.getLongVolatile(null, address);
        }
        return value;
    }

    /**
     * Puts an integer value into the byte array at the specified offset.
     *
     * @param bytes  the byte array.
     * @param offset the offset at which to insert the value.
     * @param value  the integer value to insert.
     */
    public static void putInt(byte[] bytes, int offset, int value) {
        assert SKIP_ASSERTIONS || nonNull(bytes);
        assert SKIP_ASSERTIONS || Ints.betweenZeroAndReserving().test(offset, bytes.length, Integer.BYTES);
        UnsafeMemory.UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset, value);
    }

    /**
     * Establishes a happens-before relationship, without any corresponding guarantee of visibility.
     * <p>
     * Can be used to prevent reordering of instructions by the compiler or processor.
     */
    public static void unsafeStoreFence() {
        UNSAFE.storeFence();
    }

    /**
     * Establishes a load-load and load-store barrier.
     * <p>
     * Can be used to prevent reordering of load instructions by the compiler or processor.
     */
    public static void unsafeLoadFence() {
        UNSAFE.loadFence();
    }

    /**
     * Fetches a long value from the memory location at the given address.
     *
     * @param address memory address to fetch the long value from.
     * @return the long value present at the given memory address.
     */
    public static long unsafeGetLong(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getLong(address);
    }

    /**
     * Fetches an integer value from the memory location at the given address.
     *
     * @param address memory address to fetch the integer value from.
     * @return the integer value present at the given memory address.
     */
    public static int unsafeGetInt(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getInt(address);
    }

    /**
     * Fetches a byte value from the memory location at the given address.
     *
     * @param address memory address to fetch the byte value from.
     * @return the byte value present at the given memory address.
     */
    public static byte unsafeGetByte(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getByte(address);
    }

    /**
     * Puts the provided long {@code value} at the specified memory {@code address}.
     *
     * @param address memory address where the value is to be put.
     * @param value   the long value to put.
     */
    public static void unsafePutLong(long address, long value) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putLong(address, value);
    }

    /**
     * Puts the provided int {@code value} at the specified memory {@code address}.
     *
     * @param address memory address where the value is to be put.
     * @param value   the int value to put.
     */
    public static void unsafePutInt(long address, int value) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putInt(address, value);
    }

    /**
     * Puts the provided byte {@code value} at the specified memory {@code address}.
     *
     * @param address memory address where the value is to be put.
     * @param value   the byte value to put.
     */
    public static void unsafePutByte(long address, byte value) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putByte(address, value);
    }

    /**
     * Puts the provided {@code value} into the provided {@code bytes} array at the provided byte {@code offset}.
     *
     * @param bytes  non-null byte array.
     * @param offset in the provided bytes where the value is written
     * @param value  to put
     */
    public static void unsafePutLong(byte[] bytes, int offset, long value) {
        assert SKIP_ASSERTIONS || nonNull(bytes);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.betweenZeroAndReserving(), offset, bytes.length, Long.BYTES);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset, value);
    }

    /**
     * Puts the provided {@code value} into the provided {@code bytes} array at the provided byte {@code offset}.
     *
     * @param bytes  non-null byte array.
     * @param offset in the provided bytes where the value is written
     * @param value  to put
     */
    public static void unsafePutInt(byte[] bytes, int offset, int value) {
        assert SKIP_ASSERTIONS || nonNull(bytes);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.betweenZeroAndReserving(), offset, bytes.length, Integer.BYTES);
        UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset, value);
    }

    /**
     * Puts the provided {@code value} into the provided {@code bytes} array at the provided byte {@code offset}.
     *
     * @param bytes  non-null byte array.
     * @param offset in the provided bytes where the value is written
     * @param value  to put
     */
    public static void unsafePutByte(byte[] bytes, int offset, byte value) {
        assert SKIP_ASSERTIONS || nonNull(bytes);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.betweenZeroAndReserving(), offset, bytes.length, Byte.BYTES);
        UNSAFE.putByte(bytes, ARRAY_BYTE_BASE_OFFSET + offset, value);
    }

    /**
     * Copies memory from one address to another. The regions should not overlap.
     *
     * @param from   the starting address to copy memory from.
     * @param to     the starting address to copy memory to.
     * @param length the amount of memory to copy, in bytes.
     */
    public static void copyMemory(long from, long to, int length) {
        MEMORY.copyMemory(from, to, (long) length);
    }

    /**
     * Sets the value of a boolean field or array element within an object at the given offset.
     *
     * @param obj    the object containing the field or array element, or null for static fields.
     * @param offset the offset to the field or array element.
     * @param value  the new value.
     */
    public static void unsafePutBoolean(Object obj, long offset, boolean value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putBoolean(obj, offset, value);
    }

    /**
     * Retrieves the value of a boolean field or array element within an object at the given offset.
     *
     * @param obj    the object containing the field or array element, or null for static fields.
     * @param offset the offset to the field or array element.
     * @return the value of the boolean field or array element.
     */
    public static boolean unsafeGetBoolean(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getBoolean(obj, offset);
    }

    /**
     * Fetches a byte value from the given object at the specified {@code offset}.
     *
     * @param obj    the object containing the byte to fetch.
     * @param offset the offset to the byte within the object.
     */
    public static void unsafePutByte(Object obj, long offset, byte value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putByte(obj, offset, value);
    }

    /**
     * Puts the provided byte {@code value} into the given object at the specified {@code offset}.
     *
     * @param obj    the object in which to put the byte.
     * @param offset the offset at which to put the byte within the object.
     */
    public static byte unsafeGetByte(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getByte(obj, offset);
    }

    /**
     * Sets the value of a char field or array element within an object at the given offset.
     *
     * @param obj    the object containing the field or array element, or null for static fields.
     * @param offset the offset to the field or array element.
     * @param value  the new value.
     */
    public static void unsafePutChar(Object obj, long offset, char value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putChar(obj, offset, value);
    }

    /**
     * Fetches a char value from the given object at the specified {@code offset}.
     *
     * @param obj    the object containing the char to fetch.
     * @param offset the offset to the char within the object.
     * @return the fetched char value.
     */
    public static char unsafeGetChar(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getChar(obj, offset);
    }

    /**
     * Puts the provided short {@code value} into the given object at the specified {@code offset}.
     *
     * @param obj    the object in which to put the short.
     * @param offset the offset at which to put the short within the object.
     * @param value  the short value to put.
     */
    public static void unsafePutShort(Object obj, long offset, short value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putShort(obj, offset, value);
    }

    /**
     * Fetches a short value from the given object at the specified {@code offset}.
     *
     * @param obj    the object containing the short to fetch.
     * @param offset the offset to the short within the object.
     * @return the fetched short value.
     */
    public static short unsafeGetShort(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getShort(obj, offset);
    }

    /**
     * Puts the provided int {@code value} into the given object at the specified {@code offset}.
     *
     * @param obj    the object in which to put the int.
     * @param offset the offset at which to put the int within the object.
     * @param value  the int value to put.
     */
    public static void unsafePutInt(Object obj, long offset, int value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putInt(obj, offset, value);
    }

    /**
     * Fetches an int value from the given object at the specified {@code offset}.
     *
     * @param obj    the object containing the int to fetch.
     * @param offset the offset to the int within the object.
     * @return the fetched int value.
     */
    public static int unsafeGetInt(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getInt(obj, offset);
    }

    /**
     * Puts the provided float {@code value} into the given object at the specified {@code offset}.
     *
     * @param obj    the object in which to put the float.
     * @param offset the offset at which to put the float within the object.
     * @param value  the float value to put.
     */
    public static void unsafePutFloat(Object obj, long offset, float value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putFloat(obj, offset, value);
    }

    /**
     * Fetches a float value from the given object at the specified {@code offset}.
     *
     * @param obj    the object containing the float to fetch.
     * @param offset the offset to the float within the object.
     * @return the fetched float value.
     */
    public static float unsafeGetFloat(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getFloat(obj, offset);
    }

    /**
     * Puts the provided long {@code value} into the given object at the specified {@code offset}.
     *
     * @param obj    the object in which to put the long.
     * @param offset the offset at which to put the long within the object.
     * @param value  the long value to put.
     */
    public static void unsafePutLong(Object obj, long offset, long value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putLong(obj, offset, value);
    }

    /**
     * Fetches a long value from the given object at the specified {@code offset}.
     *
     * @param obj    the object containing the long to fetch.
     * @param offset the offset to the long within the object.
     * @return the fetched long value.
     */
    public static long unsafeGetLong(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getLong(obj, offset);
    }

    /**
     * Puts the provided double {@code value} into the given object at the specified {@code offset}.
     *
     * @param obj    the object in which to put the double.
     * @param offset the offset at which to put the double within the object.
     * @param value  the double value to put.
     */
    public static void unsafePutDouble(Object obj, long offset, double value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putDouble(obj, offset, value);
    }

    /**
     * Fetches a double value from the given object at the specified {@code offset}.
     *
     * @param obj    the object containing the double to fetch.
     * @param offset the offset to the double within the object.
     * @return the fetched double value.
     */
    public static double unsafeGetDouble(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getDouble(obj, offset);
    }

    /**
     * Puts the provided {@code value} into the given object at the specified {@code offset}.
     *
     * @param obj    the object in which to put the value.
     * @param offset the offset at which to put the value within the object.
     * @param value  the value to put.
     */
    public static void unsafePutObject(Object obj, long offset, Object value) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putObject(obj, offset, value);
    }

    /**
     * Fetches an object of type {@code T} from the given object at the specified {@code offset}.
     *
     * @param <T>    the type of the object to fetch.
     * @param obj    the object containing the object to fetch.
     * @param offset the offset to the object within the object.
     * @return the fetched object.
     */
    public static <T> T unsafeGetObject(Object obj, long offset) {
        assert SKIP_ASSERTIONS || obj == null || assertIfEnabled(Longs.nonNegative(), offset);
        return (T) UNSAFE.getObject(obj, offset);
    }

    /**
     * Retrieves the offset of the provided field within its class or interface.
     *
     * @param field the field whose offset should be fetched.
     * @return the offset of the field.
     */
    public static long unsafeObjectFieldOffset(Field field) {
        assert SKIP_ASSERTIONS || nonNull(field);
        return UNSAFE.objectFieldOffset(field);
    }

    /**
     * Allocates an instance of the provided class without invoking its constructor.
     *
     * @param <E>   the type of the instance to allocate.
     * @param clazz the class to allocate an instance of.
     * @return an instance of the provided class.
     * @throws InstantiationException if an instance of the class or interface represented by the specified {@code Class} object could not be instantiated.
     */
    @NotNull
    @Override
    public <E> E allocateInstance(Class<? extends E> clazz) throws InstantiationException {
        assert SKIP_ASSERTIONS || nonNull(clazz);
        @NotNull
        E e = (E) UNSAFE.allocateInstance(clazz);
        return e;
    }

    /**
     * Retrieves the offset of the provided field within its class or interface.
     *
     * @param field the field whose offset should be fetched.
     * @return the offset of the field.
     */
    @Override
    public long getFieldOffset(Field field) {
        assert SKIP_ASSERTIONS || nonNull(field);
        return UNSAFE.objectFieldOffset(field);
    }

    /**
     * Puts the provided {@code value} into the given {@code object} at the specified {@code offset}.
     *
     * @param object the object in which to put the value.
     * @param offset the offset at which to put the value within the object.
     * @param value  the value to put.
     */
    @Override
    public void putObject(@NotNull Object object, long offset, Object value) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putObject(requireNonNull(object), offset, value);
    }

    /**
     * Fetches an object of type {@code T} from the given object at the specified {@code offset}.
     *
     * @param <T>    the type of the object to fetch.
     * @param object the object containing the object to fetch.
     * @param offset the offset to the object within the object.
     * @return the fetched object.
     */
    @NotNull
    @Override
    public <T> T getObject(@NotNull Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return (T) UNSAFE.getObject(requireNonNull(object), offset);
    }

    /**
     * Ensures that all writes made by the current thread are visible to other threads.
     */
    @Override
    public void storeFence() {
        UNSAFE.storeFence();
    }

    /**
     * Ensures that all reads made by the current thread are visible to other threads.
     */
    @Override
    public void loadFence() {
        UNSAFE.loadFence();
    }

    /**
     * Sets a given amount of memory, starting at the provided address, to a specified byte value.
     *
     * @param address the starting address.
     * @param size    the amount of memory to set.
     * @param b       the byte value to set.
     */
    @Override
    public void setMemory(long address, long size, byte b) {
        assert SKIP_ASSERTIONS || address != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), size);
        UNSAFE.setMemory(address, size, b);
    }

    /**
     * Sets a given amount of memory within an object, starting at the provided offset, to a specified byte value.
     *
     * @param o      the object containing the memory to set.
     * @param offset the offset to the start of the memory within the object.
     * @param size   the amount of memory to set.
     * @param b      the byte value to set.
     */
    @Override
    public void setMemory(Object o, long offset, long size, byte b) {
        assert SKIP_ASSERTIONS || offset != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), size);
        UNSAFE.setMemory(o, offset, size, b);
    }

    /**
     * Releases the specified amount of memory starting from the given address.
     *
     * @param address the starting address.
     * @param size    the amount of memory to free.
     */
    @Override
    public void freeMemory(long address, long size) {
        assert SKIP_ASSERTIONS || address != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), size);
        if (address != 0)
            UNSAFE.freeMemory(address);
        nativeMemoryUsed.addAndGet(-size);
    }

    /**
     * Allocates a block of native memory of the given capacity.
     *
     * @param capacity the size of the memory block to allocate.
     * @return the address of the allocated memory block.
     * @throws AssertionError   if the requested capacity is not positive.
     * @throws OutOfMemoryError if not enough free native memory is available.
     */
    @Override
    public long allocate(long capacity) {
        if (capacity <= 0)
            throw new AssertionError("Invalid capacity: " + capacity);
        long address = UNSAFE.allocateMemory(capacity);
        if (address == 0)
            throw new OutOfMemoryError("Not enough free native memory, capacity attempted: " + capacity / 1024 + " KiB");

        nativeMemoryUsed.addAndGet(capacity);

        return address;
    }

    /**
     * Retrieves the total amount of native memory used by the application.
     *
     * @return the amount of native memory used.
     */
    @Override
    public long nativeMemoryUsed() {
        return nativeMemoryUsed.get();
    }

    /**
     * Writes a byte to the given memory address.
     *
     * @param address the memory address.
     * @param b       the byte to be written.
     */
    @Override
    public void writeByte(long address, byte b) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putByte(address, b);
    }

    /**
     * Writes a byte to the given memory offset of an object.
     *
     * @param object the object containing the memory to write to.
     * @param offset the offset in the object's memory.
     * @param b      the byte to be written.
     */
    @Override
    public void writeByte(Object object, long offset, byte b) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putByte(object, offset, b);
    }

    /**
     * Reads a byte from the given memory offset of an object.
     *
     * @param object the object containing the memory to read from.
     * @param offset the offset in the object's memory.
     * @return the byte read.
     */
    @Override
    public byte readByte(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getByte(object, offset);
    }

    /**
     * Writes a byte array to the given memory address.
     *
     * @param address the memory address.
     * @param b       the byte array to be written.
     * @param offset  the starting offset in the byte array.
     * @param length  the number of bytes to write.
     * @throws IllegalArgumentException if offset + length exceeds the byte array's length.
     */
    @Override
    public void writeBytes(long address, byte[] b, int offset, int length) throws IllegalArgumentException {
        assert SKIP_ASSERTIONS || address != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), offset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        if (offset + length > b.length)
            throw new IllegalArgumentException("Invalid offset or length, array's length is " + b.length);
        UnsafeMemory.UNSAFE.copyMemory(b, ARRAY_BYTE_BASE_OFFSET + offset, null, address, length);
    }

    /**
     * Reads bytes from the given memory address into a byte array.
     *
     * @param address the memory address.
     * @param b       the byte array to be filled.
     * @param offset  the starting offset in the byte array.
     * @param length  the number of bytes to read.
     * @throws IllegalArgumentException if offset + length exceeds the byte array's length.
     */
    @Override
    public void readBytes(long address, byte[] b, long offset, int length) throws IllegalArgumentException {
        assert SKIP_ASSERTIONS || address != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        if (offset + length > b.length)
            throw new IllegalArgumentException("Invalid offset or length, array's length is " + b.length);
        UnsafeMemory.UNSAFE.copyMemory(null, address, b, ARRAY_BYTE_BASE_OFFSET + offset, length);
    }

    /**
     * Reads a byte from the given memory address.
     *
     * @param address the memory address.
     * @return the byte read.
     */
    @Override
    public byte readByte(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getByte(address);
    }

    /**
     * Writes a short value to the given memory address.
     *
     * @param address the memory address.
     * @param i16     the short value to be written.
     */
    @Override
    public void writeShort(long address, short i16) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putShort(address, i16);
    }

    /**
     * Writes a short value to the given memory offset of an object.
     *
     * @param object the object containing the memory to write to.
     * @param offset the offset in the object's memory.
     * @param i16    the short value to be written.
     */
    @Override
    public void writeShort(Object object, long offset, short i16) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putShort(object, offset, i16);
    }

    /**
     * Reads a short value from the given memory address.
     *
     * @param address the memory address.
     * @return the short value read.
     */
    @Override
    public short readShort(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getShort(address);
    }

    /**
     * Reads a short value from the given memory offset of an object.
     *
     * @param object the object containing the memory to read from.
     * @param offset the offset in the object's memory.
     * @return the short value read.
     */
    @Override
    public short readShort(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getShort(object, offset);
    }

    /**
     * Writes an int value to the given memory address.
     *
     * @param address the memory address.
     * @param i32     the int value to be written.
     */
    @Override
    public void writeInt(long address, int i32) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putInt(address, i32);
    }

    /**
     * Writes an int value to the given memory offset of an object.
     *
     * @param object the object containing the memory to write to.
     * @param offset the offset in the object's memory.
     * @param i32    the int value to be written.
     */
    @Override
    public void writeInt(Object object, long offset, int i32) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putInt(object, offset, i32);
    }

    /**
     * Writes an int value to the given memory address ensuring the order of memory operations.
     *
     * @param address the memory address.
     * @param i32     the int value to be written.
     */
    @Override
    public void writeOrderedInt(long address, int i32) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putOrderedInt(null, address, i32);
    }

    /**
     * Writes an int value to the given memory offset of an object ensuring the order of memory operations.
     *
     * @param object the object containing the memory to write to.
     * @param offset the offset in the object's memory.
     * @param i32    the int value to be written.
     */
    @Override
    public void writeOrderedInt(Object object, long offset, int i32) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putOrderedInt(object, offset, i32);
    }

    /**
     * Reads an int value from the given memory address.
     *
     * @param address the memory address.
     * @return the int value read.
     */
    @Override
    public int readInt(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getInt(address);
    }

    /**
     * Reads an int value from the given memory offset of an object.
     *
     * @param object the object containing the memory to read from.
     * @param offset the offset in the object's memory.
     * @return the int value read.
     */
    @Override
    public int readInt(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getInt(object, offset);
    }

    /**
     * Writes a long value to the given memory address.
     *
     * @param address the memory address.
     * @param i64     the long value to be written.
     */
    @Override
    public void writeLong(long address, long i64) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putLong(address, i64);
    }

    /**
     * Writes a long value to the given memory offset of an object.
     *
     * @param object the object containing the memory to write to.
     * @param offset the offset in the object's memory.
     * @param i64    the long value to be written.
     */
    @Override
    public void writeLong(Object object, long offset, long i64) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putLong(object, offset, i64);
    }

    /**
     * Reads a long value from the given memory address.
     *
     * @param address the memory address.
     * @return the long value read.
     */
    @Override
    public long readLong(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getLong(address);
    }

    /**
     * Reads a long value from the given memory offset of an object.
     *
     * @param object the object containing the memory to read from.
     * @param offset the offset in the object's memory.
     * @return the long value read.
     */
    @Override
    public long readLong(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getLong(object, offset);
    }

    /**
     * Writes a float value to the given memory address.
     *
     * @param address the memory address.
     * @param f       the float value to be written.
     */
    @Override
    public void writeFloat(long address, float f) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putFloat(address, f);
    }

    /**
     * Writes a float value to the given memory offset of an object.
     *
     * @param object the object containing the memory to write to.
     * @param offset the offset in the object's memory.
     * @param f      the float value to be written.
     */
    @Override
    public void writeFloat(Object object, long offset, float f) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putFloat(object, offset, f);
    }

    /**
     * Reads a float value from the given memory address.
     *
     * @param address the memory address.
     * @return the float value read.
     */
    @Override
    public float readFloat(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getFloat(address);
    }

    /**
     * Reads a float value from the given memory offset of an object.
     *
     * @param object the object containing the memory to read from.
     * @param offset the offset in the object's memory.
     * @return the float value read.
     */
    @Override
    public float readFloat(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getFloat(object, offset);
    }

    /**
     * Writes a double value to the given memory address.
     *
     * @param address the memory address.
     * @param d       the double value to be written.
     */
    @Override
    public void writeDouble(long address, double d) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putDouble(address, d);
    }

    /**
     * Writes a double value to the given memory offset of an object.
     *
     * @param object the object containing the memory to write to.
     * @param offset the offset in the object's memory.
     * @param d      the double value to be written.
     */
    @Override
    public void writeDouble(Object object, long offset, double d) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putDouble(object, offset, d);
    }

    /**
     * Reads a double value from the given memory address.
     *
     * @param address the memory address.
     * @return the double value read.
     */
    @Override
    public double readDouble(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getDouble(address);
    }

    /**
     * Reads a double value from the given memory offset of an object.
     *
     * @param object the object containing the memory to read from.
     * @param offset the offset in the object's memory.
     * @return the double value read.
     */
    @Override
    public double readDouble(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getDouble(object, offset);
    }

    /**
     * Copies memory from a byte array to a given memory address.
     *
     * @param src         source byte array.
     * @param srcOffset   offset of the source array from where to start copying.
     * @param destAddress destination memory address.
     * @param length      the length of memory to copy.
     */
    @Override
    public void copyMemory(byte[] src, int srcOffset, long destAddress, int length) {
        final long offset2 = ARRAY_BYTE_BASE_OFFSET + srcOffset;
        copyMemory(src, offset2, destAddress, length);
    }

    /**
     * Copies memory from a given memory address to another.
     *
     * @param srcAddress  source memory address.
     * @param destAddress destination memory address.
     * @param length      the length of memory to copy.
     */
    @Override
    public void copyMemory(long srcAddress, long destAddress, long length) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcAddress);
        assert SKIP_ASSERTIONS || destAddress != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);
        if (length < UNSAFE_COPY_THRESHOLD) {
            UNSAFE.copyMemory(srcAddress, destAddress, length);
        } else {
            copyMemory0(null, srcAddress, null, destAddress, length);
        }
    }

    /**
     * Copies memory from a byte array to an object.
     *
     * @param src        source byte array.
     * @param srcOffset  offset of the source array from where to start copying.
     * @param dest       destination object.
     * @param destOffset offset of the destination object from where to place the copied memory.
     * @param length     the length of memory to copy.
     */
    @Override
    public void copyMemory(byte[] src, int srcOffset, @Nullable Object dest, long destOffset, int length) {
        assert SKIP_ASSERTIONS || nonNull(src);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);

        if (dest instanceof byte[]) {
            copyMemory(src, srcOffset, (byte[]) dest, Math.toIntExact(destOffset - ARRAY_BYTE_BASE_OFFSET), length);
        } else {
            copyMemoryLoop(src, ARRAY_BYTE_BASE_OFFSET + srcOffset, dest, destOffset, length);
        }
    }

    /**
     * Copies memory from one byte array to another.
     *
     * @param src        source byte array.
     * @param srcOffset  offset of the source array from where to start copying.
     * @param dest       destination byte array.
     * @param destOffset offset of the destination array from where to place the copied memory.
     * @param length     the length of memory to copy.
     */
    public void copyMemory(byte[] src, int srcOffset, byte[] dest, int destOffset, int length) {
        assert SKIP_ASSERTIONS || nonNull(src);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcOffset);
        assert SKIP_ASSERTIONS || nonNull(dest);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);
        final long offsetB = ARRAY_BYTE_BASE_OFFSET + srcOffset;
        final long offset2B = ARRAY_BYTE_BASE_OFFSET + destOffset;
        if (length < UNSAFE_COPY_THRESHOLD) {
            UNSAFE.copyMemory(src, offsetB, dest, offset2B, length);
        } else {
            copyMemory0(src, offsetB, dest, offset2B, length);
        }
    }

    /**
     * Copies memory from an object to a given memory address.
     *
     * @param src         source object.
     * @param srcOffset   offset of the source object from where to start copying.
     * @param destAddress destination memory address.
     * @param length      the length of memory to copy.
     */
    @Override
    public void copyMemory(@Nullable Object src, long srcOffset, long destAddress, int length) {
        copyMemoryObjectToAddress.apply(src, srcOffset, destAddress, length);
    }

    /**
     * Copies memory from one object to another.
     * If either {code o} or {code o2} are {code null}, {code offset} or {code offset2} are treated as
     * addresses. Instead of calling this with {code o == o2 == null}, use {@link #copyMemory(long, long, int)}
     *
     * @param src        source object.
     * @param srcOffset  offset of the source object from where to start copying.
     * @param dest       destination object.
     * @param destOffset offset of the destination object from where to place the copied memory.
     * @param length     the length of memory to copy.
     */
    @Override
    public void copyMemory(@Nullable Object src, long srcOffset, @Nullable Object dest, long destOffset, int length) {
        assert SKIP_ASSERTIONS || !(src == null && dest == null);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);
        if (src instanceof byte[]) {
            if (dest instanceof byte[]) {
                copyMemory((byte[]) src, Math.toIntExact(srcOffset - Unsafe.ARRAY_BYTE_BASE_OFFSET), (byte[]) dest, Math.toIntExact(destOffset - ARRAY_BYTE_BASE_OFFSET), length);
            } else {
                copyMemoryLoop(src, ARRAY_BYTE_BASE_OFFSET + Math.toIntExact(srcOffset - Unsafe.ARRAY_BYTE_BASE_OFFSET), dest, destOffset, length);
            }
        } else {
            if (src == null) {
                if (dest == null) {
                    copyMemory(srcOffset, destOffset, (long) length);
                } else {
                    copyMemory(srcOffset, dest, destOffset, length);
                }
            } else {
                if (dest == null) {
                    copyMemory(src, srcOffset, destOffset, length);
                } else {
                    copyMemoryLoop(src, srcOffset, dest, destOffset, length);
                }
            }
        }
    }

    private void copyMemoryLoop(Object src, long srcOffset, Object dest, long destOffset, int length) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);
        if (src == dest && srcOffset < destOffset) {
            backwardCopyMemoryLoop(src, srcOffset, dest, destOffset, length);
            return;
        }
        int i = 0;
        for (; i < length - 7; i += 8)
            MEMORY.writeLong(dest, destOffset + i, UNSAFE.getLong(src, srcOffset + i));
        if (i < length - 3) {
            UNSAFE.putInt(dest, destOffset + i, UNSAFE.getInt(src, srcOffset + i));
            i += 4;
        }
        for (; i < length; i++)
            MEMORY.writeByte(dest, destOffset + i, UNSAFE.getByte(src, srcOffset + i));
    }

    private void backwardCopyMemoryLoop(Object src, long srcOffset, Object dest, long destOffset, int length) {
        assert SKIP_ASSERTIONS || nonNull(src);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcOffset);
        assert SKIP_ASSERTIONS || nonNull(dest);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);
        srcOffset += length;
        destOffset += length;
        int i = 0;
        for (; i < length - 7; i += 8)
            MEMORY.writeLong(dest, destOffset - 8 - i, UNSAFE.getLong(src, srcOffset - 8 - i));
        for (; i < length; i++) {
            MEMORY.writeByte(dest, destOffset - 1 - i, UNSAFE.getByte(src, srcOffset - 1 - i));
        }
    }

    /**
     * Copies memory from a given memory address to an object.
     *
     * @param srcAddress source memory address.
     * @param dest       destination object.
     * @param destOffset offset of the destination object from where to place the copied memory.
     * @param length     the length of memory to copy.
     */
    @Override
    public void copyMemory(long srcAddress, @Nullable Object dest, long destOffset, int length) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcAddress);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);
        long start = length > 128 << 10 ? System.nanoTime() : 0;
        copyMemoryLoop(null, srcAddress, dest, destOffset, length);
        if (length > 128 << 10) {
            long time = System.nanoTime() - start;
            if (time > 100_000)
                Jvm.perf().on(getClass(), "Took " + time / 1000 / 1e3 + " ms to copy " + length / 1024 + " KB");
        }
    }

    /**
     * Copies memory from one object to another object or memory address.
     * This is the fundamental method used by all copy operations in the class.
     *
     * @param src        source object.
     * @param srcOffset  offset of the source object from where to start copying.
     * @param dest       destination object.
     * @param destOffset offset of the destination object from where to place the copied memory.
     * @param length     the length of memory to copy.
     */
    void copyMemory0(@Nullable Object src, long srcOffset, @Nullable Object dest, long destOffset, long length) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), srcOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), destOffset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), length);
        // use a loop to ensure there is a safe point every so often.
        while (length > 0) {
            long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
            UNSAFE.copyMemory(src, srcOffset, dest, destOffset, size);
            length -= size;
            srcOffset += size;
            destOffset += size;
        }
    }

    /**
     * Calculates the stop bit length of an integer value.
     * The stop bit length is defined as the number of 7-bit groups needed to represent the given integer.
     * The calculation depends on the value of the integer. If it's negative, it will be {@code 1 + stopBitLength(~i)}.
     *
     * @param i the integer value to calculate its stop bit length.
     * @return the stop bit length of the provided integer.
     */
    @Override
    public int stopBitLength(int i) {
        // common case
        if ((i & ~0x7f) == 0)
            return 1;
        return stopBitLength0(i);
    }

    /**
     * Helper method to calculate the stop bit length of an integer value.
     * This method is used for all values that can't be resolved in a single step.
     *
     * @param i the integer value to calculate its stop bit length.
     * @return the stop bit length of the provided integer.
     */
    private int stopBitLength0(int i) {
        if (i < 0)
            return 1 + stopBitLength(~i);
        return (32 + 6 - Integer.numberOfLeadingZeros(i)) / 7;
    }

    /**
     * Calculates the stop bit length of a long integer value.
     * The stop bit length is defined as the number of 7-bit groups needed to represent the given long integer.
     * The calculation depends on the value of the long integer. If it's negative, {@code 1 + stopBitLength(~i)}.
     *
     * @param l the long integer value to calculate its stop bit length.
     * @return the stop bit length of the provided long integer.
     */
    @Override
    public int stopBitLength(long l) {
        // common case
        if ((l & ~0x7fL) == 0)
            return 1;
        return stopBitLength0(l);
    }

    /**
     * Helper method to calculate the stop bit length of a long integer value.
     * This method is used for all values that can't be resolved in a single step.
     *
     * @param l the long integer value to calculate its stop bit length.
     * @return the stop bit length of the provided long integer.
     */
    private int stopBitLength0(long l) {
        if (l < 0)
            return 1 + stopBitLength(~l);
        return (64 + 6 - Long.numberOfLeadingZeros(l)) / 7;
    }

    /**
     * Reads a portion of the given byte array starting from a given offset for a specified length.
     * Length determines the number of bytes to be read and also the interpretation of the read value (byte, short, int, or long).
     * If length is not a standard size (1, 2, 4, or 8), a combination of reads will be performed.
     *
     * @param bytes  the byte array to read from.
     * @param offset the offset from which to start reading.
     * @param length the number of bytes to read.
     * @return the value read as a long.
     */
    @Override
    public long partialRead(byte[] bytes, int offset, int length) {
        assert SKIP_ASSERTIONS || nonNull(bytes);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.betweenZeroAndReserving(), offset, bytes.length, length);
        switch (length) {
            case 8:
                return UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
            case 4:
                return UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) & 0xFFFF_FFFFL;
            case 2:
                return UNSAFE.getShort(bytes, ARRAY_BYTE_BASE_OFFSET + offset) & 0xFFFF;
            case 1:
                return bytes[offset] & 0xFF;
            case 0:
                return 0;
            default:
                // Do nothing here, instead continue below
        }
        long value = 0;
        offset += length;
        if ((length & 4) != 0) {
            offset -= 4;
            value = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset) & 0xFFFF_FFFFL;
        }
        if ((length & 2) != 0) {
            value <<= 16;
            offset -= 2;
            int s = UNSAFE.getShort(bytes, ARRAY_BYTE_BASE_OFFSET + offset) & 0xFFFF;
            value |= s;
        }
        if ((length & 1) != 0) {
            offset -= 1;
            value <<= 8;
            int b = bytes[offset] & 0xFF;
            value |= b;
        }
        return value;
    }

    /**
     * Reads a portion of memory from a given address for a specified length.
     * Length determines the number of bytes to be read and also the interpretation of the read value (byte, short, int, or long).
     * If length is not a standard size (1, 2, 4, or 8), a combination of reads will be performed.
     *
     * @param addr   the address from which to read.
     * @param length the number of bytes to read.
     * @return the value read as a long.
     */
    @Override
    public long partialRead(long addr, int length) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), addr);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        switch (length) {
            case 8:
                return UNSAFE.getLong(addr);
            case 4:
                return UNSAFE.getInt(addr) & 0xFFFF_FFFFL;
            case 2:
                return UNSAFE.getShort(addr) & 0xFFFF;
            case 1:
                return UNSAFE.getByte(addr) & 0xFF;
            case 0:
                return 0;
            default:
                // Do nothing here, instead continue below
        }
        long value = 0;
        addr += length;
        if ((length & 4) != 0) {
            addr -= 4;
            value = UNSAFE.getInt(addr) & 0xFFFF_FFFFL;
        }
        if ((length & 2) != 0) {
            value <<= 16;
            addr -= 2;
            int s = UNSAFE.getShort(addr) & 0xFFFF;
            value |= s;
        }
        if ((length & 1) != 0) {
            value <<= 8;
            addr--;
            int b = UNSAFE.getByte(addr) & 0xFF;
            value |= b;
        }
        return value;
    }

    /**
     * Writes a long value to a portion of the given byte array starting from a given offset for a specified length.
     * Length determines the number of bytes to be written and also the interpretation of the value to be written (byte, short, int, or long).
     * If length is not a standard size (1, 2, 4, or 8), a combination of writes will be performed.
     *
     * @param bytes  the byte array to write to.
     * @param offset the offset from which to start writing.
     * @param value  the value to be written.
     * @param length the number of bytes to write.
     */
    @Override
    public void partialWrite(byte[] bytes, int offset, long value, int length) {
        assert SKIP_ASSERTIONS || nonNull(bytes);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), offset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), value);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        switch (length) {
            case 8:
                UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset, value);
                return;
            case 4:
                UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset, (int) value);
                return;
            case 2:
                UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + offset, (short) value);
                return;
            case 1:
                UNSAFE.putByte(bytes, ARRAY_BYTE_BASE_OFFSET + offset, (byte) value);
                return;
            case 0:
                return;
            default:
                // Do nothing here, instead continue below
        }
        if ((length & 1) != 0) {
            UNSAFE.putByte(bytes, ARRAY_BYTE_BASE_OFFSET + offset, (byte) value);
            offset += 1;
            value >>>= 8;
        }
        if ((length & 2) != 0) {
            UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + offset, (short) value);
            offset += 2;
            value >>>= 16;
        }
        if ((length & 4) != 0) {
            UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset, (int) value);
        }
    }

    /**
     * Writes a long value to a portion of memory at a given address for a specified length.
     * Length determines the number of bytes to be written and also the interpretation of the value to be written (byte, short, int, or long).
     * If length is not a standard size (1, 2, 4, or 8), a combination of writes will be performed.
     *
     * @param addr   the address to write to.
     * @param value  the value to be written.
     * @param length the number of bytes to write.
     */
    @Override
    public void partialWrite(long addr, long value, int length) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), addr);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), value);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        switch (length) {
            case 8:
                UNSAFE.putLong(addr, value);
                return;
            case 4:
                UNSAFE.putInt(addr, (int) value);
                return;
            case 2:
                UNSAFE.putShort(addr, (short) value);
                return;
            case 1:
                UNSAFE.putByte(addr, (byte) value);
                return;
            case 0:
                return;
            default:
                // Do nothing here, instead continue below
        }
        if ((length & 1) != 0) {
            UNSAFE.putByte(addr, (byte) value);
            addr += 1;
            value >>>= 8;
        }
        if ((length & 2) != 0) {
            UNSAFE.putShort(addr, (short) value);
            addr += 2;
            value >>>= 16;
        }
        if ((length & 4) != 0) {
            UNSAFE.putInt(addr, (int) value);
        }
    }

    /**
     * Checks if a range of bytes in the provided byte array contains only 7-bit ASCII characters.
     *
     * @param bytes  the byte array to check.
     * @param offset the starting index of the range to check.
     * @param length the number of bytes to check.
     * @return true if all bytes in the range are 7-bit ASCII characters, false otherwise.
     */
    @Override
    public boolean is7Bit(byte[] bytes, int offset, int length) {
        assert SKIP_ASSERTIONS || nonNull(bytes);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), offset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        final long offset2 = offset + ARRAY_BYTE_BASE_OFFSET;
        int i = 0;
        for (; i < length - 7; i += 8)
            if ((UnsafeMemory.UNSAFE.getLong(bytes, offset2 + i) & 0x8080808080808080L) != 0)
                return false;

        if (i < length - 3) {
            if ((UnsafeMemory.UNSAFE.getInt(bytes, offset2 + i) & 0x80808080) != 0)
                return false;
            i += 4;
        }
        if (i < length - 1) {
            if ((UnsafeMemory.UNSAFE.getShort(bytes, offset2 + i) & 0x8080) != 0)
                return false;
            i += 2;
        }

        if (i < length)
            return UnsafeMemory.UNSAFE.getByte(bytes, offset2 + i) >= 0;
        return true;
    }

    /**
     * Checks if a range of characters in the provided character array contains only 7-bit ASCII characters.
     *
     * @param chars  the character array to check.
     * @param offset the starting index of the range to check.
     * @param length the number of characters to check.
     * @return true if all characters in the range are 7-bit ASCII characters, false otherwise.
     */
    @Override
    public boolean is7Bit(char[] chars, int offset, int length) {
        assert SKIP_ASSERTIONS || nonNull(chars);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), offset);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        final long offset2 = offset * 2L + ARRAY_CHAR_BASE_OFFSET;
        int i = 0;
        for (; i < length - 3; i += 4)
            if ((UnsafeMemory.UNSAFE.getLong(chars, offset2 + i + i) & 0xFF80FF80FF80FF80L) != 0)
                return false;
        if (i < length - 1) {
            if ((UnsafeMemory.UNSAFE.getInt(chars, offset2 + i + i) & 0xFF80FF80) != 0)
                return false;
            i += 2;
        }
        if (i < length)
            return (UnsafeMemory.UNSAFE.getChar(chars, offset2 + i + i) & 0xFF80) == 0;
        return true;
    }

    /**
     * Checks if a range of bytes in the memory starting from the provided address contains only 7-bit ASCII characters.
     *
     * @param address the starting address of the range to check.
     * @param length  the number of bytes to check.
     * @return true if all bytes in the range are 7-bit ASCII characters, false otherwise.
     */
    @Override
    public boolean is7Bit(long address, int length) {
        assert SKIP_ASSERTIONS || address != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        int i = 0;
        for (; i < length - 7; i += 8)
            if ((UnsafeMemory.UNSAFE.getLong(address + i) & 0x8080808080808080L) != 0)
                return false;
        if (i < length - 3) {
            if ((UnsafeMemory.UNSAFE.getInt(address + i) & 0x80808080) != 0)
                return false;
            i += 4;
        }
        if (i < length - 1) {
            if ((UnsafeMemory.UNSAFE.getShort(address + i) & 0x8080) != 0)
                return false;
            i += 2;
        }
        if (i < length)
            return UnsafeMemory.UNSAFE.getByte(address + i) >= 0;
        return true;
    }

    /**
     * Writes a long value to the memory location specified by the address, in a way that is
     * guaranteed to be ordered with respect to other memory operations.
     *
     * @param address the memory address to write to.
     * @param i       the long value to write.
     */
    @Override
    public void writeOrderedLong(long address, long i) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putOrderedLong(null, address, i);
    }

    /**
     * Writes a long value to the field of the specified object at the given offset, in a way that is
     * guaranteed to be ordered with respect to other memory operations.
     *
     * @param object the object whose field to write to.
     * @param offset the offset of the field.
     * @param i      the long value to write.
     */
    @Override
    public void writeOrderedLong(Object object, long offset, long i) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putOrderedLong(object, offset, i);
    }

    /**
     * Performs an atomic compare-and-set operation on an integer value at the specified memory address.
     * If the current value equals the expected value, it is set to the new value.
     *
     * @param address  the memory address.
     * @param offset   the offset of the integer value.
     * @param expected the expected integer value.
     * @param value    the new integer value.
     * @throws IllegalStateException if the current value does not equal the expected value.
     */
    @Override
    public void testAndSetInt(long address, long offset, int expected, int value) throws IllegalStateException {
        assert (address & 63) <= 64 - 4;
        assert SKIP_ASSERTIONS || address != 0;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        if (UNSAFE.compareAndSwapInt(null, address, expected, value))
            return;
        int actual = UNSAFE.getIntVolatile(null, address);
        throw new IllegalStateException(CANNOT_CHANGE_AT + offset + EXPECTED + expected + WAS + actual);
    }

    /**
     * Performs an atomic compare-and-set operation on an integer field of the specified object.
     * If the current value equals the expected value, it is set to the new value.
     *
     * @param object   the object whose field to operate on.
     * @param offset   the offset of the field.
     * @param expected the expected integer value.
     * @param value    the new integer value.
     * @throws IllegalStateException if the current value does not equal the expected value.
     */
    @Override
    public void testAndSetInt(Object object, long offset, int expected, int value) throws IllegalStateException {
        assert SKIP_ASSERTIONS || nonNull(object);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        if (UNSAFE.compareAndSwapInt(object, offset, expected, value))
            return;
        int actual = UNSAFE.getIntVolatile(object, offset);
        throw new IllegalStateException("Cannot change " + object.getClass().getSimpleName() + " at " + offset + EXPECTED + expected + WAS + actual);
    }

    /**
     * Performs an atomic compare-and-swap operation on an integer value at the specified memory address.
     * If the current value equals the expected value, it is set to the new value and true is returned.
     *
     * @param address  the memory address.
     * @param expected the expected integer value.
     * @param value    the new integer value.
     * @return true if the operation was successful, false otherwise.
     * @throws MisAlignedAssertionError if the address is not correctly aligned.
     */
    @Override
    public boolean compareAndSwapInt(long address, int expected, int value) throws MisAlignedAssertionError {
        assert (address & 63) <= 64 - 4;
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.compareAndSwapInt(null, address, expected, value);
    }

    /**
     * Performs an atomic compare-and-swap operation on an integer field of the specified object.
     * If the current value equals the expected value, it is set to the new value and true is returned.
     *
     * @param object   the object whose field to operate on.
     * @param offset   the offset of the field.
     * @param expected the expected integer value.
     * @param value    the new integer value.
     * @return true if the operation was successful, false otherwise.
     * @throws MisAlignedAssertionError if the offset is not correctly aligned.
     */
    @Override
    public boolean compareAndSwapInt(Object object, long offset, int expected, int value) throws MisAlignedAssertionError {
        assert (offset & 63) <= 64 - 4;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.compareAndSwapInt(object, offset, expected, value);
    }

    /**
     * Performs an atomic compare-and-swap operation on a long value at the specified memory address.
     * If the current value equals the expected value, it is set to the new value and true is returned.
     *
     * @param address  the memory address.
     * @param expected the expected long value.
     * @param value    the new long value.
     * @return true if the operation was successful, false otherwise.
     * @throws MisAlignedAssertionError if the address is not correctly aligned.
     */
    @Override
    public boolean compareAndSwapLong(long address, long expected, long value) throws MisAlignedAssertionError {
        if (!safeAlignedLong(address))
            throw new MisAlignedAssertionError();
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.compareAndSwapLong(null, address, expected, value);
    }

    /**
     * Performs an atomic compare-and-swap operation on a long field of the specified object.
     * If the current value equals the expected value, it is set to the new value and true is returned.
     *
     * @param object   the object whose field to operate on.
     * @param offset   the offset of the field.
     * @param expected the expected long value.
     * @param value    the new long value.
     * @return true if the operation was successful, false otherwise.
     * @throws MisAlignedAssertionError if the offset is not correctly aligned.
     */
    @Override
    public boolean compareAndSwapLong(Object object, long offset, long expected, long value) throws MisAlignedAssertionError {
        assert SKIP_ASSERTIONS || (object == null || assertIfEnabled(Longs.nonNegative(), offset));
        return UNSAFE.compareAndSwapLong(object, offset, expected, value);
    }

    /**
     * Performs an atomic get-and-set operation on an integer value at the specified memory address.
     * The method sets the field to the given value and returns the old value.
     *
     * @param address the memory address.
     * @param value   the new integer value.
     * @return the old integer value.
     * @throws MisAlignedAssertionError if the address is not correctly aligned.
     */
    @Override
    public int getAndSetInt(long address, int value) throws MisAlignedAssertionError {
        assert (address & 63) <= 64 - 4;
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getAndSetInt(null, address, value);
    }

    /**
     * Performs an atomic get-and-set operation on an integer field of the specified object.
     * The method sets the field to the given value and returns the old value.
     *
     * @param object the object whose field to operate on.
     * @param offset the offset of the field.
     * @param value  the new integer value.
     * @return the old integer value.
     * @throws MisAlignedAssertionError if the offset is not correctly aligned.
     */
    @Override
    public int getAndSetInt(Object object, long offset, int value) throws MisAlignedAssertionError {
        assert (offset & 63) <= 64 - 4;
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getAndSetInt(object, offset, value);
    }

    /**
     * Returns the memory page size.
     *
     * @return the memory page size.
     */
    @Override
    public int pageSize() {
        return UNSAFE.pageSize();
    }

    /**
     * Reads a byte value in a volatile manner from the specified memory address.
     *
     * @param address the memory address.
     * @return the byte value read from the address.
     */
    @Override
    public byte readVolatileByte(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getByteVolatile(null, address);
    }

    /**
     * Reads a byte value in a volatile manner from the specified object at the given offset.
     *
     * @param object the object from which to read.
     * @param offset the offset from which to read.
     * @return the byte value read from the object at the offset.
     */
    @Override
    public byte readVolatileByte(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getByteVolatile(object, offset);
    }

    /**
     * Reads a short value in a volatile manner from the specified memory address.
     *
     * @param address the memory address.
     * @return the short value read from the address.
     * @implNote This method currently does not support a short split across cache lines.
     */
    @Override
    public short readVolatileShort(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        // TODO add support for a short split across cache lines.
        return UNSAFE.getShortVolatile(null, address);
    }

    /**
     * Reads a short value in a volatile manner from the specified object at the given offset.
     *
     * @param object the object from which to read.
     * @param offset the offset from which to read.
     * @return the short value read from the object at the offset.
     */
    @Override
    public short readVolatileShort(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getShortVolatile(object, offset);
    }

    /**
     * Reads an integer value in a volatile manner from the specified memory address.
     * If the address is correctly aligned, this method attempts to read the integer value twice
     * to ensure that it has been loaded correctly from main memory.
     *
     * @param address the memory address.
     * @return the integer value read from the address.
     */
    @Override
    public int readVolatileInt(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        int value = UNSAFE.getIntVolatile(null, address);
        if ((address & 63) <= 60) {
            if (value == 0)
                value = UNSAFE.getIntVolatile(null, address);
            return value;
        }
        return retryReadVolatileInt(address, value);
    }

    /**
     * Reads an integer value in a volatile manner from the specified object at the given offset.
     *
     * @param object the object from which to read.
     * @param offset the offset from which to read.
     * @return the integer value read from the object at the offset.
     */
    @Override
    public int readVolatileInt(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getIntVolatile(object, offset);
    }

    /**
     * Reads a float value in a volatile manner from the specified memory address.
     *
     * @param address the memory address.
     * @return the float value read from the address.
     * @implNote This method currently does not support a float split across cache lines.
     */
    @Override
    public float readVolatileFloat(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        // TODO add support for a float split across cache lines.
        return UNSAFE.getFloatVolatile(null, address);
    }

    /**
     * Reads a float value in a volatile manner from the specified object at the given offset.
     *
     * @param object the object from which to read.
     * @param offset the offset from which to read.
     * @return the float value read from the object at the offset.
     */
    @Override
    public float readVolatileFloat(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getFloatVolatile(object, offset);
    }

    /**
     * Reads a long value in a volatile manner from the specified memory address.
     * If the address is correctly aligned, this method attempts to read the long value twice
     * to ensure that it has been loaded correctly from main memory.
     *
     * @param address the memory address.
     * @return the long value read from the address.
     */
    @Override
    public long readVolatileLong(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        long value = UNSAFE.getLongVolatile(null, address);
        if ((address & 63) <= 64 - 8) {
            return value;
        }
        return retryReadVolatileLong(address, value);
    }

    /**
     * Reads a long value in a volatile manner from the specified object at the given offset.
     *
     * @param object the object from which to read.
     * @param offset the offset from which to read.
     * @return the long value read from the object at the offset.
     */
    @Override
    public long readVolatileLong(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getLongVolatile(object, offset);
    }

    /**
     * Reads a double value in a volatile manner from the specified memory address.
     *
     * @param address the memory address.
     * @return the double value read from the address.
     * @implNote This method currently does not support a double split across cache lines.
     */
    @Override
    public double readVolatileDouble(long address) {
        assert SKIP_ASSERTIONS || address != 0;
        // TODO add support for a double split across cache lines.
        return UNSAFE.getDoubleVolatile(null, address);
    }

    /**
     * Reads a double value in a volatile manner from the specified object at the given offset.
     *
     * @param object the object from which to read.
     * @param offset the offset from which to read.
     * @return the double value read from the object at the offset.
     */
    @Override
    public double readVolatileDouble(Object object, long offset) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getDoubleVolatile(object, offset);
    }

    /**
     * Writes a byte value in a volatile manner to the specified memory address.
     *
     * @param address the memory address.
     * @param b       the byte value to write.
     */
    @Override
    public void writeVolatileByte(long address, byte b) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putByteVolatile(null, address, b);
    }

    /**
     * Writes a byte value in a volatile manner to the specified object at the given offset.
     *
     * @param object the object to which to write.
     * @param offset the offset at which to write.
     * @param b      the byte value to write.
     */
    @Override
    public void writeVolatileByte(Object object, long offset, byte b) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putByteVolatile(object, offset, b);
    }

    /**
     * Writes a short value in a volatile manner to the specified memory address.
     *
     * @param address the memory address.
     * @param i16     the short value to write.
     */
    @Override
    public void writeVolatileShort(long address, short i16) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putShortVolatile(null, address, i16);
    }

    /**
     * Writes a short value in a volatile manner to the specified object at the given offset.
     *
     * @param object the object to which to write.
     * @param offset the offset at which to write.
     * @param i16    the short value to write.
     */
    @Override
    public void writeVolatileShort(Object object, long offset, short i16) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putShortVolatile(object, offset, i16);
    }

    /**
     * Writes an integer value in a volatile manner to the specified memory address.
     *
     * @param address the memory address.
     * @param i32     the integer value to write.
     */
    @Override
    public void writeVolatileInt(long address, int i32) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putIntVolatile(null, address, i32);
    }

    /**
     * Writes an integer value in a volatile manner to the specified object at the given offset.
     *
     * @param object the object to which to write.
     * @param offset the offset at which to write.
     * @param i32    the integer value to write.
     */
    @Override
    public void writeVolatileInt(Object object, long offset, int i32) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putIntVolatile(object, offset, i32);
    }

    /**
     * Writes a float value in a volatile manner to the specified memory address.
     *
     * @param address the memory address.
     * @param f       the float value to write.
     */
    @Override
    public void writeVolatileFloat(long address, float f) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putFloatVolatile(null, address, f);
    }

    /**
     * Writes a float value in a volatile manner to the specified object at the given offset.
     *
     * @param object the object to which to write.
     * @param offset the offset at which to write.
     * @param f      the float value to write.
     */
    @Override
    public void writeVolatileFloat(Object object, long offset, float f) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putFloatVolatile(object, offset, f);
    }

    /**
     * Writes a long value in a volatile manner to the specified memory address.
     *
     * @param address the memory address.
     * @param i64     the long value to write.
     */
    @Override
    public void writeVolatileLong(long address, long i64) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putLongVolatile(null, address, i64);
    }

    /**
     * Writes a long value in a volatile manner to the specified object at the given offset.
     *
     * @param object the object to which to write.
     * @param offset the offset at which to write.
     * @param i64    the long value to write.
     */
    @Override
    public void writeVolatileLong(Object object, long offset, long i64) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putLongVolatile(object, offset, i64);
    }

    /**
     * Writes a double value in a volatile manner to the specified memory address.
     *
     * @param address the memory address.
     * @param d       the double value to write.
     */
    @Override
    public void writeVolatileDouble(long address, double d) {
        assert SKIP_ASSERTIONS || address != 0;
        UNSAFE.putDoubleVolatile(null, address, d);
    }

    /**
     * Writes a double value in a volatile manner to the specified object at the given offset.
     *
     * @param object the object to which to write.
     * @param offset the offset at which to write.
     * @param d      the double value to write.
     */
    @Override
    public void writeVolatileDouble(Object object, long offset, double d) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        UNSAFE.putDoubleVolatile(object, offset, d);
    }

    /**
     * Atomically adds the given increment to an integer value at the specified memory address.
     *
     * @param address   the memory address.
     * @param increment the value to add.
     * @return the updated value.
     * @throws MisAlignedAssertionError if address is not properly aligned.
     */
    @Override
    public int addInt(long address, int increment) throws MisAlignedAssertionError {
        assert SKIP_ASSERTIONS || address != 0;
        return UNSAFE.getAndAddInt(null, address, increment) + increment;
    }

    /**
     * Atomically adds the given increment to an integer value at the specified offset in the given object.
     *
     * @param object    the object to which to add the value.
     * @param offset    the offset at which to add the value.
     * @param increment the value to add.
     * @return the updated value.
     */
    @Override
    public int addInt(Object object, long offset, int increment) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
        return UNSAFE.getAndAddInt(object, offset, increment) + increment;
    }

    /**
     * Atomically adds the given increment to a long value at the specified memory address.
     *
     * @param address   the memory address.
     * @param increment the value to add.
     * @return the updated value.
     * @throws MisAlignedAssertionError if address is not properly aligned.
     */
    @Override
    public long addLong(long address, long increment) throws MisAlignedAssertionError {
        assert SKIP_ASSERTIONS || address != 0;
//        assert (address & 0x7) == 0;
        return UNSAFE.getAndAddLong(null, address, increment) + increment;
    }

    /**
     * Atomically adds the given increment to a long value at the specified offset in the given object.
     *
     * @param object    the object to which to add the value.
     * @param offset    the offset at which to add the value.
     * @param increment the value to add.
     * @return the updated value.
     * @throws MisAlignedAssertionError if offset is not properly aligned.
     */
    @Override
    public long addLong(Object object, long offset, long increment) throws MisAlignedAssertionError {
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
//        assert (offset & 0x7) == 0;
        return UNSAFE.getAndAddLong(object, offset, increment) + increment;
    }

    /**
     * Copies an 8-bit representation of the specified substring of the given string into memory at the specified address.
     *
     * @param s      the source string.
     * @param start  the beginning index, inclusive.
     * @param length the number of characters to be copied.
     * @param addr   the target memory address.
     */
    public void copy8bit(String s, int start, int length, long addr) {
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), start);
        assert SKIP_ASSERTIONS || assertIfEnabled(Ints.nonNegative(), length);
        assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), addr);
        if (CachedReflection.STRING_VALUE_OFFSET == 0) {
            copy8BitJava9(s, start, length, addr);
            return;
        }
        char[] chars = (char[]) UNSAFE.getObject(s, CachedReflection.STRING_VALUE_OFFSET);
        for (int i = 0; i < length; i++)
            UNSAFE.putByte(addr + i, (byte) chars[start + i]);
    }

    /**
     * Private helper method to copy an 8-bit representation of the specified substring of the given string into
     * memory at the specified address for Java 9 and later versions.
     *
     * @param s      the source string.
     * @param start  the beginning index, inclusive.
     * @param length the number of characters to be copied.
     * @param addr   the target memory address.
     */
    private void copy8BitJava9(String s, int start, int length, long addr) {
        for (int i = 0; i < length; i++)
            UNSAFE.putByte(addr + i, (byte) s.charAt(start + i));
    }

    /**
     * Writes an 8-bit representation of the specified substring of the given string to the specified offset in the given object.
     *
     * @param s      the source string.
     * @param start  the beginning index, inclusive.
     * @param object the target object.
     * @param offset the offset within the object.
     * @param length the number of characters to be copied.
     */
    public void write8bit(String s, int start, Object object, long offset, int length) {
        if (CachedReflection.STRING_VALUE_OFFSET == 0) {
            write8bitJava9(s, start, object, offset, length);
            return;
        }
        char[] chars = (char[]) UNSAFE.getObject(s, CachedReflection.STRING_VALUE_OFFSET);
        for (int i = 0; i < length; i++)
            UNSAFE.putByte(object, offset + i, (byte) chars[start + i]);
    }

    /**
     * Private helper method to write an 8-bit representation of the specified substring of the given string to
     * the specified offset in the given object for Java 9 and later versions.
     *
     * @param s      the source string.
     * @param start  the beginning index, inclusive.
     * @param object the target object.
     * @param offset the offset within the object.
     * @param length the number of characters to be copied.
     */
    private void write8bitJava9(String s, int start, Object object, long offset, int length) {
        for (int i = 0; i < length; i++)
            UNSAFE.putByte(object, offset + i, (byte) s.charAt(start + i));
    }

    /**
     * Compares the substring of the given string with the string represented in the memory at the specified address.
     *
     * @param addr   the memory address of the source string.
     * @param s      the target string.
     * @param length the number of characters to be compared.
     * @return true if strings are equal, false otherwise.
     */
    public boolean isEqual(long addr, String s, int length) {
        if (CachedReflection.STRING_VALUE_OFFSET == 0) {
            return isEqualJava9(addr, s, length);
        }
        char[] chars = (char[]) UNSAFE.getObject(s, CachedReflection.STRING_VALUE_OFFSET);
        for (int i = 0; i < length; i++)
            if (UNSAFE.getByte(addr + i) != chars[i])
                return false;
        return true;
    }

    /**
     * Private helper method to compare the substring of the given string with the string represented in
     * the memory at the specified address for Java 9 and later versions.
     *
     * @param addr   the memory address of the source string.
     * @param s      the target string.
     * @param length the number of characters to be compared.
     * @return true if strings are equal, false otherwise.
     */
    private boolean isEqualJava9(long addr, String s, int length) {
        for (int i = 0; i < length; i++)
            if (UNSAFE.getByte(addr + i) != s.charAt(i))
                return false;
        return true;
    }

    /**
     * Checks if the given address is safely aligned for an int operation.
     *
     * @param addr the address to check.
     * @return true if address is safely aligned, false otherwise.
     */
    @Override
    public boolean safeAlignedInt(long addr) {
        // This will return true for all address values except 4 preceding steps before a cache-line.
        return (addr & 63) <= 60;
    }

    /**
     * Checks if the given address is safely aligned for a long operation.
     *
     * @param addr the address to check.
     * @return true if address is safely aligned, false otherwise.
     */
    @Override
    public boolean safeAlignedLong(long addr) {
        // This will return true for all address values except 8 preceding steps before a cache-line.
        return (addr & 63) <= 56;
    }

    /**
     * Returns the array base offset for the given type.
     *
     * @param type the class representing the array type.
     * @return the array base offset for the given type.
     */
    @Override
    public int arrayBaseOffset(Class<?> type) {
        return UNSAFE.arrayBaseOffset(type);
    }

    /**
     * Returns the offset of the field represented by the given Field object.
     *
     * @param field the field object.
     * @return the offset of the field.
     */
    @Override
    public long objectFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    /**
     * Returns the address of the ByteBuffer's backing data.
     *
     * @param bb the ByteBuffer.
     * @return the address of the ByteBuffer's backing data.
     */
    @Override
    public long address(ByteBuffer bb) {
        return DirectBufferUtil.addressOrThrow(bb);
    }

    private interface ObjectToAddress {
        void apply(Object src, long srcOffset, long dest, int length);
    }

    // https://github.com/OpenHFT/OpenHFT/issues/23

    static final class CachedReflection {

        private static final long STRING_VALUE_OFFSET;

        static {
            long offset = 0;
            try {
                if (!Jvm.isJava9Plus()) {
                    final Field valueField = String.class.getDeclaredField("value");
                    offset = UNSAFE.objectFieldOffset(valueField);
                }
            } catch (NoSuchFieldException e) {
                offset = 0;
            }
            STRING_VALUE_OFFSET = offset;
        }

        // Suppresses default constructor, ensuring non-instantiability.
        private CachedReflection() {
        }
    }

    /**
     * The ARMMemory class extends the UnsafeMemory class to provide memory operations specifically tailored
     * to ARM architecture. This class addresses ARM's alignment restrictions and encapsulates the necessary
     * precautions for handling volatile reads and writes of short values. It ensures that all memory operations
     * are performed in a safe, controlled manner.
     * <p>
     * Due to the alignment requirements on ARM, any memory address or offset that is not aligned to 2 bytes
     * (the size of a short) will result in explicit memory barriers enforced using the loadFence or storeFence
     * methods of the Unsafe class.
     * <p>
     * Care must be taken while using this class due to the low-level memory manipulation it provides. Misuse
     * of these operations can lead to unpredictable results or system crashes. Therefore, it is recommended
     * to be used only when absolutely necessary and always with the understanding of the potential risks involved.
     *
     * @see UnsafeMemory for the base implementation of memory operations.
     */
    static class ARMMemory extends UnsafeMemory {
        /**
         * Reads a volatile short from the provided address.
         * If the address is not aligned to 2 bytes, a memory fence is enforced before reading.
         *
         * @param address the address to read from
         * @return the short read from the address
         */
        @Override
        public short readVolatileShort(long address) {
            assert SKIP_ASSERTIONS || address != 0;
            if ((address & 0x1) == 0)
                return super.readVolatileShort(address);
            UNSAFE.loadFence();
            return super.readShort(address);
        }

        /**
         * Writes a volatile short to the provided address.
         * If the address is not aligned to 2 bytes, the short is written and then a memory fence is enforced.
         *
         * @param address the address to write to
         * @param i16     the short to write
         */
        @Override
        public void writeVolatileShort(long address, short i16) {
            assert SKIP_ASSERTIONS || address != 0;
            if ((address & 0x1) == 0) {
                super.writeVolatileShort(address, i16);
            } else {
                super.writeShort(address, i16);
                UNSAFE.storeFence();
            }
        }

        /**
         * Reads a volatile short from the object at the provided offset.
         * If the offset is not aligned to 2 bytes, a memory fence is enforced before reading.
         *
         * @param object the object to read from
         * @param offset the offset to read from
         * @return the short read from the object at the offset
         */
        @Override
        public short readVolatileShort(Object object, long offset) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if ((offset & 0x1) == 0)
                return super.readVolatileShort(object, offset);
            UNSAFE.loadFence();
            return super.readShort(object, offset);
        }

        /**
         * Writes a volatile short to the object at the provided offset.
         * If the offset is not aligned to 2 bytes, the short is written and then a memory fence is enforced.
         *
         * @param object the object to write to
         * @param offset the offset to write to
         * @param i16    the short to write
         */
        @Override
        public void writeVolatileShort(Object object, long offset, short i16) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if ((offset & 0x1) == 0) {
                super.writeVolatileShort(object, offset, i16);
            } else {
                super.writeShort(object, offset, i16);
                UNSAFE.storeFence();
            }
        }

        /**
         * Writes a float to the specified address.
         * If the address is not aligned to 4 bytes (which is the size of an int),
         * the float is first converted to raw int bits before writing.
         *
         * @param address the memory address to write to
         * @param f       the float value to write
         */
        @Override
        public void writeFloat(long address, float f) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                super.writeFloat(address, f);
            else
                super.writeInt(address, Float.floatToRawIntBits(f));
        }

        /**
         * Reads a float from the specified address.
         * If the address is not aligned to 4 bytes (which is the size of an int),
         * the method reads an int and converts it to a float.
         *
         * @param address the memory address to read from
         * @return the float value read from the address
         */
        @Override
        public float readFloat(long address) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                return super.readFloat(address);
            return Float.intBitsToFloat(super.readInt(address));
        }

        /**
         * Writes a float to the object at the specified offset.
         * If the offset is not aligned to 4 bytes (which is the size of an int),
         * the float is first converted to raw int bits before writing.
         *
         * @param object the object to write to
         * @param offset the offset to write to
         * @param f      the float value to write
         */
        @Override
        public void writeFloat(Object object, long offset, float f) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if ((offset & 0x3) == 0)
                super.writeFloat(object, offset, f);
            else
                super.writeInt(object, offset, Float.floatToRawIntBits(f));

        }

        /**
         * Reads a float from the object at the specified offset.
         * If the offset is not aligned to 4 bytes (which is the size of an int),
         * the method reads an int and converts it to a float.
         *
         * @param object the object to read from
         * @param offset the offset to read from
         * @return the float value read from the object at the offset
         */
        @Override
        public float readFloat(Object object, long offset) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedInt(offset))
                return super.readFloat(object, offset);
            return Float.intBitsToFloat(super.readInt(object, offset));
        }

        /**
         * Reads a volatile int from the specified address.
         * If the address is not aligned to 4 bytes (which is the size of an int),
         * a memory fence is used to ensure ordering of reads and writes.
         *
         * @param address the memory address to read from
         * @return the int value read from the address
         */
        @Override
        public int readVolatileInt(long address) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                return super.readVolatileInt(address);
            UNSAFE.loadFence();
            return super.readInt(address);
        }

        /**
         * Reads a volatile int from the object at the specified offset.
         * If the offset is not aligned to 4 bytes (which is the size of an int),
         * a memory fence is used to ensure ordering of reads and writes.
         *
         * @param object the object to read from
         * @param offset the offset to read from
         * @return the int value read from the object at the offset
         */
        @Override
        public int readVolatileInt(Object object, long offset) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedInt(offset))
                return super.readVolatileInt(object, offset);
            UNSAFE.loadFence();
            return super.readInt(object, offset);
        }

        /**
         * Reads a volatile float from the specified address.
         * If the address is not aligned to 4 bytes (which is the size of an int),
         * a memory fence is used to ensure ordering of reads and writes.
         *
         * @param address the memory address to read from
         * @return the float value read from the address
         */
        @Override
        public float readVolatileFloat(long address) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                return super.readVolatileFloat(address);
            UNSAFE.loadFence();
            return readFloat(address);
        }

        /**
         * Writes a volatile int to the specified address.
         * If the address is not aligned to 4 bytes (which is the size of an int),
         * the int is written and a store memory fence is used to ensure the ordering of reads and writes.
         *
         * @param address the memory address to write to
         * @param i32     the int value to write
         */
        @Override
        public void writeVolatileInt(long address, int i32) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address)) {
                super.writeVolatileInt(address, i32);
            } else {
                UNSAFE.putInt(address, i32);
                UNSAFE.storeFence();
            }
        }

        /**
         * Writes an int to the specified address with ordered/lazy semantics. If the address
         * is not aligned to 4 bytes (which is the size of an int), the int is written and a
         * store memory fence is used to ensure the ordering of reads and writes.
         *
         * @param address the memory address to write to
         * @param i32     the int value to write
         */
        @Override
        public void writeOrderedInt(long address, int i32) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                super.writeOrderedInt(address, i32);
            else {
                UNSAFE.putInt(address, i32);
                UNSAFE.storeFence();
            }
        }

        /**
         * Writes a volatile int to the object at the specified offset. If the offset
         * is not aligned to 4 bytes (which is the size of an int), the int is written and a
         * store memory fence is used to ensure the ordering of reads and writes.
         *
         * @param object the object to write to
         * @param offset the offset to write at
         * @param i32    the int value to write
         */
        @Override
        public void writeVolatileInt(Object object, long offset, int i32) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if ((offset & 0x3) == 0)
                super.writeVolatileInt(object, offset, i32);
            else {
                UNSAFE.putInt(object, offset, i32);
                UNSAFE.storeFence();
            }
        }

        /**
         * Writes an int to the object at the specified offset with ordered/lazy semantics.
         * If the offset is not aligned to 4 bytes (which is the size of an int), the int is
         * written and a store memory fence is used to ensure the ordering of reads and writes.
         *
         * @param object the object to write to
         * @param offset the offset to write at
         * @param i32    the int value to write
         */
        @Override
        public void writeOrderedInt(Object object, long offset, int i32) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedInt(offset)) {
                super.writeOrderedInt(object, offset, i32);
            } else {
                UNSAFE.putInt(object, offset, i32);
                UNSAFE.storeFence();
            }
        }

        /**
         * Writes a volatile float to the specified address. If the address is not aligned to
         * 4 bytes (which is the size of an int), the float is written as a volatile int to the address.
         *
         * @param address the memory address to write to
         * @param f       the float value to write
         */
        @Override
        public void writeVolatileFloat(long address, float f) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                super.writeVolatileFloat(address, f);
            else
                writeVolatileInt(address, Float.floatToRawIntBits(f));
        }

        /**
         * Atomically adds the given increment to an int value at the specified memory address.
         * If the address is not aligned to 4 bytes (which is the size of an int), a MisAlignedAssertionError is thrown.
         *
         * @param address   the memory address of the int value
         * @param increment the value to add
         * @return the updated value
         * @throws MisAlignedAssertionError if the address is not aligned to 4 bytes
         */
        @Override
        public int addInt(long address, int increment) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                return super.addInt(address, increment);
            throw new MisAlignedAssertionError();
        }

        /**
         * Atomically sets the int at the specified memory address to the given value if it currently
         * holds the expected value. If the address is not aligned to 4 bytes (which is the size of an int),
         * a MisAlignedAssertionError is thrown.
         *
         * @param address  the memory address of the int value
         * @param expected the expected current value
         * @param value    the new value
         * @return true if successful, false otherwise
         * @throws MisAlignedAssertionError if the address is not aligned to 4 bytes
         */
        @Override
        public boolean compareAndSwapInt(long address, int expected, int value) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                return super.compareAndSwapInt(address, expected, value);
            throw new MisAlignedAssertionError();
        }

        /**
         * Atomically sets the int at the specified offset within the given object to the given
         * value if it currently holds the expected value. If the offset is not aligned to 4 bytes
         * (which is the size of an int), a MisAlignedAssertionError is thrown.
         *
         * @param object   the object containing the int value
         * @param offset   the offset of the int value within the object
         * @param expected the expected current value
         * @param value    the new value
         * @return true if successful, false otherwise
         * @throws MisAlignedAssertionError if the offset is not aligned to 4 bytes
         */
        @Override
        public boolean compareAndSwapInt(Object object, long offset, int expected, int value) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedInt(offset))
                return super.compareAndSwapInt(object, offset, expected, value);
            throw new MisAlignedAssertionError();
        }

        /**
         * Atomically sets the int at the specified memory address to the given value and
         * returns the old value. If the address is not aligned to 4 bytes (which is the size of an int),
         * a MisAlignedAssertionError is thrown.
         *
         * @param address the memory address of the int value
         * @param value   the new value
         * @return the old value
         * @throws MisAlignedAssertionError if the address is not aligned to 4 bytes
         */
        @Override
        public int getAndSetInt(long address, int value) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedInt(address))
                return super.getAndSetInt(address, value);
            throw new MisAlignedAssertionError();
        }

        /**
         * Atomically sets the int at the specified offset within the given object to the given value
         * and returns the old value. If the offset is not aligned to 4 bytes (which is the size of an int),
         * a MisAlignedAssertionError is thrown.
         *
         * @param object the object containing the int value
         * @param offset the offset of the int value within the object
         * @param value  the new value
         * @return the old value
         * @throws MisAlignedAssertionError if the offset is not aligned to 4 bytes
         */
        @Override
        public int getAndSetInt(Object object, long offset, int value) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedInt(offset))
                return super.getAndSetInt(object, offset, value);
            throw new MisAlignedAssertionError();
        }

        /**
         * Atomically sets the int at the specified memory address to the given value if it currently
         * holds the expected value. If it doesn't hold the expected value, or if the address is not
         * aligned to 4 bytes (which is the size of an int), an IllegalStateException is thrown.
         *
         * @param address  the memory address of the int value
         * @param offset   the offset of the int value within the memory address
         * @param expected the expected current value
         * @param value    the new value
         * @throws IllegalStateException if the address is not aligned to 4 bytes or if the current value is not the expected value
         */
        @Override
        public void testAndSetInt(long address, long offset, int expected, int value) throws IllegalStateException {
            assert SKIP_ASSERTIONS || address != 0;
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedInt(address)) {
                if (UNSAFE.compareAndSwapInt(null, address, expected, value)) {
                    return;
                }
                int actual = UNSAFE.getIntVolatile(null, address);
                throw new IllegalStateException(CANNOT_CHANGE_AT + offset + EXPECTED + expected + WAS + actual);
            } else {
                UNSAFE.loadFence();
                int actual = UNSAFE.getInt(address);
                if (actual == expected) {
                    UNSAFE.putInt(address, value);
                    UNSAFE.storeFence();
                    return;
                }
                throw new IllegalStateException(CANNOT_CHANGE_AT + offset + EXPECTED + expected + WAS + actual + " (mis-aligned)");
            }
        }

        /**
         * Atomically sets the int at the specified offset within the given object to the given
         * value if it currently holds the expected value. If it doesn't hold the expected value, or if the
         * offset is not aligned to 4 bytes (which is the size of an int), an IllegalStateException is thrown.
         *
         * @param object   the object containing the int value
         * @param offset   the offset of the int value within the object
         * @param expected the expected current value
         * @param value    the new value
         * @throws IllegalStateException if the offset is not aligned to 4 bytes or if the current value is not the expected value
         */
        @Override
        public void testAndSetInt(Object object, long offset, int expected, int value) throws IllegalStateException {
            assert SKIP_ASSERTIONS || nonNull(object);
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedInt(offset)) {
                if (UNSAFE.compareAndSwapInt(object, offset, expected, value)) {
                    return;
                }
                int actual = UNSAFE.getIntVolatile(object, offset);
                throw new IllegalStateException("Cannot change " + object.getClass().getSimpleName() + " at " + offset + EXPECTED + expected + WAS + actual);
            } else {
                UNSAFE.loadFence();
                int actual = UNSAFE.getInt(object, offset);
                if (actual == expected) {
                    UNSAFE.putInt(object, offset, value);
                    UNSAFE.storeFence();
                    return;
                }
                throw new IllegalStateException("Cannot perform thread safe operation on " + object.getClass().getSimpleName() + " at " + offset + " as mis-aligned");
            }
        }

        /**
         * Writes the specified double value to the specified memory address.
         * If the address is not aligned to 8 bytes (which is the size of a long), the double value is converted
         * to a long using Double.doubleToRawLongBits(d) and then written.
         *
         * @param address the memory address
         * @param d       the double value to write
         */
        @Override
        public void writeDouble(long address, double d) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address))
                super.writeDouble(address, d);
            else
                super.writeLong(address, Double.doubleToRawLongBits(d));
        }

        /**
         * Reads a double value from the specified memory address.
         * If the address is not aligned to 8 bytes (which is the size of a long), a long is read and then converted
         * to a double using Double.longBitsToDouble().
         *
         * @param address the memory address
         * @return the read double value
         */
        @Override
        public double readDouble(long address) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address))
                return super.readDouble(address);
            return Double.longBitsToDouble(super.readLong(address));
        }

        /**
         * Writes the specified double value to the specified offset within the given object.
         * If the offset is not aligned to 8 bytes (which is the size of a long), the double value is converted
         * to a long using Double.doubleToRawLongBits(d) and then written.
         *
         * @param object the object containing the double value
         * @param offset the offset of the double value within the object
         * @param d      the double value to write
         */
        @Override
        public void writeDouble(Object object, long offset, double d) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedLong(offset))
                super.writeDouble(object, offset, d);
            else
                super.writeLong(object, offset, Double.doubleToRawLongBits(d));
        }

        /**
         * Reads a double value from the specified offset within the given object.
         * If the offset is not aligned to 8 bytes (which is the size of a long), a long is read and then converted
         * to a double using Double.longBitsToDouble().
         *
         * @param object the object containing the double value
         * @param offset the offset of the double value within the object
         * @return the read double value
         */
        @Override
        public double readDouble(Object object, long offset) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedLong(offset))
                return super.readDouble(object, offset);
            return Double.longBitsToDouble(super.readLong(object, offset));
        }

        /**
         * Writes the specified long value to the specified memory address using an ordered/lazy write.
         * If the address is not aligned to 8 bytes (which is the size of a long), it falls back to
         * an unsafe put and store fence.
         *
         * @param address the memory address
         * @param i       the long value to write
         */
        @Override
        public void writeOrderedLong(long address, long i) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address)) {
                super.writeOrderedLong(address, i);
            } else {
                UNSAFE.putLong(address, i);
                UNSAFE.storeFence();
            }
        }

        /**
         * Reads a volatile long value from the specified memory address.
         * If the address is not aligned to 8 bytes (which is the size of a long), it performs an unsafe load fence and
         * a normal read.
         *
         * @param address the memory address
         * @return the read long value
         */
        @Override
        public long readVolatileLong(long address) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address))
                return super.readVolatileLong(address);
            UNSAFE.loadFence();
            return readLong(address);
        }

        /**
         * Writes the specified long value to the specified offset within the given object using an ordered/lazy write.
         * If the offset is not aligned to 8 bytes (which is the size of a long), it falls back to
         * an unsafe put and store fence.
         *
         * @param object the object containing the long value
         * @param offset the offset of the long value within the object
         * @param i      the long value to write
         */
        @Override
        public void writeOrderedLong(Object object, long offset, long i) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedLong(offset)) {
                super.writeOrderedLong(object, offset, i);
            } else {
                UNSAFE.putLong(object, offset, i);
                UNSAFE.storeFence();
            }
        }

        /**
         * Reads a volatile long value from the specified offset within the given object.
         * If the offset is not aligned to 8 bytes (which is the size of a long), it performs an unsafe load fence and
         * a normal read.
         *
         * @param object the object containing the long value
         * @param offset the offset of the long value within the object
         * @return the read long value
         */
        @Override
        public long readVolatileLong(Object object, long offset) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedLong(offset))
                return super.readVolatileLong(object, offset);
            UNSAFE.loadFence();
            return readLong(object, offset);
        }

        /**
         * Reads a volatile double value from the specified memory address.
         * If the address is not aligned to 8 bytes (which is the size of a long), it performs an unsafe load fence and
         * a normal read.
         *
         * @param address the memory address
         * @return the read double value
         */
        @Override
        public double readVolatileDouble(long address) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address))
                return super.readVolatileDouble(address);
            UNSAFE.loadFence();
            return readDouble(address);
        }

        /**
         * Writes the specified long value to the given object at the specified offset in a volatile manner.
         * If the offset is not aligned to 8 bytes (which is the size of a long), it performs a non-atomic write and a store fence.
         *
         * @param object the object containing the long value
         * @param offset the offset of the long value within the object
         * @param i64    the long value to write
         */
        @Override
        public void writeVolatileLong(Object object, long offset, long i64) {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedLong(offset)) {
                super.writeVolatileLong(object, offset, i64);
            } else {
                writeLong(object, offset, i64);
                UNSAFE.storeFence();
            }
        }

        /**
         * Writes the specified long value to the specified memory address in a volatile manner.
         * If the address is not aligned to 8 bytes (which is the size of a long), it performs a non-atomic write and a store fence.
         *
         * @param address the memory address
         * @param i64     the long value to write
         */
        @Override
        public void writeVolatileLong(long address, long i64) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address)) {
                super.writeVolatileLong(address, i64);
            } else {
                writeLong(address, i64);
                UNSAFE.storeFence();
            }
        }

        /**
         * Writes the specified double value to the specified memory address in a volatile manner.
         * If the address is not aligned to 8 bytes (which is the size of a long), it performs a non-atomic write and a store fence.
         *
         * @param address the memory address
         * @param d       the double value to write
         */
        @Override
        public void writeVolatileDouble(long address, double d) {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address))
                super.writeVolatileDouble(address, d);
            else
                writeLong(address, Double.doubleToRawLongBits(d));
        }

        /**
         * Adds the specified increment to the long value at the specified memory address.
         * If the address is not aligned to 8 bytes (which is the size of a long), it throws a MisAlignedAssertionError.
         *
         * @param address   the memory address
         * @param increment the increment to add
         * @return the result of the addition
         * @throws MisAlignedAssertionError if the address is not aligned to 8 bytes
         */
        @Override
        public long addLong(long address, long increment) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address))
                return super.addLong(address, increment);
            throw new MisAlignedAssertionError();
        }

        /**
         * Atomically updates the long value at the given offset in the specified object if the current value equals the expected value.
         * If the offset is not aligned to 8 bytes (which is the size of a long), it throws a MisAlignedAssertionError.
         *
         * @param object   the object containing the long value
         * @param offset   the offset of the long value within the object
         * @param expected the expected current value
         * @param value    the new value
         * @return true if the current value was equal to the expected value, false otherwise
         * @throws MisAlignedAssertionError if the offset is not aligned to 8 bytes
         */
        @Override
        public boolean compareAndSwapLong(Object object, long offset, long expected, long value) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || assertIfEnabled(Longs.nonNegative(), offset);
            if (safeAlignedLong(offset))
                return super.compareAndSwapLong(object, offset, expected, value);
            throw new MisAlignedAssertionError();
        }

        /**
         * Atomically updates the long value at the specified memory address if the current value equals the expected value.
         * If the address is not aligned to 8 bytes (which is the size of a long), it throws a MisAlignedAssertionError.
         *
         * @param address  the memory address
         * @param expected the expected current value
         * @param value    the new value
         * @return true if the current value was equal to the expected value, false otherwise
         * @throws MisAlignedAssertionError if the address is not aligned to 8 bytes
         */
        @Override
        public boolean compareAndSwapLong(long address, long expected, long value) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || address != 0;
            if (safeAlignedLong(address))
                return super.compareAndSwapLong(address, expected, value);
            throw new MisAlignedAssertionError();
        }

        /**
         * Checks if the specified memory address is aligned to 4 bytes (which is the size of an int).
         *
         * @param addr the memory address
         * @return true if the address is aligned to 4 bytes, false otherwise
         */
        @Override
        public boolean safeAlignedInt(long addr) {
            return (addr & 3) == 0;
        }

        /**
         * Checks if the specified memory address is aligned to 8 bytes (which is the size of a long).
         *
         * @param addr the memory address
         * @return true if the address is aligned to 8 bytes, false otherwise
         */
        @Override
        public boolean safeAlignedLong(long addr) {
            return (addr & 7) == 0;
        }

        /**
         * Adds the specified increment to the long value at the given offset in the specified object.
         * If the offset is not aligned to 8 bytes (which is the size of a long), it throws a MisAlignedAssertionError.
         *
         * @param object    the object containing the long value
         * @param offset    the offset of the long value within the object
         * @param increment the increment to add
         * @return the result of the addition
         * @throws MisAlignedAssertionError if the offset is not aligned to 8 bytes
         */
        @Override
        public long addLong(Object object, long offset, long increment) throws MisAlignedAssertionError {
            assert SKIP_ASSERTIONS || offset > 8;
            if (safeAlignedLong(offset))
                return super.addLong(object, offset, increment);
            throw new MisAlignedAssertionError();
        }
    }
}
