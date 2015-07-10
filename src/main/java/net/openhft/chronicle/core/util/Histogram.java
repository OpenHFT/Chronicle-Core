/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core.util;

/**
 * Created by peter on 10/07/15.
 */
public class Histogram {
    private final int fractionBits;
    private long totalCount, underRange, overRange;
    private int[] sampleCount;
    private long floor;

    public Histogram(int powersOf2, int fractionBits) {
        this.fractionBits = fractionBits;
        sampleCount = new int[powersOf2 << fractionBits];
        floor = Double.doubleToRawLongBits(1) >> (52 - fractionBits);
    }

    public int sample(double time) {
        int bucket = (int) ((Double.doubleToRawLongBits(time) >> (52 - fractionBits)) - floor);
        if (bucket < 0)
            underRange++;
        else if (bucket >= sampleCount.length)
            overRange++;
        else
            sampleCount[bucket]++;
        totalCount++;
        return bucket;
    }

    public double percentile(double fraction) {
        long value = (long) (totalCount * (1 - fraction));
        value -= overRange;
        if (value < 0)
            return Double.POSITIVE_INFINITY;
        for (int i = sampleCount.length - 1; i >= 0; i--) {
            value -= sampleCount[i];
            if (value < 0) {
                long bits = ((((i + floor) << 1) + 1) << (51 - fractionBits));
                return Double.longBitsToDouble(bits);
            }
        }
        return 1;
    }

    public String toMicrosFormat() {
        return String.format("50/90 99/99.9 99.99/99.999 - worst was %.2f/%.2f %.1f/%.1f %,d/%,d - %,d",
                percentile(0.5) / 1e3, percentile(0.9) / 1e3,
                percentile(0.99) / 1e3, percentile(0.999) / 1e3,
                (long) percentile(0.9999) / 1000, (long) percentile(0.99999) / 1000,
                (long) percentile(1) / 1000);
    }
}
