package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Maths;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public enum RawText {
    ;

    public static final String MIN_VALUE_STRING = "" + Long.MIN_VALUE;

    public static long appendBase10(long address, long num) {
        if (num >= 0) {
            // nothing
        } else if (num > Long.MIN_VALUE) {
            UNSAFE.putByte(address++, (byte) '-');
            num = -num;
        } else {
            return append8bit(address, MIN_VALUE_STRING);
        }
//        int digits = Maths.digits(num);
        long start = address;
        do {
            UNSAFE.putByte(address++, (byte) ('0' + num % 10));
            num /= 10;
        } while (num > 0);
        // reverse the order
        int half = (int) ((address - start) >> 1);
        for (int i = 0; i < half; i++) {
            long a1 = address - i - 1;
            long a2 = start + i;
            byte b1 = UNSAFE.getByte(a1);
            byte b2 = UNSAFE.getByte(a2);
            UNSAFE.putByte(a2, b1);
            UNSAFE.putByte(a1, b2);
        }
        return address;
    }


    private static long append8bit(long address, String s) {
        throw new UnsupportedOperationException();
    }
}
