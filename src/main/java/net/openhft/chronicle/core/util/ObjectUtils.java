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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by peter on 23/06/15.
 */
public enum ObjectUtils {
    ;


    public static <E> E convertTo(Class<E> eClass, Object o) throws ClassCastException {
        if (eClass.isInstance(o) || o == null) return (E) o;
        if (Enum.class.isAssignableFrom(eClass)) {
            return (E) Enum.valueOf((Class) eClass, o.toString());
        }
        if (o instanceof String) {
            try {
                Method valueOf = eClass.getDeclaredMethod("valueOf", String.class);
                valueOf.setAccessible(true);
                return (E) valueOf.invoke(null, o);
            } catch (InvocationTargetException | IllegalAccessException e) {
                ClassCastException cce = new ClassCastException();
                cce.initCause(e);
                throw cce;
            } catch (NoSuchMethodException e) {
            }
            try {
                Constructor<E> constructor = eClass.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(o);
            } catch (Exception e) {
                ClassCastException cce = new ClassCastException();
                cce.initCause(e);
                throw cce;
            }
        }
        if (Number.class.isAssignableFrom(eClass)) {
            return (E) convertToNumber(eClass, o);
        }
        if (ReadResolvable.class.isAssignableFrom(eClass))
            return (E) o;
        throw new ClassCastException("Unable to convert " + o.getClass() + " " + o + " to " + eClass);
    }

    private static Number convertToNumber(Class eClass, Object o) {
        if (o instanceof Number) {
            Number n = (Number) o;
            if (eClass == Double.class)
                return n.doubleValue();
            if (eClass == Long.class)
                return n.longValue();
            if (eClass == Integer.class)
                return n.intValue();
            if (eClass == Float.class)
                return n.floatValue();
            if (eClass == Short.class)
                return n.shortValue();
            if (eClass == Byte.class)
                return n.byteValue();
            if (eClass == BigDecimal.class)
                return n instanceof Long ? BigDecimal.valueOf(n.longValue()) : BigDecimal.valueOf(n.doubleValue());
            // TODO fix for large numbers.
            if (eClass == BigInteger.class)
                return new BigInteger(o.toString());
        } else {
            String s = o.toString();
            if (eClass == Double.class)
                return Double.parseDouble(s);
            if (eClass == Long.class)
                return Long.parseLong(s);
            if (eClass == Integer.class)
                return Integer.parseInt(s);
            if (eClass == Float.class)
                return Float.parseFloat(s);
            if (eClass == Short.class)
                return Short.parseShort(s);
            if (eClass == Byte.class)
                return Byte.parseByte(s);
            if (eClass == BigDecimal.class)
                return new BigDecimal(s);
            // TODO fix for large numbers.
            if (eClass == BigInteger.class)
                return new BigInteger(s);
        }
        throw new UnsupportedOperationException("Cannot convert " + o.getClass() + " to " + eClass);
    }
}
