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

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by peter on 23/06/15.
 */
public enum ObjectUtils {
    ;

    static final ClassLocal<Supplier> SUPPLIER_CLASS_LOCAL = ClassLocal.withInitial(c -> {
        try {
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return () -> {
                try {
                    return constructor.newInstance();
                } catch (Exception e) {
                    throw Jvm.rethrow(e);
                }
            };
        } catch (Exception e) {
            return () -> OS.memory().allocateInstance(c);
        }
    });

    public static <E> E convertTo(Class<E> eClass, Object o)
            throws ClassCastException, IllegalArgumentException {
        if (eClass.isInstance(o) || o == null) return (E) o;
        if (Enum.class.isAssignableFrom(eClass)) {
            return (E) Enum.valueOf((Class) eClass, o.toString());
        }
        if (o instanceof CharSequence) {
            CharSequence cs = (CharSequence) o;
            if (Character.class.equals(eClass)) {
                if (cs.length() > 0)
                    return (E) (Character) cs.charAt(0);
                else
                    return null;
            }
            String s = cs.toString();
            if (eClass == String.class)
                return (E) s;

            try {
                Method valueOf = eClass.getDeclaredMethod("valueOf", String.class);
                valueOf.setAccessible(true);
                return (E) valueOf.invoke(null, s);

            } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
                throw asCCE(e);
            } catch (NoSuchMethodException e) {
            }
            try {
                Constructor<E> constructor = eClass.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(s);
            } catch (Exception e) {
                if (s.length() == 0) {
                    try {
                        return newInstance(eClass);
                    } catch (Exception e2) {
                        throw asCCE(e);
                    }
                }

                throw asCCE(e);
            }
        }
        if (Number.class.isAssignableFrom(eClass)) {
            return (E) convertToNumber(eClass, o);
        }
        if (ReadResolvable.class.isAssignableFrom(eClass))
            return (E) o;
        if (Object[].class.isAssignableFrom(eClass)) {
            return convertToArray(eClass, o);
        }
//        if (Collection.class.isAssignableFrom(eClass)) {
//            return convertCollection(eClass, o);
//        }
        throw new ClassCastException("Unable to convert " + o.getClass() + " " + o + " to " + eClass);
    }

    public static ClassCastException asCCE(Exception e) {
        ClassCastException cce = new ClassCastException();
        cce.initCause(e);
        return cce;
    }

    private static <E> E convertToArray(Class<E> eClass, Object o)
            throws IllegalArgumentException {
        int len = sizeOf(o);
        Object array = Array.newInstance(eClass, len);
        Iterator iter = iteratorFor(o);
        Class elementType = elementType(eClass);
        for (int i = 0; i < len; i++)
            Array.set(array, i, convertTo(elementType, iter.next()));
        return (E) array;
    }

    private static <E> Class elementType(Class<E> eClass) {
        if (Object[].class.isAssignableFrom(eClass))
            return eClass.getComponentType();
        return Object.class;
    }

    private static Iterator iteratorFor(Object o) {
        if (o instanceof Iterable) {
            return ((Iterable) o).iterator();
        }
        if (o instanceof Object[]) {
            return Arrays.asList((Object[]) o).iterator();
        }
        throw new UnsupportedOperationException();
    }

    private static int sizeOf(Object o) throws IllegalArgumentException {
        if (o instanceof Collection)
            return ((Collection) o).size();
        if (o instanceof Map)
            return ((Map) o).size();
        if (o.getClass().isArray())
            return Array.getLength(o);
        throw new UnsupportedOperationException();
    }

    private static Number convertToNumber(Class eClass, Object o)
            throws NumberFormatException {
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

    public static <T> T newInstance(Class<T> clazz) {
        Supplier cons = SUPPLIER_CLASS_LOCAL.get(clazz);
        return (T) cons.get();
    }
}
