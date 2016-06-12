/*
 * Copyright 2016 higherfrequencytrading.com
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

    long allocate(long capacity) throws IllegalArgumentException;

    long nativeMemoryUsed();

    void writeByte(long address, byte i8);

    void writeByte(Object object, long offset, byte b);

    byte readByte(Object object, long offset);

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

    void copyMemory(byte[] bytes, int offset, Object obj2, long offset2, int length);

    void copyMemory(long fromAddress, Object obj2, long offset2, int length);

    void writeOrderedLong(long address, long i);

    void writeOrderedLong(Object object, long offset, long i);

    boolean compareAndSwapInt(long address, int expected, int value);

    boolean compareAndSwapInt(Object object, long offset, int expected, int value);

    boolean compareAndSwapLong(long address, long expected, long value);

    boolean compareAndSwapLong(Object object, long offset, long expected, long value);

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

    int addInt(long address, int increment);

    int addInt(Object object, long offset, int increment);

    long addLong(long address, long increment);

    long addLong(Object object, long offset, long increment);

    <E> E allocateInstance(Class<E> clazz) throws InstantiationException;

    long getFieldOffset(Field field);

    void setInt(Object o, long offset, int value);

    <T> T getObject(Object o, long offset);
}
