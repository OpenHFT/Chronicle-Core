/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Maths;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;
import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

/**
 * These are fast, unsafe ways to render text.
 * NOTE: The caller has to ensure there is always plenty of memory to perform this operation.
 */
public final class UnsafeText {

    // Suppresses default constructor, ensuring non-instantiability.
    private UnsafeText() {
    }

    private static final long MAX_VALUE_DIVIDE_5 = Long.MAX_VALUE / 5;
    private static final String MIN_VALUE_STR = "" + Long.MIN_VALUE;
    private static final long ARRAY_BYTE_BASE_OFFSET = Jvm.arrayByteBaseOffset();

    public static long appendFixed(long address, long num) {
        if (num >= 0) {
            // nothing
        } else if (num > Long.MIN_VALUE) {
            MEMORY.writeByte(address++, (byte) '-');
            num = -num;
        } else {
            return appendText(address, MIN_VALUE_STR);
        }

        long start = address;
        do {
            long div = num / 10;
            long mod = num % 10;
            MEMORY.writeByte(address++, (byte) ('0' + mod));
            num = div;
        } while (num > 0);
        // reverse the order
        reverseTheOrder(address, start);
        return address;
    }

    private static void reverseTheOrder(long address, long start) {
        int end = (int) (address - start) - 1;
        for (int i = 0; i < end; i++, end--) {
            long a1 = start + i;
            long a2 = start + end;
            byte b1 = UNSAFE.getByte(a1);
            byte b2 = UNSAFE.getByte(a2);
            MEMORY.writeByte(a2, b1);
            MEMORY.writeByte(a1, b2);
        }
    }

    public static long appendFixed(long address, double num, int digits) {
        long tens = Maths.tens(digits);
        double mag = num * tens;
        if (Math.abs(mag) < 1L << 53) {
            long num2 = Math.round(mag);
            return appendBase10d(address, num2, digits);
        } else {
            return appendDouble(address, num);
        }
    }

    public static long appendBase10d(long address, long num, int decimal) {
        if (num >= 0) {
            // nothing
        } else if (num > Long.MIN_VALUE) {
            MEMORY.writeByte(address++, (byte) '-');
            num = -num;
        } else {
            throw new AssertionError();
        }

        long start = address;
        do {
            long div = num / 10;
            long mod = num % 10;
            MEMORY.writeByte(address++, (byte) ('0' + mod));
            if (--decimal == 0)
                MEMORY.writeByte(address++, (byte) '.');
            num = div;
        } while (num > 0 || decimal >= 0);
        // reverse the order
        reverseTheOrder(address, start);
        return address;
    }

    //      throws BufferOverflowException, IllegalArgumentException
    public static long appendDouble(long address, double d) {
        long val = Double.doubleToRawLongBits(d);
        int sign = (int) (val >>> 63);
        int exp = (int) ((val >>> 52) & 2047);
        long mantissa = val & ((1L << 52) - 1);
        if (sign != 0) {
            MEMORY.writeByte(address++, (byte) '-');
        }
        if (exp == 0 && mantissa == 0) {
            MEMORY.writeByte(address, (byte) '0');
            UNSAFE.putShort(address + 1, (short) ('.' + ('0' << 8)));
            address += 3;
            return address;

        } else if (exp == 2047) {
            return appendText(address,
                    mantissa == 0 ? "Infinity" : "NaN");

        } else if (exp > 0) {
            mantissa += 1L << 52;
        }
        final int shift = (1023 + 52) - exp;

        if (shift > 0) {
            // integer and faction
            if (shift < 53) {
                return appendIntegerAndFraction(address, d, sign, mantissa, shift);

            } else {
                return appendFraction(address, d, sign, mantissa, shift);
            }
        }
        // large number
        return appendLargeNumber(address, mantissa, shift);
    }

    private static long appendLargeNumber(long address, long mantissa, int shift) {
        mantissa <<= 10;
        int precision = -10 - shift;
        int digits = 0;
        while ((precision > 53 || mantissa > Long.MAX_VALUE >> precision) && precision > 0) {
            digits++;
            precision--;
            long mod = mantissa % 5;
            mantissa /= 5;
            int modDiv = 1;
            while (mantissa < MAX_VALUE_DIVIDE_5 && precision > 1) {
                precision -= 1;
                mantissa <<= 1;
                modDiv <<= 1;
            }
            mantissa += modDiv * mod / 5;
        }
        long val2 = precision > 0 ? mantissa << precision : mantissa >>> -precision;

        address = appendFixed(address, val2);
        for (int i = 0; i < digits; i++)
            MEMORY.writeByte(address++, (byte) '0');
        return address;
    }

    private static long appendFraction(long address, double d, int sign, long mantissa, int shift) {
        // fraction.
        UNSAFE.putShort(address, (short) ('0' + ('.' << 8)));
        address += 2;
        final int shift2 = 10;
        mantissa <<= shift2;
        long error = (1 << (shift2 - 1));
        mantissa += error;
        int precision = shift + shift2;

        long value = 0;
        int decimalPlaces = 0;
        while (mantissa > error) {
            while (mantissa > MAX_VALUE_DIVIDE_5) {
                mantissa = (mantissa + 1) >>> 1;
                error = ((error + 1) >>> 1);
                precision--;
            }
            // times 5*2 = 10
            mantissa *= 5;
            error *= 5;
            precision--;
            if (precision >= 64) {
                decimalPlaces++;
                MEMORY.writeByte(address++, (byte) '0');
                continue;
            }
            long num = (mantissa >>> precision);
            value = value * 10 + num;
            final char c = (char) ('0' + num);
            MEMORY.writeByte(address++, (byte) c);
            mantissa -= num << precision;
            ++decimalPlaces;
            final double parsedValue = asDouble(value, 0, sign != 0, decimalPlaces);
            if (parsedValue == d)
                break;
        }
        return address;
    }

    private static long appendIntegerAndFraction(long address, double d, int sign, long mantissa, int shift) {
        long intValue = mantissa >> shift;
        address = appendFixed(address, intValue);
        mantissa -= intValue << shift;
        if (mantissa > 0) {
            MEMORY.writeByte(address++, (byte) '.');
            mantissa <<= 1;
            mantissa++;
            int precision = shift + 1;
            long error = 1;

            long value = intValue;
            int decimalPlaces = 0;
            while (mantissa > error) {
                // times 5*2 = 10
                mantissa *= 5;
                error *= 5;
                precision--;
                long num = (mantissa >> precision);
                value = value * 10 + num;
                MEMORY.writeByte(address++, (byte) ('0' + num));
                mantissa -= num << precision;

                final double parsedValue = asDouble(value, 0, sign != 0, ++decimalPlaces);
                if (parsedValue == d)
                    break;
            }
        } else {
            UNSAFE.putShort(address, (short) ('.' + ('0' << 8)));
            address += 2;
        }
        return address;
    }

    private static long appendText(long address, String s) {
        for (int i = 0; i < s.length(); i++) {
            MEMORY.writeByte(address++, (byte) s.charAt(i));
        }
        return address;
    }

    private static double asDouble(long value, int exp, boolean negative, int deci) {
        int scale2 = 0;
        double d;
        if (deci >= 28) {
            d = value / Math.pow(5, deci);

        } else if (deci > 0) {
            scale2 = Long.numberOfLeadingZeros(value) - 1;
            value <<= scale2;
            long fives = Maths.fives(deci);

            // We have at most [63 - scale2] significant binary digits in "value".
            // 53 binary digits fit in mantissa without errors.
            // Thus, if [63 - scale2] <= 53, "value" casted to double will contain the exact value.
            if (scale2 >= 10)
                d = (double)value / fives;
            else {
                long whole = value / fives;
                long rem = value % fives;
                d = whole + (double) rem / fives;
            }

        } else if (deci <= -28) {
            d = value * Math.pow(5, -deci);

        } else if (deci < 0) {
            double fives = Maths.fives(-deci);
            d = value * fives;

        } else {
            d = value;
        }

        double scalb = Math.scalb(d, exp - deci - scale2);
        return negative ? -scalb : scalb;
    }

    public static long append8bit(long address, byte[] bytes) {
        final int len = bytes.length;
        int i;
        for (i = 0; i < len - 7; i += 8)
            MEMORY.writeLong(address + i, UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + (long) i));
        for (; i < len; i++)
            MEMORY.writeByte(address + i, UNSAFE.getByte(bytes, ARRAY_BYTE_BASE_OFFSET + (long) i));
        return address + len;
    }

    public static long append8bit(long address, char[] chars) {
        final int len = chars.length;
        int i;
        for (i = 0; i < len; i++)
            MEMORY.writeByte(address + i, (byte) chars[i]);
        return address + len;
    }
}
