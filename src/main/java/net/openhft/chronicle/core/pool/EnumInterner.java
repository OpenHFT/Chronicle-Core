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

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.Maths;
import net.openhft.chronicle.core.util.StringUtils;

/**
 * @author peter.lawrey
 */
public class EnumInterner<E extends Enum<E>> {
    public static final ClassLocal<EnumInterner> ENUM_INTERNER =
            ClassLocal.withInitial(c -> new EnumInterner<>(c));
    private final E[] interner;
    private final int mask;
    private final Class<E> eClass;

    public EnumInterner(Class<E> eClass) {
        this(eClass, 64);
    }

    public EnumInterner(Class<E> eClass, int capacity) {
        this.eClass = eClass;
        int n = Maths.nextPower2(capacity, 16);
        interner = (E[]) new Enum[n];
        mask = n - 1;
    }

    public E intern(CharSequence cs) {
        int h = Maths.hash32(cs) & mask;
        E e = interner[h];
        if (e != null && StringUtils.isEqual(e.name(), cs))
            return e;
        String s2 = cs.toString();
        return interner[h] = Enum.valueOf(eClass, s2);
    }
}
