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

import net.openhft.chronicle.core.annotation.ForceInline;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

public enum UnsafeMemory implements Memory {
    INSTANCE;

    public static final Unsafe UNSAFE;
    // see java.nio.Bits.copyMemory
    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safepoint polling
    // during a large copy
    static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;

    static {
        try {
            Field theUnsafe = Jvm.getField(Unsafe.class, "theUnsafe");
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    private final AtomicLong nativeMemoryUsed = new AtomicLong();

    public <E> E allocateInstance(Class<E> clazz) throws InstantiationException {
        @SuppressWarnings("unchecked")
        E e = (E) UNSAFE.allocateInstance(clazz);
        return e;
    }

    @Override
    public long getFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    @Override
    public void setInt(Object o, long offset, int value) {
        UNSAFE.putInt(o, offset, value);
    }

    @Override
    public <T> T getObject(Object o, long offset) {
        return (T) UNSAFE.getObject(o, offset);
    }

    @Override
    @ForceInline
    public void storeFence() {
        UNSAFE.storeFence();
    }

    @Override
    @ForceInline
    public void loadFence() {
        UNSAFE.loadFence();
    }

    @Override
    @ForceInline
    public void setMemory(long address, long size, byte b) {
        UNSAFE.setMemory(address, size, b);
    }

    @Override
    public void freeMemory(long address, long size) {
        if (address != 0)
            UNSAFE.freeMemory(address);
        nativeMemoryUsed.addAndGet(-size);
    }

    @Override
    public long allocate(long capacity) throws IllegalArgumentException {
        if (capacity <= 0)
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        long address = UNSAFE.allocateMemory(capacity);
        if (address == 0)
            throw new OutOfMemoryError("Not enough free native memory, capacity attempted: " + capacity / 1024 + " KiB");

        nativeMemoryUsed.addAndGet(capacity);

        return address;
    }

    @Override
    public long nativeMemoryUsed() {
        return nativeMemoryUsed.get();
    }

    @Override
    @ForceInline
    public void writeByte(long address, byte b) {
        UNSAFE.putByte(address, b);
    }

    @Override
    @ForceInline
    public void writeByte(Object object, long offset, byte b) {
        UNSAFE.putByte(object, offset, b);
    }

    @Override
    @ForceInline
    public byte readByte(Object object, long offset) {
        return UNSAFE.getByte(object, offset);
    }

    @Override
    @ForceInline
    public byte readByte(long address) {
        return UNSAFE.getByte(address);
    }

    @Override
    @ForceInline
    public void writeShort(long address, short i16) {
        UNSAFE.putShort(address, i16);
    }

    @Override
    @ForceInline
    public void writeShort(Object object, long offset, short i16) {
        UNSAFE.putShort(object, offset, i16);
    }

    @Override
    @ForceInline
    public short readShort(long address) {
        return UNSAFE.getShort(address);
    }

    @Override
    @ForceInline
    public short readShort(Object object, long offset) {
        return UNSAFE.getShort(object, offset);
    }

    @Override
    @ForceInline
    public void writeInt(long address, int i32) {
        UNSAFE.putInt(address, i32);
    }

    @Override
    @ForceInline
    public void writeInt(Object object, long offset, int i32) {
        UNSAFE.putInt(object, offset, i32);
    }

    @Override
    @ForceInline
    public void writeOrderedInt(long address, int i32) {
//        assert (address & 0x3) == 0;
        UNSAFE.putOrderedInt(null, address, i32);
    }

    @Override
    @ForceInline
    public void writeOrderedInt(Object object, long offset, int i32) {
        UNSAFE.putOrderedInt(object, offset, i32);
    }

    @Override
    @ForceInline
    public int readInt(long address) {
        return UNSAFE.getInt(address);
    }

    @Override
    @ForceInline
    public int readInt(Object object, long offset) {
        return UNSAFE.getInt(object, offset);
    }

    @Override
    @ForceInline
    public void writeLong(long address, long i64) {
        UNSAFE.putLong(address, i64);
    }

    @Override
    @ForceInline
    public void writeLong(Object object, long offset, long i64) {
        UNSAFE.putLong(object, offset, i64);
    }

    @Override
    @ForceInline
    public long readLong(long address) {
        return UNSAFE.getLong(address);
    }

    @Override
    @ForceInline
    public long readLong(Object object, long offset) {
        return UNSAFE.getLong(object, offset);
    }

    @Override
    @ForceInline
    public void writeFloat(long address, float f) {
        UNSAFE.putFloat(address, f);
    }

    @Override
    @ForceInline
    public void writeFloat(Object object, long offset, float f) {
        UNSAFE.putFloat(object, offset, f);
    }

    @Override
    @ForceInline
    public float readFloat(long address) {
        return UNSAFE.getFloat(address);
    }

    @Override
    @ForceInline
    public float readFloat(Object object, long offset) {
        return UNSAFE.getFloat(object, offset);
    }

    @Override
    @ForceInline
    public void writeDouble(long address, double d) {
        UNSAFE.putDouble(address, d);
    }

    @Override
    @ForceInline
    public void writeDouble(Object object, long offset, double d) {
        UNSAFE.putDouble(object, offset, d);
    }

    @Override
    @ForceInline
    public double readDouble(long address) {
        return UNSAFE.getDouble(address);
    }

    @Override
    @ForceInline
    public double readDouble(Object object, long offset) {
        return UNSAFE.getDouble(object, offset);
    }

    @Override
    @ForceInline
    public void copyMemory(byte[] bytes, int offset, long address, int length) {
        copyMemory(bytes, offset, null, address, length);
    }

    @Override
    @ForceInline
    public void copyMemory(long fromAddress, long address, long length) {
        copyMemory0(null, fromAddress, null, address, length);
    }

    @Override
    @ForceInline
    public void copyMemory(byte[] bytes, int offset, Object obj2, long offset2, int length) {
        copyMemory0(bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, obj2, offset2, length);
    }

    @Override
    @ForceInline
    public void copyMemory(long fromAddress, Object obj2, long offset2, int length) {
        copyMemory0(null, fromAddress, obj2, offset2, length);
    }

    void copyMemory0(Object from, long fromOffset, Object to, long toOffset, long length) {
        // use a loop to ensure there is a safe point every so often.
        while (length > 0) {
            long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
            UNSAFE.copyMemory(from, fromOffset, to, toOffset, size);
            length -= size;
            fromOffset += size;
            toOffset += size;
        }
    }

    @Override
    @ForceInline
    public void writeOrderedLong(long address, long i) {
//        assert (address & 0x7) == 0;
        UNSAFE.putOrderedLong(null, address, i);
    }

    @Override
    @ForceInline
    public void writeOrderedLong(Object object, long offset, long i) {
        UNSAFE.putOrderedLong(object, offset, i);
    }

    @Override
    @ForceInline
    public boolean compareAndSwapInt(long address, int expected, int value) {
//        assert (address & 0x3) == 0;
        return UNSAFE.compareAndSwapInt(null, address, expected, value);
    }

    @Override
    @ForceInline
    public boolean compareAndSwapInt(Object object, long offset, int expected, int value) {
        return UNSAFE.compareAndSwapInt(object, offset, expected, value);
    }

    @Override
    @ForceInline
    public boolean compareAndSwapLong(long address, long expected, long value) {
//        assert (address & 0x7) == 0;
        return UNSAFE.compareAndSwapLong(null, address, expected, value);
    }

    @Override
    @ForceInline
    public boolean compareAndSwapLong(Object object, long offset, long expected, long value) {
        return UNSAFE.compareAndSwapLong(object, offset, expected, value);
    }

    @Override
    public int pageSize() {
        return UNSAFE.pageSize();
    }

    @Override
    @ForceInline
    public byte readVolatileByte(long address) {
        return UNSAFE.getByteVolatile(null, address);
    }

    @Override
    @ForceInline
    public byte readVolatileByte(Object object, long offset) {
        return UNSAFE.getByteVolatile(object, offset);
    }

    @Override
    @ForceInline
    public short readVolatileShort(long address) {
        return UNSAFE.getShortVolatile(null, address);
    }

    @Override
    @ForceInline
    public short readVolatileShort(Object object, long offset) {
        return UNSAFE.getShortVolatile(object, offset);
    }

    @Override
    @ForceInline
    public int readVolatileInt(long address) {
        int value = UNSAFE.getIntVolatile(null, address);
        if (/*value != 256 || */(address & 63) != 63) {
            return value;
        }
//        Thread.yield();
        int value2 = UNSAFE.getIntVolatile(null, address);
        while (value2 != value) {
            if (value == 256 || value2 == 256)
                System.out.println(Long.toHexString(address) + " (" + (address & 63) + ") " +
                        "was " + Integer.toHexString(value) +
                        " is now " + Integer.toHexString(value2));
            value = value2;
            value2 = UNSAFE.getIntVolatile(null, address);
        }
        return value;
    }

    @Override
    @ForceInline
    public int readVolatileInt(Object object, long offset) {
        return UNSAFE.getIntVolatile(object, offset);
    }

    @Override
    @ForceInline
    public float readVolatileFloat(long address) {
        return UNSAFE.getFloatVolatile(null, address);
    }

    @Override
    @ForceInline
    public float readVolatileFloat(Object object, long offset) {
        return UNSAFE.getFloatVolatile(object, offset);
    }

    @Override
    @ForceInline
    public long readVolatileLong(long address) {
        return UNSAFE.getLongVolatile(null, address);
    }

    @Override
    @ForceInline
    public long readVolatileLong(Object object, long offset) {
        return UNSAFE.getLongVolatile(object, offset);
    }

    @Override
    @ForceInline
    public double readVolatileDouble(long address) {
        return UNSAFE.getDoubleVolatile(null, address);
    }

    @Override
    @ForceInline
    public double readVolatileDouble(Object object, long offset) {
        return UNSAFE.getDoubleVolatile(object, offset);
    }

    @Override
    @ForceInline
    public void writeVolatileByte(long address, byte b) {
        UNSAFE.putByteVolatile(null, address, b);
    }

    @Override
    @ForceInline
    public void writeVolatileByte(Object object, long offset, byte b) {
        UNSAFE.putByteVolatile(object, offset, b);
    }

    @Override
    @ForceInline
    public void writeVolatileShort(long address, short i16) {
        UNSAFE.putShortVolatile(null, address, i16);
    }

    @Override
    @ForceInline
    public void writeVolatileShort(Object object, long offset, short i16) {
        UNSAFE.putShortVolatile(object, offset, i16);
    }

    @Override
    @ForceInline
    public void writeVolatileInt(long address, int i32) {
//        assert (address & 0x3) == 0;
        UNSAFE.putIntVolatile(null, address, i32);
    }

    @Override
    @ForceInline
    public void writeVolatileInt(Object object, long offset, int i32) {
        UNSAFE.putIntVolatile(object, offset, i32);
    }

    @Override
    @ForceInline
    public void writeVolatileFloat(long address, float f) {
//        assert (address & 0x3) == 0;
        UNSAFE.putFloatVolatile(null, address, f);
    }

    @Override
    @ForceInline
    public void writeVolatileFloat(Object object, long offset, float f) {
        UNSAFE.putFloatVolatile(object, offset, f);
    }

    @Override
    @ForceInline
    public void writeVolatileLong(long address, long i64) {
//        assert (address & 0x7) == 0;
        UNSAFE.putLongVolatile(null, address, i64);
    }

    @Override
    @ForceInline
    public void writeVolatileLong(Object object, long offset, long i64) {
        UNSAFE.putLongVolatile(object, offset, i64);
    }

    @Override
    @ForceInline
    public void writeVolatileDouble(long address, double d) {
//        assert (address & 0x7) == 0;

        UNSAFE.putDoubleVolatile(null, address, d);
    }

    @Override
    @ForceInline
    public void writeVolatileDouble(Object object, long offset, double d) {
        UNSAFE.putDoubleVolatile(object, offset, d);
    }

    @Override
    @ForceInline
    public int addInt(long address, int increment) {
//        assert (address & 0x3) == 0;
        return UNSAFE.getAndAddInt(null, address, increment) + increment;
    }

    @Override
    @ForceInline
    public int addInt(Object object, long offset, int increment) {
//        assert (offset & 0x3) == 0;
        return UNSAFE.getAndAddInt(object, offset, increment) + increment;
    }

    @Override
    @ForceInline
    public long addLong(long address, long increment) {
//        assert (address & 0x7) == 0;
        return UNSAFE.getAndAddLong(null, address, increment) + increment;
    }

    @Override
    @ForceInline
    public long addLong(Object object, long offset, long increment) {
//        assert (offset & 0x7) == 0;
        return UNSAFE.getAndAddLong(object, offset, increment) + increment;
    }
}
