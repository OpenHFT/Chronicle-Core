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

import net.openhft.chronicle.core.annotation.ForceInline;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.concurrent.atomic.AtomicLong;

import static net.openhft.chronicle.core.util.ObjectUtils.requireNonNull;

@SuppressWarnings("unchecked")
public class UnsafeMemory implements Memory {

    @NotNull
    public static final Unsafe UNSAFE;
    public static final UnsafeMemory INSTANCE;
    @Deprecated(/* to be removed in .x21 */)
    public static final boolean tracing = false;
    // see java.nio.Bits.copyMemory
    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safepoint polling
    // during a large copy
    static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    private static final String MIS_ALIGNED = "mis-aligned";

    static {
//        System.getProperties().entrySet().forEach(System.out::println);
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            System.out.println("UNSAFE: " + UNSAFE);
        } catch (@NotNull NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new AssertionError(e);
        }
        INSTANCE = Bootstrap.isArm0() ? new ARMMemory() : new UnsafeMemory();
    }

    private final AtomicLong nativeMemoryUsed = new AtomicLong();

    private static int retryReadVolatileInt(long address, int value) {
        int value2 = UNSAFE.getIntVolatile(null, address);
        while (value2 != value) {
            if (value != 0 && value != 0x80000000)
                Jvm.warn().on(UnsafeMemory.class, "Int@" + Long.toHexString(address) + " (" + (address & 63) + ") " +
                        "was " + Integer.toHexString(value) +
                        " is now " + Integer.toHexString(value2));
            value = value2;
            value2 = UNSAFE.getIntVolatile(null, address);
        }
        return value;
    }

    private static long retryReadVolatileLong(long address, long value) {
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

    public static void putInt(byte[] bytes, int offset, int value) {
        UnsafeMemory.UNSAFE.putInt(bytes,
                (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + offset,
                value);
    }

    public static void unsafeStoreFence() {
        UNSAFE.storeFence();
    }

    public static void unsafeLoadFence() {
        UNSAFE.loadFence();
    }

    public static long unsafeGetLong(long address) {
        return UNSAFE.getLong(address);
    }

    public static int unsafeGetInt(long address) {
        return UNSAFE.getInt(address);
    }

    public static byte unsafeGetByte(long address) {
        return UNSAFE.getByte(address);
    }

    public static void unsafePutLong(long address, long value) {
        UNSAFE.putLong(address, value);
    }

    public static void unsafePutInt(long address, int value) {
        UNSAFE.putInt(address, value);
    }

    public static void unsafePutByte(long address, byte value) {
        UNSAFE.putByte(address, value);
    }

    public static void unsafePutLong(byte[] bytes, int offset, long value) {
        UNSAFE.putLong(bytes, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, value);
    }

    public static void unsafePutInt(byte[] bytes, int offset, int value) {
        UNSAFE.putInt(bytes, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, value);
    }

    public static void unsafePutByte(byte[] bytes, int offset, byte value) {
        UNSAFE.putByte(bytes, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, value);
    }

    public static void copyMemory(long from, long to, int length)
            throws BufferUnderflowException, BufferOverflowException {
        long i = 0;
        for (; i < length - 7; i += 8) {
            unsafePutLong(to, unsafeGetLong(from));
            from += 8;
            to += 8;
        }
        for (; i < length; i++)
            unsafePutByte(to++, unsafeGetByte(from++));
    }

    public static void unsafePutBoolean(@NotNull Object obj, long offset, boolean value) {
        UNSAFE.putBoolean(obj, offset, value);
    }

    public static boolean unsafeGetBoolean(@NotNull Object obj, long offset) {
        return UNSAFE.getBoolean(obj, offset);
    }

    public static void unsafePutByte(@NotNull Object obj, long offset, byte value) {
        UNSAFE.putByte(obj, offset, value);
    }

    public static byte unsafeGetByte(@NotNull Object obj, long offset) {
        return UNSAFE.getByte(obj, offset);
    }

    public static void unsafePutChar(@NotNull Object obj, long offset, char value) {
        UNSAFE.putChar(obj, offset, value);
    }

    public static char unsafeGetChar(@NotNull Object obj, long offset) {
        return UNSAFE.getChar(obj, offset);
    }

    public static void unsafePutShort(@NotNull Object obj, long offset, short value) {
        UNSAFE.putShort(obj, offset, value);
    }

    public static short unsafeGetShort(@NotNull Object obj, long offset) {
        return UNSAFE.getShort(obj, offset);
    }

    public static void unsafePutInt(@NotNull Object obj, long offset, int value) {
        UNSAFE.putInt(obj, offset, value);
    }

    public static int unsafeGetInt(@NotNull Object obj, long offset) {
        return UNSAFE.getInt(obj, offset);
    }

    public static void unsafePutFloat(@NotNull Object obj, long offset, float value) {
        UNSAFE.putFloat(obj, offset, value);
    }

    public static float unsafeGetFloat(@NotNull Object obj, long offset) {
        return UNSAFE.getFloat(obj, offset);
    }

    public static void unsafePutLong(@NotNull Object obj, long offset, long value) {
        UNSAFE.putLong(obj, offset, value);
    }

    public static long unsafeGetLong(@NotNull Object obj, long offset) {
        return UNSAFE.getLong(obj, offset);
    }

    public static void unsafePutDouble(@NotNull Object obj, long offset, double value) {
        UNSAFE.putDouble(obj, offset, value);
    }

    public static double unsafeGetDouble(@NotNull Object obj, long offset) {
        return UNSAFE.getDouble(obj, offset);
    }

    public static void unsafePutObject(@NotNull Object obj, long offset, Object value) {
        UNSAFE.putObject(obj, offset, value);
    }

    public static <T> T unsafeGetObject(@NotNull Object obj, long offset) {
        return (T) UNSAFE.getObject(obj, offset);
    }

    public static long unsafeObjectFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    @NotNull
    @Override
    public <E> E allocateInstance(Class<? extends E> clazz) throws InstantiationException {
        @NotNull
        E e = (E) UNSAFE.allocateInstance(clazz);
        return e;
    }

    @Override
    public long getFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    @Override
    public void setInt(@NotNull Object object, long offset, int value) {
        requireNonNull(object);
        UNSAFE.putInt(object, offset, value);
    }

    @NotNull
    @Override
    public <T> T getObject(@NotNull Object object, long offset) {
        requireNonNull(object);
        return (T) UNSAFE.getObject(object, offset);
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
    public void writeByte(@NotNull Object object, long offset, byte b) {
        requireNonNull(object);
        UNSAFE.putByte(object, offset, b);
    }

    @Override
    @ForceInline
    public byte readByte(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getByte(object, offset);
    }

    @Override
    @ForceInline
    public void writeBytes(long address, byte[] b, int offset, int length) {
        if (offset + length > b.length)
            throw new IllegalArgumentException("Invalid offset or length, array's length is " + b.length);
        UnsafeMemory.UNSAFE.copyMemory(b, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, null, address, length);
    }

    @Override
    @ForceInline
    public void readBytes(long address, byte[] b, long offset, int length) {
        if (offset + length > b.length)
            throw new IllegalArgumentException("Invalid offset or length, array's length is " + b.length);
        UnsafeMemory.UNSAFE.copyMemory(null, address, b, Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, length);
    }

    @Override
    @ForceInline
    public byte readByte(long address) {
//        if (tracing)
//            System.out.println("Read " + Long.toHexString(address));
        return UNSAFE.getByte(address);
    }

    @Override
    @ForceInline
    public void writeShort(long address, short i16) {
        UNSAFE.putShort(address, i16);
    }

    @Override
    @ForceInline
    public void writeShort(@NotNull Object object, long offset, short i16) {
        requireNonNull(object);
        UNSAFE.putShort(object, offset, i16);
    }

    @Override
    @ForceInline
    public short readShort(long address) {
        return UNSAFE.getShort(address);
    }

    @Override
    @ForceInline
    public short readShort(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getShort(object, offset);
    }

    @Override
    @ForceInline
    public void writeInt(long address, int i32) {
        UNSAFE.putInt(address, i32);
    }

    @Override
    @ForceInline
    public void writeInt(@NotNull Object object, long offset, int i32) {
        requireNonNull(object);
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
    public void writeOrderedInt(@NotNull Object object, long offset, int i32) {
        requireNonNull(object);
        UNSAFE.putOrderedInt(object, offset, i32);
    }

    @Override
    @ForceInline
    public int readInt(long address) {
//        if (tracing)
//            System.out.println("Read int " + Long.toHexString(address));
        return UNSAFE.getInt(address);
    }

    @Override
    @ForceInline
    public int readInt(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getInt(object, offset);
    }

    @Override
    @ForceInline
    public void writeLong(long address, long i64) {
        UNSAFE.putLong(address, i64);
    }

    @Override
    @ForceInline
    public void writeLong(@NotNull Object object, long offset, long i64) {
        requireNonNull(object);
        UNSAFE.putLong(object, offset, i64);
    }

    @Override
    @ForceInline
    public long readLong(long address) {
//        if (tracing)
//            System.out.println("Read long " + Long.toHexString(address));
        return UNSAFE.getLong(address);
    }

    @Override
    @ForceInline
    public long readLong(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getLong(object, offset);
    }

    @Override
    @ForceInline
    public void writeFloat(long address, float f) {
        UNSAFE.putFloat(address, f);
    }

    @Override
    @ForceInline
    public void writeFloat(@NotNull Object object, long offset, float f) {
        requireNonNull(object);
        UNSAFE.putFloat(object, offset, f);
    }

    @Override
    @ForceInline
    public float readFloat(long address) {
        return UNSAFE.getFloat(address);
    }

    @Override
    @ForceInline
    public float readFloat(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getFloat(object, offset);
    }

    @Override
    @ForceInline
    public void writeDouble(long address, double d) {
        UNSAFE.putDouble(address, d);
    }

    @Override
    @ForceInline
    public void writeDouble(@NotNull Object object, long offset, double d) {
        requireNonNull(object);
        UNSAFE.putDouble(object, offset, d);
    }

    @Override
    @ForceInline
    public double readDouble(long address) {
        return UNSAFE.getDouble(address);
    }

    @Override
    @ForceInline
    public double readDouble(@NotNull Object object, long offset) {
        requireNonNull(object);
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
        if (length < UNSAFE_COPY_THRESHOLD) {
            UNSAFE.copyMemory(null, fromAddress, null, address, length);

        } else {
            copyMemory0(null, fromAddress, null, address, length);
        }
    }

    @Override
    @ForceInline
    public void copyMemory(byte[] bytes, int offset, Object obj2, long offset2, int length) {
        if (length < UNSAFE_COPY_THRESHOLD) {
            UNSAFE.copyMemory(bytes, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, obj2, offset2, length);
        } else {
            copyMemory0(bytes, (long) Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, obj2, offset2, length);
        }
    }

    @Override
    @ForceInline
    public void copyMemory(long fromAddress, Object obj2, long offset2, int length) {
        long start = length > 128 << 10 ? System.nanoTime() : 0;
        copyMemory0(null, fromAddress, obj2, offset2, length);
        if (length > 128 << 10) {
            long time = System.nanoTime() - start;
            if (time > 100_000)
                Jvm.perf().on(getClass(), "Took " + time / 1000 / 1e3 + " ms to copy " + length / 1024 + " KB");
        }
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
    public void writeOrderedLong(@NotNull Object object, long offset, long i) {
        requireNonNull(object);
        UNSAFE.putOrderedLong(object, offset, i);
    }

    @Override
    public void testAndSetInt(long address, long offset, int expected, int value) throws IllegalStateException {
        if (UNSAFE.compareAndSwapInt(null, address, expected, value))
            return;
        int actual = UNSAFE.getIntVolatile(null, address);
        throw new IllegalStateException("Cannot change at " + offset + " expected " + expected + " was " + actual);
    }

    @Override
    public void testAndSetInt(@NotNull Object object, long offset, int expected, int value) throws IllegalStateException {
        if (UNSAFE.compareAndSwapInt(object, offset, expected, value))
            return;
        int actual = UNSAFE.getIntVolatile(object, offset);
        throw new IllegalStateException("Cannot change " + object.getClass().getSimpleName() + " at " + offset + " expected " + expected + " was " + actual);
    }

    @Override
    @ForceInline
    public boolean compareAndSwapInt(long address, int expected, int value) {
//        assert (address & 0x3) == 0;
        return UNSAFE.compareAndSwapInt(null, address, expected, value);
    }

    @Override
    @ForceInline
    public boolean compareAndSwapInt(@NotNull Object object, long offset, int expected, int value) {
        requireNonNull(object);
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
    public boolean compareAndSwapLong(@NotNull Object object, long offset, long expected, long value) {
        requireNonNull(object);
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
    public byte readVolatileByte(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getByteVolatile(object, offset);
    }

    @Override
    @ForceInline
    public short readVolatileShort(long address) {
        // TODO add support for a short split across cache lines.
        return UNSAFE.getShortVolatile(null, address);
    }

    @Override
    @ForceInline
    public short readVolatileShort(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getShortVolatile(object, offset);
    }

    @Override
    @ForceInline
    public int readVolatileInt(long address) {
        int value = UNSAFE.getIntVolatile(null, address);
        if ((address & 63) <= 60) {
            if (value == 0)
                value = UNSAFE.getIntVolatile(null, address);
            return value;
        }
        return retryReadVolatileInt(address, value);
    }

    @Override
    @ForceInline
    public int readVolatileInt(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getIntVolatile(object, offset);
    }

    @Override
    @ForceInline
    public float readVolatileFloat(long address) {
        // TODO add support for a float split across cache lines.
        return UNSAFE.getFloatVolatile(null, address);
    }

    @Override
    @ForceInline
    public float readVolatileFloat(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getFloatVolatile(object, offset);
    }

    @Override
    @ForceInline
    public long readVolatileLong(long address) {
        long value = UNSAFE.getLongVolatile(null, address);
        if ((address & 63) <= 64 - 8) {
            return value;
        }
        return retryReadVolatileLong(address, value);
    }

    @Override
    @ForceInline
    public long readVolatileLong(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getLongVolatile(object, offset);
    }

    @Override
    @ForceInline
    public double readVolatileDouble(long address) {
        // TODO add support for a double split across cache lines.
        return UNSAFE.getDoubleVolatile(null, address);
    }

    @Override
    @ForceInline
    public double readVolatileDouble(@NotNull Object object, long offset) {
        requireNonNull(object);
        return UNSAFE.getDoubleVolatile(object, offset);
    }

    @Override
    @ForceInline
    public void writeVolatileByte(long address, byte b) {
        UNSAFE.putByteVolatile(null, address, b);
    }

    @Override
    @ForceInline
    public void writeVolatileByte(@NotNull Object object, long offset, byte b) {
        requireNonNull(object);
        UNSAFE.putByteVolatile(object, offset, b);
    }

    @Override
    @ForceInline
    public void writeVolatileShort(long address, short i16) {
        UNSAFE.putShortVolatile(null, address, i16);
    }

    @Override
    @ForceInline
    public void writeVolatileShort(@NotNull Object object, long offset, short i16) {
        requireNonNull(object);
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
    public void writeVolatileInt(@NotNull Object object, long offset, int i32) {
        requireNonNull(object);
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
    public void writeVolatileFloat(@NotNull Object object, long offset, float f) {
        requireNonNull(object);
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
    public void writeVolatileLong(@NotNull Object object, long offset, long i64) {
        requireNonNull(object);
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
    public void writeVolatileDouble(@NotNull Object object, long offset, double d) {
        requireNonNull(object);
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
    public int addInt(@NotNull Object object, long offset, int increment) {
//        assert (offset & 0x3) == 0;
        requireNonNull(object);
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
    public long addLong(@NotNull Object object, long offset, long increment) {
//        assert (offset & 0x7) == 0;
        requireNonNull(object);
        return UNSAFE.getAndAddLong(object, offset, increment) + increment;
    }

    // https://github.com/OpenHFT/OpenHFT/issues/23
    static class ARMMemory extends UnsafeMemory {
        @Override
        public short readVolatileShort(long address) {
            if ((address & 0x1) == 0)
                return super.readVolatileShort(address);
            UNSAFE.loadFence();
            return super.readShort(address);
        }

        @Override
        public void writeVolatileShort(long address, short i16) {
            if ((address & 0x1) == 0) {
                super.writeVolatileShort(address, i16);
            } else {
                super.writeShort(address, i16);
                UNSAFE.storeFence();
            }
        }

        @Override
        public void writeFloat(long address, float f) {
            if ((address & 0x3) == 0)
                super.writeFloat(address, f);
            else
                super.writeInt(address, Float.floatToRawIntBits(f));
        }

        @Override
        public float readFloat(long address) {
            if ((address & 0x3) == 0)
                return super.readFloat(address);
            return Float.intBitsToFloat(super.readInt(address));
        }

        @Override
        public void writeFloat(@NotNull Object object, long offset, float f) {
            if ((offset & 0x3) == 0)
                super.writeFloat(object, offset, f);
            else
                super.writeInt(object, offset, Float.floatToRawIntBits(f));

        }

        @Override
        public float readFloat(@NotNull Object object, long offset) {
            if ((offset & 0x3) == 0)
                return super.readFloat(object, offset);
            return Float.intBitsToFloat(super.readInt(object, offset));
        }

        @Override
        public int readVolatileInt(long address) {
            if ((address & 0x3) == 0)
                return super.readVolatileInt(address);
            UNSAFE.loadFence();
            return super.readInt(address);
        }

        @Override
        public float readVolatileFloat(long address) {
            if ((address & 0x3) == 0)
                return super.readVolatileFloat(address);
            UNSAFE.loadFence();
            return readFloat(address);
        }

        @Override
        public void writeVolatileInt(long address, int i32) {
            if ((address & 0x3) == 0) {
                super.writeVolatileInt(address, i32);
            } else {
                writeInt(address, i32);
                UNSAFE.storeFence();
            }
        }

        @Override
        public void writeOrderedInt(long address, int i32) {
            if ((address & 0x3) == 0)
                super.writeOrderedInt(address, i32);
            else
                writeVolatileInt(address, i32);
        }

        @Override
        public void writeOrderedInt(@NotNull Object object, long offset, int i32) {
            if ((offset & 0x3) == 0)
                super.writeOrderedInt(object, offset, i32);
            else
                super.writeVolatileInt(object, offset, i32);
        }

        @Override
        public void writeVolatileFloat(long address, float f) {
            if ((address & 0x3) == 0)
                super.writeVolatileFloat(address, f);
            else
                writeVolatileInt(address, Float.floatToRawIntBits(f));
        }

        @Override
        public int addInt(long address, int increment) {
            if ((address & 0x3) == 0)
                return super.addInt(address, increment);
            throw new IllegalArgumentException(MIS_ALIGNED);
        }

        @Override
        public boolean compareAndSwapInt(long address, int expected, int value) {
            if ((address & 0x3) == 0)
                return super.compareAndSwapInt(address, expected, value);
            throw new IllegalArgumentException(MIS_ALIGNED);
        }

        @Override
        public boolean compareAndSwapInt(@NotNull Object object, long offset, int expected, int value) {
            if ((offset & 0x3) == 0)
                return super.compareAndSwapInt(object, offset, expected, value);
            throw new IllegalArgumentException(MIS_ALIGNED);
        }

        @Override
        public void testAndSetInt(long address, long offset, int expected, int value) throws IllegalStateException {
            if ((address & ~0x3) == 0) {
                if (UNSAFE.compareAndSwapInt(null, address, expected, value)) {
                    return;
                }
                int actual = UNSAFE.getIntVolatile(null, address);
                throw new IllegalStateException("Cannot change at " + offset + " expected " + expected + " was " + actual);
            } else {
                UNSAFE.loadFence();
                int actual = UNSAFE.getInt(address);
                if (actual == expected) {
                    UNSAFE.putInt(address, value);
                    UNSAFE.storeFence();
                    return;
                }
                throw new IllegalStateException("Cannot perform thread safe operation at " + offset + " as mis-aligned");
            }
        }

        @Override
        public void testAndSetInt(@NotNull Object object, long offset, int expected, int value) throws IllegalStateException {
            if ((offset & ~0x3) == 0) {
                if (UNSAFE.compareAndSwapInt(object, offset, expected, value)) {
                    return;
                }
                int actual = UNSAFE.getIntVolatile(object, offset);
                throw new IllegalStateException("Cannot change " + object.getClass().getSimpleName() + " at " + offset + " expected " + expected + " was " + actual);
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

        @Override
        public void writeDouble(long address, double d) {
            if ((address & 0x7) == 0)
                super.writeDouble(address, d);
            else
                super.writeLong(address, Double.doubleToRawLongBits(d));
        }

        @Override
        public double readDouble(long address) {
            if ((address & 0x7) == 0)
                return super.readDouble(address);
            return Double.longBitsToDouble(super.readLong(address));
        }

        @Override
        public void writeDouble(@NotNull Object object, long offset, double d) {
            if ((offset & 0x7) == 0) super.writeDouble(object, offset, d);
            else
                super.writeLong(object, offset, Double.doubleToRawLongBits(d));
        }

        @Override
        public double readDouble(@NotNull Object object, long offset) {
            if ((offset & 0x7) == 0) return super.readDouble(object, offset);
            return Double.longBitsToDouble(super.readLong(object, offset));
        }

        @Override
        public void writeOrderedLong(long address, long i) {
            if ((address & 0x7) == 0)
                super.writeOrderedLong(address, i);
            else
                writeVolatileLong(address, i);
        }

        @Override
        public long readVolatileLong(long address) {
            if ((address & 0x7) == 0)
                return super.readVolatileLong(address);
            UNSAFE.loadFence();
            return readLong(address);
        }

        @Override
        public void writeOrderedLong(@NotNull Object object, long offset, long i) {
            if ((offset & 0x7) == 0)
                super.writeOrderedLong(object, offset, i);
            else
                writeVolatileLong(object, offset, i);
        }

        @Override
        public long readVolatileLong(@NotNull Object object, long offset) {
            if ((offset & 0x7) == 0) return super.readVolatileLong(object, offset);
            UNSAFE.loadFence();
            return readLong(object, offset);
        }

        @Override
        public double readVolatileDouble(long address) {
            if ((address & 0x7) == 0)
                return super.readVolatileDouble(address);
            UNSAFE.loadFence();
            return readDouble(address);
        }

        @Override
        public void writeVolatileLong(@NotNull Object object, long offset, long i64) {
            if ((offset & 0x7) == 0) {
                super.writeVolatileLong(object, offset, i64);
            } else {
                writeLong(object, offset, i64);
                UNSAFE.storeFence();
            }
        }

        @Override
        public void writeVolatileLong(long address, long i64) {
            if ((address & 0x7) == 0) {
                super.writeVolatileLong(address, i64);
            } else {
                writeLong(address, i64);
                UNSAFE.storeFence();
            }
        }

        @Override
        public void writeVolatileDouble(long address, double d) {
            if ((address & 0x7) == 0)
                super.writeVolatileDouble(address, d);
            else
                writeLong(address, Double.doubleToRawLongBits(d));
        }

        @Override
        public long addLong(long address, long increment) {
            if ((address & 0x7) == 0)
                return super.addLong(address, increment);
            throw new IllegalArgumentException(MIS_ALIGNED);
        }

        @Override
        public boolean compareAndSwapLong(@NotNull Object object, long offset, long expected, long value) {
            if ((offset & 0x7) == 0)
                return super.compareAndSwapLong(object, offset, expected, value);
            throw new IllegalArgumentException(MIS_ALIGNED);
        }

        @Override
        public boolean compareAndSwapLong(long address, long expected, long value) {
            if ((address & 0x7) == 0)
                return super.compareAndSwapLong(address, expected, value);
            throw new IllegalArgumentException(MIS_ALIGNED);
        }
    }
}
