/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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
import net.openhft.chronicle.core.util.MisAlignedAssertionError;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Low level memory access
 */
public interface Memory {
    default long heapUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    void storeFence();

    void loadFence();

    void setMemory(long address, long size, byte b);

    void setMemory(Object o, long offset, long size, byte b);

    void freeMemory(long address, long size);

    /**
     * Allocates memory and returns the low level base address of the newly allocated
     * memory region.
     *
     * @param capacity to allocate
     * @return the low level base address of the newly allocated
     * memory region
     * @throws IllegalArgumentException if the capacity is non-positive
     * @throws OutOfMemoryError         if there are not enough memory to allocate
     */
    long allocate(@Positive long capacity);

    long nativeMemoryUsed();

    void writeByte(long address, byte i8);

    void writeByte(Object object, long offset, byte b);

    byte readByte(Object object, long offset);

    void writeBytes(long address, byte[] b, int offset, int length) throws IllegalArgumentException;

    void readBytes(long address, byte[] b, long offset, int length) throws IllegalArgumentException;

    byte readByte(long address);

    void writeShort(long address, short i16);

    void writeShort(Object object, long offset, short i16);

    short readShort(long address);

    short readShort(Object object, long offset);

    void writeInt(long address, int i32);

    void writeInt(Object object, long offset, int i32);

    void writeOrderedInt(long offset, int i32);

    void writeOrderedInt(Object object, long offset, int i32);

    int readInt(long address);

    int readInt(Object object, long offset);

    void writeLong(long address, long i64);

    void writeLong(Object object, long offset, long i64);

    long readLong(long address);

    long readLong(Object object, long offset);

    void writeFloat(long address, float f);

    void writeFloat(Object object, long offset, float f);

    float readFloat(long address);

    float readFloat(Object object, long offset);

    void writeDouble(long address, double d);

    void writeDouble(Object object, long offset, double d);

    double readDouble(long address);

    double readDouble(Object object, long offset);

    void copyMemory(byte[] bytes, int offset, long address, int length);

    void copyMemory(long fromAddress, long address, long length);

    @Deprecated(/* to be removed in x.24 */)
    void copyMemory(byte[] bytes, int offset, Object obj2, long offset2, int length);

    void copyMemory(Object o, long offset, long toAddress, int length);

    void copyMemory(Object o, long offset, Object o2, long offset2, int length);

    void copyMemory(long fromAddress, Object obj2, long offset2, int length);

    int stopBitLength(int i);

    int stopBitLength(long l);

    long partialRead(byte[] bytes, int offset, int length);

    long partialRead(long addr, int length);

    void partialWrite(byte[] bytes, int offset, long value, int length);

    void partialWrite(long addr, long value, int length);

    boolean is7Bit(byte[] bytes, int offset, int length);

    boolean is7Bit(char[] chars, int offset, int length);

    boolean is7Bit(long address, int length);

    void writeOrderedLong(long address, long i);

    void writeOrderedLong(Object object, long offset, long i);

    // Throws IllegalStateException
    void testAndSetInt(long address, long offset, int expected, int value) throws IllegalStateException;

    // throws IllegalStateException
    void testAndSetInt(Object object, long offset, int expected, int value) throws IllegalStateException;

    boolean compareAndSwapInt(long address, int expected, int value) throws MisAlignedAssertionError;

    boolean compareAndSwapInt(Object object, long offset, int expected, int value) throws MisAlignedAssertionError;

    boolean compareAndSwapLong(long address, long expected, long value) throws MisAlignedAssertionError;

    boolean compareAndSwapLong(Object object, long offset, long expected, long value) throws MisAlignedAssertionError;

    int pageSize();

    byte readVolatileByte(long address);

    byte readVolatileByte(Object object, long offset);

    short readVolatileShort(long address);

    short readVolatileShort(Object object, long offset);

    int readVolatileInt(long address);

    int readVolatileInt(Object object, long offset);

    float readVolatileFloat(long address);

    float readVolatileFloat(Object object, long offset);

    long readVolatileLong(long address);

    long readVolatileLong(Object object, long offset);

    double readVolatileDouble(long address);

    double readVolatileDouble(Object object, long offset);

    void writeVolatileByte(long address, byte b);

    void writeVolatileByte(Object object, long offset, byte b);

    void writeVolatileShort(long address, short i16);

    void writeVolatileShort(Object object, long offset, short i16);

    void writeVolatileInt(long address, int i32);

    void writeVolatileInt(Object object, long offset, int i32);

    void writeVolatileFloat(long address, float f);

    void writeVolatileFloat(Object object, long offset, float f);

    void writeVolatileLong(long address, long i64);

    void writeVolatileLong(Object object, long offset, long i64);

    void writeVolatileDouble(long address, double d);

    void writeVolatileDouble(Object object, long offset, double d);

    int addInt(long address, int increment) throws MisAlignedAssertionError;

    int addInt(Object object, long offset, int increment);

    long addLong(long address, long increment) throws MisAlignedAssertionError;

    long addLong(Object object, long offset, long increment) throws MisAlignedAssertionError;

    @NotNull <E> E allocateInstance(Class<? extends E> clazz) throws InstantiationException;

    long getFieldOffset(Field field);

    void putObject(@NotNull Object o, long offset, Object value);

    @NotNull <T> T getObject(@NotNull Object o, long offset);

    int arrayBaseOffset(Class<?> type);

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

    boolean safeAlignedInt(long addr);

    boolean safeAlignedLong(long addr);

    long address(ByteBuffer bb);
}
