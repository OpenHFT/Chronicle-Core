/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.ClassLocal;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.pool.ClassAliasPool;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created by peter on 23/06/15.
 */
public enum ObjectUtils {
    ;

    public static final Object[] NO_OBJECTS = {};
    static final Map<Class, Class> primMap = new LinkedHashMap<Class, Class>() {{
        put(boolean.class, Boolean.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(short.class, Short.class);
        put(int.class, Integer.class);
        put(float.class, Float.class);
        put(long.class, Long.class);
        put(double.class, Double.class);
        put(void.class, Void.class);
    }};
    static final Map<Class, Object> DEFAULT_MAP = new HashMap<>();
    static final ClassLocal<ThrowingFunction<String, Object, Exception>> PARSER_CL = ClassLocal.withInitial(c -> {
        if (c == Class.class)
            return ClassAliasPool.CLASS_ALIASES::forName;

        try {
            Method valueOf = c.getDeclaredMethod("valueOf", String.class);
            valueOf.setAccessible(true);
            return s -> valueOf.invoke(null, s);
        } catch (NoSuchMethodException e) {
            // ignored
        }

        try {
            Method valueOf = c.getDeclaredMethod("parse", CharSequence.class);
            valueOf.setAccessible(true);
            return s -> valueOf.invoke(null, s);

        } catch (NoSuchMethodException e) {
            // ignored
        }
        try {
            Method valueOf = c.getDeclaredMethod("fromString", String.class);
            valueOf.setAccessible(true);
            return s -> valueOf.invoke(null, s);

        } catch (NoSuchMethodException e) {
            // ignored
        }
        try {
            Constructor constructor = c.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return s -> constructor.newInstance(s);
        } catch (Exception e) {
            throw asCCE(e);
        }
    });
    private static final ClassLocal<Supplier> SUPPLIER_CLASS_LOCAL = ClassLocal.withInitial(c -> {
        if (c == null)
            throw new NullPointerException();
        if (c.isPrimitive())
            throw new IllegalArgumentException("primitive: " + c.getName());
        if (c.isInterface())
            throw new IllegalArgumentException("interface: " + c.getName());
        try {
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return ThrowingSupplier.asSupplier((ThrowingSupplier<Object, ReflectiveOperationException>) constructor::newInstance);

        } catch (Exception e) {
            return () -> {
                try {
                    return OS.memory().allocateInstance(c);
                } catch (InstantiationException e1) {
                    throw Jvm.rethrow(e1);
                }
            };
        }
    });

    static {
        DEFAULT_MAP.put(boolean.class, false);
        DEFAULT_MAP.put(byte.class, (byte) 0);
        DEFAULT_MAP.put(short.class, (short) 0);
        DEFAULT_MAP.put(char.class, (char) 0);
        DEFAULT_MAP.put(int.class, 0);
        DEFAULT_MAP.put(long.class, 0L);
        DEFAULT_MAP.put(float.class, 0.0f);
        DEFAULT_MAP.put(double.class, 0.0);
    }

    /**
     * If the class is a primitive type, change it to the equivalent wrapper.
     *
     * @param eClass to check
     * @return the wrapper class if eClass is a primitive type, or the eClass if not.
     */
    public static Class primToWrapper(Class eClass) {
        Class clazz0 = primMap.get(eClass);
        if (clazz0 != null)
            eClass = clazz0;
        return eClass;
    }

    public static <E> E convertTo(Class<E> eClass, Object o)
            throws ClassCastException, IllegalArgumentException {
        // shorter path.
        return eClass == null || o == null || eClass.isInstance(o)
                ? (E) o
                : convertTo0(eClass, o);
    }

    static <E> E convertTo0(Class<E> eClass, Object o)
            throws ClassCastException, IllegalArgumentException {
        eClass = primToWrapper(eClass);
        if (eClass.isInstance(o) || o == null) return (E) o;
        if (eClass == Void.class) return null;
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
                return (E) PARSER_CL.get(eClass).apply(s);

            } catch (Exception e) {
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
        if (Set.class.isAssignableFrom(eClass)) {
            return (E) new LinkedHashSet((Collection) o);
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
        Object array = Array.newInstance(eClass.getComponentType(), len);
        Iterator iter = iteratorFor(o);
        Class elementType = elementType(eClass);
        for (int i = 0; i < len; i++) {
            Object value = convertTo(elementType, iter.next());
            Array.set(array, i, value);
        }
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

    public static <T> T[] addAll(T first, T... additional) {
        T[] interfaces;
        if (additional.length == 0) {
            interfaces = (T[]) Array.newInstance(first.getClass(), 1);
            interfaces[0] = first;
        } else {
            List<T> objs = new ArrayList<>();
            objs.add(first);
            Collections.addAll(objs, additional);
            interfaces = objs.toArray((T[]) Array.newInstance(first.getClass(), objs.size()));
        }
        return interfaces;
    }

    public static <T> T printAll(Class<T> tClass, Class... additional) {
        return onMethodCall((method, args) -> {
            String argsStr = args == null ? "()" : Arrays.toString(args);
            System.out.println(method.getName() + " " + argsStr);
            return defaultValue(method.getReturnType());
        }, tClass, additional);
    }

    public static Object defaultValue(Class<?> type) {
        return DEFAULT_MAP.get(type);
    }

    public static <T> T onMethodCall(BiFunction<Method, Object[], Object> biFunction, Class<T> tClass, Class... additional) {
        Class[] interfaces = addAll(tClass, additional);
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }
                return biFunction.apply(method, args);
            }
        });
    }

    public static Class getTypeFor(Class clazz, Class interfaceClass) {
        return getTypeFor(clazz, interfaceClass, 0);
    }

    public static Class getTypeFor(Class clazz, Class interfaceClass, int index) {
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) type;
                if (interfaceClass.isAssignableFrom((Class<?>) ptype.getRawType())) {
                    Type type0 = ptype.getActualTypeArguments()[index];
                    if (type0 instanceof Class)
                        return (Class) type0;
                    throw new IllegalArgumentException("The match super interface for " + clazz + " was not a concrete class, was " + ptype);
                }
            }
        }
        throw new IllegalArgumentException("No matching super interface for " + clazz + " which was a " + interfaceClass);
    }
}
