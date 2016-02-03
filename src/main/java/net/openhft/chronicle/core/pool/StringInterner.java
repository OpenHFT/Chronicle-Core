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

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * @author peter.lawrey
 */
public class StringInterner {
    protected final String[] interner;
    protected final int mask, shift;
    protected boolean toggle = false;

    public StringInterner(int capacity) throws IllegalArgumentException {
        int n = Maths.nextPower2(capacity, 128);
        shift = Maths.intLog2(n);
        interner = new String[n];
        mask = n - 1;
    }

    @Nullable
    public String intern(@Nullable CharSequence cs) {
        if (cs == null)
            return null;
        if (cs.length() > interner.length)
            return cs.toString();
        int hash = Maths.hash32(cs);
        int h = hash & mask;
        String s = interner[h];
        if (StringUtils.isEqual(s, cs))
            return s;
        int h2 = (hash >> shift) & mask;
        String s2 = interner[h2];
        if (StringUtils.isEqual(s2, cs))
            return s2;
        String s3 = cs.toString();
        return interner[s == null || (s2 != null && toggle()) ? h : h2] = s3;
    }

    protected boolean toggle() {
        return toggle = !toggle;
    }

    public int valueCount() {
        return (int) Stream.of(interner).filter(s -> s != null).count();
    }
}
