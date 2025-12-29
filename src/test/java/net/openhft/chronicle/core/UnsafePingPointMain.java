/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core;

import org.jetbrains.annotations.NotNull;

public class UnsafePingPointMain implements Runnable {
    private final Memory memory;
    private final long addrA;
    private final long addrB;

    private UnsafePingPointMain(Memory memory, long addrA, long addrB) {
        this.memory = memory;
        this.addrA = addrA;
        this.addrB = addrB;
    }

    @NotNull
    private static Memory getMemory() {
        return OS.memory();
    }

    public static void main(String[] args) {
        @NotNull Memory memory = getMemory();
        // make sure its a memory mapping.
        long baseAddress = memory.allocate(256 << 10);

        long addr1 = baseAddress + 63;
        long addr2 = addr1 + 4096;

        new Thread(new UnsafePingPointMain(memory, addr1, addr2)).start();
        new Thread(new UnsafePingPointMain(memory, addr2, addr1)).start();
    }

    @Override
    public void run() {
        for (int i = 0; i < 10000000; i++) {
            toggle(0, -1);
            toggle(-1, 0);

        }
    }

    private void toggle(int x, int y) {
        assert memory.compareAndSwapInt(addrA, x, y);
        int value = memory.readVolatileInt(addrB);
        int count = 1000;
        while (value != y && count-- > 0) {
            if (value != x)
                System.out.println(Long.toHexString(addrB) + " was " + Integer.toHexString(value));
            value = memory.readVolatileInt(addrB);
        }
    }
}
