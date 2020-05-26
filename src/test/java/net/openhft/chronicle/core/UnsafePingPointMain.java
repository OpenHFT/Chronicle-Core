/*
 * Copyright 2016-2020 Chronicle Software
 *
 * https://chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafePingPointMain implements Runnable {
    private final Unsafe unsafe;
    private final long addrA;
    private final long addrB;

    public UnsafePingPointMain(Unsafe unsafe, long addrA, long addrB) {
        this.unsafe = unsafe;
        this.addrA = addrA;
        this.addrB = addrB;
    }

    @NotNull
    public static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (@NotNull NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) {
        @NotNull Unsafe unsafe = getUnsafe();
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
//        System.out.println(Thread.currentThread().getName() + " x: " + x + " y: " + y);
        if (!unsafe.compareAndSwapInt(null, addrA, x, y)) {
            assert false;
        }
        int value = unsafe.getIntVolatile(null, addrB);
        int count = 1000;
        while (value != y && count-- > 0) {
            if (value != x)
                System.out.println(Long.toHexString(addrB) + " was " + Integer.toHexString(value));
            value = unsafe.getIntVolatile(null, addrB);
        }
    }
}
