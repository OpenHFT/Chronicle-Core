/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core;

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

    <E> E allocateInstance(Class<E> clazz);
}
