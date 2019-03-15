package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Maths;

import java.nio.BufferOverflowException;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public enum UnsafeText {
    ;

    private static final long MAX_VALUE_DIVIDE_5 = Long.MAX_VALUE / 5;

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

    public static long appendDouble(long address, double d)
            throws BufferOverflowException, IllegalArgumentException {
        long val = Double.doubleToRawLongBits(d);
        int sign = (int) (val >>> 63);
        int exp = (int) ((val >>> 52) & 2047);
        long mantissa = val & ((1L << 52) - 1);
        if (sign != 0) {
            UNSAFE.putByte(address++, (byte) '-');
        }
        if (exp == 0 && mantissa == 0) {
            UNSAFE.putByte(address++, (byte) '0');
            UNSAFE.putByte(address++, (byte) '.');
            UNSAFE.putByte(address++, (byte) '0');
            return address;

        } else if (exp == 2047) {
            if (mantissa == 0) {
                return appendText(address, "Infinity");

            } else {
                return appendText(address, "NaN");
            }

        } else if (exp > 0) {
            mantissa += 1L << 52;
        }
        final int shift = (1023 + 52) - exp;
        if (shift > 0) {
            // integer and faction
            if (shift < 53) {
                long intValue = mantissa >> shift;
                address = appendBase10(address, intValue);
                mantissa -= intValue << shift;
                if (mantissa > 0) {
                    UNSAFE.putByte(address++, (byte) '.');
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
                        UNSAFE.putByte(address++, (byte) ('0' + num));
                        mantissa -= num << precision;

                        final double parsedValue = asDouble(value, 0, sign != 0, ++decimalPlaces);
                        if (parsedValue == d)
                            break;
                    }
                } else {
                    UNSAFE.putByte(address++, (byte) '.');
                    UNSAFE.putByte(address++, (byte) '0');
                }
                return address;

            } else {
                // faction.
                UNSAFE.putByte(address++, (byte) '0');
                UNSAFE.putByte(address++, (byte) '.');
                mantissa <<= 6;
                mantissa += (1 << 5);
                int precision = shift + 6;

                long error = (1 << 5);

                long value = 0;
                int decimalPlaces = 0;
                while (mantissa > error) {
                    while (mantissa > MAX_VALUE_DIVIDE_5) {
                        mantissa >>>= 1;
                        error = (error + 1) >>> 1;
                        precision--;
                    }
                    // times 5*2 = 10
                    mantissa *= 5;
                    error *= 5;
                    precision--;
                    if (precision >= 64) {
                        decimalPlaces++;
                        UNSAFE.putByte(address++, (byte) '0');
                        continue;
                    }
                    long num = (mantissa >>> precision);
                    value = value * 10 + num;
                    final char c = (char) ('0' + num);
                    assert !(c < '0' || c > '9');
                    UNSAFE.putByte(address++, (byte) c);
                    mantissa -= num << precision;
                    final double parsedValue = asDouble(value, 0, sign != 0, ++decimalPlaces);
                    if (parsedValue == d)
                        break;
                }
                return address;
            }
        }
        // large number
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

        address = appendBase10(address, val2);
        for (int i = 0; i < digits; i++)
            UNSAFE.putByte(address++, (byte) '0');
        return address;
    }

    private static long appendText(long address, String infinity) {
        throw new UnsupportedOperationException();
    }

    private static double asDouble(long value, int exp, boolean negative, int deci) {
        // these numbers were determined empirically.
        int leading = 11;
        if (value >= 1L << 53)
            leading = Long.numberOfLeadingZeros(value) - 1;
        else if (value >= 1L << 49)
            leading = 10;

        int scale2 = 0;
        if (leading > 0) {
            scale2 = leading;
            value <<= scale2;
        }
        double d;
        if (deci > 29) {
            d = value / Math.pow(5, -deci);

        } else if (deci > 0) {
            long fives = Maths.fives(deci);
            long whole = value / fives;
            long rem = value % fives;
            d = whole + (double) rem / fives;

        } else if (deci < -29) {
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
}
