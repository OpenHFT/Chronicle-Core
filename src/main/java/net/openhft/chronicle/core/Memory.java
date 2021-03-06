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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

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

    void freeMemory(long address, long size);

    /**
     * Allocates memory and returns the low level base address of the newly allocated
     * memory region.
     *
     * @param capacity to allocate
     * @return the low level base address of the newly allocated
     *         memory region
     * @throws IllegalArgumentException if the capacity is non-positive
     * @throws OutOfMemoryError if there are not enough memory to allocate
     */
    long allocate(@Positive long capacity);

    long nativeMemoryUsed();

    void writeByte(long address, byte i8);

    void writeByte(@NotNull Object object, long offset, byte b);

    byte readByte(@NotNull Object object, long offset);

    void writeBytes(long address, byte[] b, int offset, int length) throws IllegalArgumentException;

    void readBytes(long address, byte[] b, long offset, int length) throws IllegalArgumentException;

    byte readByte(long address);

    void writeShort(long address, short i16);

    void writeShort(@NotNull Object object, long offset, short i16);

    short readShort(long address);

    short readShort(@NotNull Object object, long offset);

    void writeInt(long address, int i32);

    void writeInt(@NotNull Object object, long offset, int i32);

    void writeOrderedInt(long offset, int i32);

    void writeOrderedInt(@NotNull Object object, long offset, int i32);

    int readInt(long address);

    int readInt(@NotNull Object object, long offset);

    void writeLong(long address, long i64);

    void writeLong(@NotNull Object object, long offset, long i64);

    long readLong(long address);

    long readLong(@NotNull Object object, long offset);

    void writeFloat(long address, float f);

    void writeFloat(@NotNull Object object, long offset, float f);

    float readFloat(long address);

    float readFloat(@NotNull Object object, long offset);

    void writeDouble(long address, double d);

    void writeDouble(@NotNull Object object, long offset, double d);

    double readDouble(long address);

    double readDouble(@NotNull Object object, long offset);

    void copyMemory(byte[] bytes, int offset, long address, int length);

    void copyMemory(long fromAddress, long address, long length);

    void copyMemory(byte[] bytes, int offset, Object obj2, long offset2, int length);

    void copyMemory(long fromAddress, Object obj2, long offset2, int length);

    int stopBitLength(int i);

    int stopBitLength(long l);

    boolean is7Bit(byte[] bytes, int offset, int length);

    boolean is7Bit(char[] chars, int offset, int length);

    boolean is7Bit(long address, int length);

    void writeOrderedLong(long address, long i);

    void writeOrderedLong(@NotNull Object object, long offset, long i);

    // Throws IllegalStateException
    void testAndSetInt(long address, long offset, int expected, int value) throws IllegalStateException;

    // throws IllegalStateException
    void testAndSetInt(@NotNull Object object, long offset, int expected, int value) throws IllegalStateException;

    boolean compareAndSwapInt(long address, int expected, int value);

    boolean compareAndSwapInt(@NotNull Object object, long offset, int expected, int value);

    boolean compareAndSwapLong(long address, long expected, long value);

    boolean compareAndSwapLong(@NotNull Object object, long offset, long expected, long value);

    int pageSize();

    byte readVolatileByte(long address);

    byte readVolatileByte(@NotNull Object object, long offset);

    short readVolatileShort(long address);

    short readVolatileShort(@NotNull Object object, long offset);

    int readVolatileInt(long address);

    int readVolatileInt(@NotNull Object object, long offset);

    float readVolatileFloat(long address);

    float readVolatileFloat(@NotNull Object object, long offset);

    long readVolatileLong(long address);

    long readVolatileLong(@NotNull Object object, long offset);

    double readVolatileDouble(long address);

    double readVolatileDouble(@NotNull Object object, long offset);

    void writeVolatileByte(long address, byte b);

    void writeVolatileByte(@NotNull Object object, long offset, byte b);

    void writeVolatileShort(long address, short i16);

    void writeVolatileShort(@NotNull Object object, long offset, short i16);

    void writeVolatileInt(long address, int i32);

    void writeVolatileInt(@NotNull Object object, long offset, int i32);

    void writeVolatileFloat(long address, float f);

    void writeVolatileFloat(@NotNull Object object, long offset, float f);

    void writeVolatileLong(long address, long i64);

    void writeVolatileLong(@NotNull Object object, long offset, long i64);

    void writeVolatileDouble(long address, double d);

    void writeVolatileDouble(@NotNull Object object, long offset, double d);

    int addInt(long address, int increment);

    int addInt(@NotNull Object object, long offset, int increment);

    long addLong(long address, long increment);

    long addLong(@NotNull Object object, long offset, long increment);

    @NotNull <E> E allocateInstance(Class<? extends E> clazz) throws InstantiationException;

    long getFieldOffset(Field field);

    /**
     * @deprecated Redundant to {@link #writeInt(Object, long, int)}.
     */
    @Deprecated(/* to be removed in x.22 */)
    void setInt(@NotNull Object o, long offset, int value);

    @NotNull <T> T getObject(@NotNull Object o, long offset);
}
