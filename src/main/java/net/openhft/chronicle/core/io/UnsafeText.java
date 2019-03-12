package net.openhft.chronicle.core.io;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public enum UnsafeText {
    ;

    public static long appendBase10(long address, long num) {
        if (num >= 0) {
            // nothing
        } else if (num > Long.MIN_VALUE) {
            UNSAFE.putByte(address++, (byte) '-');
            num = -num;
        } else {
            return appendBase10(appendBase10(address, Long.MIN_VALUE / 10), 8);
        }

        long start = address;
        do {
            long div = num / 10;
            long mod = num % 10;
            UNSAFE.putByte(address++, (byte) ('0' + mod));
            num = div;
        } while (num > 0);
        // reverse the order
        int end = (int) (address - start) - 1;
        for (int i = 0; i < end; i++, end--) {
            long a1 = start + i;
            long a2 = start + end;
            byte b1 = UNSAFE.getByte(a1);
            byte b2 = UNSAFE.getByte(a2);
            UNSAFE.putByte(a2, b1);
            UNSAFE.putByte(a1, b2);
        }
        return address;
    }
}
