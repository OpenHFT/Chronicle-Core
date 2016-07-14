package net.openhft.chronicle.core;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by peter on 14/07/16.
 */
public class UnsafePingPointMain implements Runnable {
    private final Unsafe unsafe;
    private final long addrA;
    private final long addrB;

    public UnsafePingPointMain(Unsafe unsafe, long addrA, long addrB) {
        this.unsafe = unsafe;
        this.addrA = addrA;
        this.addrB = addrB;
    }

    public static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) {
        Unsafe unsafe = getUnsafe();
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
