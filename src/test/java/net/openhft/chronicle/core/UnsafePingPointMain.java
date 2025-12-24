/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UnsafePingPointMain implements Runnable {
    private final UnsafeFacade unsafe;
    private final long addrA;
    private final long addrB;

    private UnsafePingPointMain(UnsafeFacade unsafe, long addrA, long addrB) {
        this.unsafe = unsafe;
        this.addrA = addrA;
        this.addrB = addrB;
    }

    @NotNull
    private static UnsafeFacade getUnsafe() {
        return UnsafeFacade.create();
    }

    public static void main(String[] args) {
        @NotNull UnsafeFacade unsafe = getUnsafe();
        // make sure its a memory mapping.
        long memory = unsafe.allocateMemory(256 << 10);

        long addr1 = memory + 63;
        long addr2 = addr1 + 4096;

        new Thread(new UnsafePingPointMain(unsafe, addr1, addr2)).start();
        new Thread(new UnsafePingPointMain(unsafe, addr2, addr1)).start();
    }

    @Override
    public void run() {
        for (int i = 0; i < 10000000; i++) {
            toggle(0, -1);
            toggle(-1, 0);

        }
    }

    private void toggle(int x, int y) {
        assert unsafe.compareAndSwapInt(null, addrA, x, y);
        int value = unsafe.getIntVolatile(null, addrB);
        int count = 1000;
        while (value != y && count-- > 0) {
            if (value != x)
                System.out.println(Long.toHexString(addrB) + " was " + Integer.toHexString(value));
            value = unsafe.getIntVolatile(null, addrB);
        }
    }

    private static final class UnsafeFacade {
        private final Object unsafe;
        private final Method allocateMemory;
        private final Method compareAndSwapInt;
        private final Method getIntVolatile;

        private UnsafeFacade(Object unsafe, Method allocateMemory, Method compareAndSwapInt, Method getIntVolatile) {
            this.unsafe = unsafe;
            this.allocateMemory = allocateMemory;
            this.compareAndSwapInt = compareAndSwapInt;
            this.getIntVolatile = getIntVolatile;
        }

        static UnsafeFacade create() {
            try {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                Object unsafe = theUnsafe.get(null);
                return new UnsafeFacade(
                        unsafe,
                        unsafeClass.getMethod("allocateMemory", long.class),
                        unsafeClass.getMethod("compareAndSwapInt", Object.class, long.class, int.class, int.class),
                        unsafeClass.getMethod("getIntVolatile", Object.class, long.class));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Failed to access Unsafe via reflection", e);
            }
        }

        long allocateMemory(long bytes) {
            try {
                return (long) allocateMemory.invoke(unsafe, bytes);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Unsafe allocateMemory invocation failed", e);
            }
        }

        boolean compareAndSwapInt(Object target, long offset, int expected, int value) {
            try {
                return (boolean) compareAndSwapInt.invoke(unsafe, target, offset, expected, value);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Unsafe compareAndSwapInt invocation failed", e);
            }
        }

        int getIntVolatile(Object target, long offset) {
            try {
                return (int) getIntVolatile.invoke(unsafe, target, offset);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Unsafe getIntVolatile invocation failed", e);
            }
        }
    }
}
