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
import net.openhft.chronicle.core.pool.EnumCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static net.openhft.chronicle.core.util.ObjectUtils.Immutability.MAYBE;
import static net.openhft.chronicle.core.util.ObjectUtils.Immutability.NO;

/*
 * Created by Peter Lawrey on 23/06/15.
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
    static final ClassLocal<ThrowingFunction<String, Object, Exception>> PARSER_CL = ClassLocal.withInitial(new ConversionFunction());
    static final ClassLocal<Map<String, Enum>> CASE_IGNORE_LOOKUP = ClassLocal.withInitial(ObjectUtils::caseIgnoreLookup);
    static final ClassValue<Method> READ_RESOLVE = ClassLocal.withInitial(c -> {
        try {
            Method m = c.getDeclaredMethod("readResolve");
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException expected) {
            return null;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    });
    private static final Map<Class, Immutability> immutabilityMap = new ConcurrentHashMap<>();
    private static final ClassLocal<Supplier> SUPPLIER_CLASS_LOCAL = ClassLocal.withInitial(c -> {
        if (c == null)
            throw new NullPointerException();
        if (c.isPrimitive())
            throw new IllegalArgumentException("primitive: " + c.getName());
        if (c.isInterface())
            throw new IllegalArgumentException("interface: " + c.getName());
        if (Modifier.isAbstract(c.getModifiers()))
            throw new IllegalArgumentException("abstract class: " + c.getName());
        if (c.isEnum())
            throw new IllegalArgumentException("enum class: " + c.getName());
        try {
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return ThrowingSupplier.asSupplier(constructor::newInstance);

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

    public static void immutabile(Class clazz, boolean isImmutable) {
        immutabilityMap.put(clazz, isImmutable ? Immutability.YES : Immutability.NO);
    }

    public static Immutability isImmutable(@NotNull Class clazz) {
        Immutability immutability = immutabilityMap.get(clazz);
        if (immutability == null)
            return Comparable.class.isAssignableFrom(clazz) ? MAYBE : NO;
        return immutability;
    }

    public static boolean isTrue(CharSequence s) {
        switch (s.length()) {
            case 1:
                char ch = Character.toLowerCase(s.charAt(0));
                return ch == 't' || ch == 'y';
            case 3:
                return StringUtils.equalsCaseIgnore(s, "yes");
            case 4:
                return StringUtils.equalsCaseIgnore(s, "true");
            default:
                return false;
        }
    }

    public static boolean isFalse(CharSequence s) {
        switch (s.length()) {
            case 1:
                char ch = Character.toLowerCase(s.charAt(0));
                return ch == 'f' || ch == 'n';
            case 2:
                return StringUtils.equalsCaseIgnore(s, "no");
            case 5:
                return StringUtils.equalsCaseIgnore(s, "false");
            default:
                return false;
        }
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

    @Nullable
    public static <E> E convertTo(@Nullable Class<E> eClass, @Nullable Object o)
            throws ClassCastException, IllegalArgumentException {
        // shorter path.
        return eClass == null || o == null || eClass.isInstance(o)
                ? (E) o
                : convertTo0(eClass, o);
    }

    @NotNull
    private static Map<String, Enum> caseIgnoreLookup(@NotNull Class c) {
        @NotNull Map<String, Enum> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Object o : c.getEnumConstants()) {
            @NotNull Enum e = (Enum) o;
            map.put(e.name().toUpperCase(), e);
        }
        return map;
    }

    @NotNull
    public static <E extends Enum<E>> E valueOfIgnoreCase(@NotNull Class<E> eClass, @NotNull String name) {
        final Map<String, Enum> map = CASE_IGNORE_LOOKUP.get(eClass);
        if (name.startsWith("{") && name.endsWith("}"))
            return getSingletonForEnum(eClass);
        @NotNull final E anEnum = (E) map.get(name);
        return anEnum == null ? EnumCache.of(eClass).valueOf(name) : anEnum;
    }

    public static <E extends Enum<E>> E getSingletonForEnum(Class<E> eClass) {
        E[] enumConstants = eClass.getEnumConstants();
        if (enumConstants.length == 0)
            throw new IllegalStateException("Cannot convert marshallable to " + eClass + " as it doesn't have any instances");
        if (enumConstants.length > 1)
            Jvm.warn().on(ObjectUtils.class, eClass + " has multiple INSTANCEs, picking the first one");
        return enumConstants[0];
    }

    static <E> E convertTo0(Class<E> eClass, @Nullable Object o)
            throws ClassCastException, IllegalArgumentException {
        eClass = primToWrapper(eClass);
        if (eClass.isInstance(o) || o == null) return (E) o;
        if (eClass == Void.class) return null;
        if (eClass == String.class) return (E) o.toString();
        if (Enum.class.isAssignableFrom(eClass)) {
            return (E) valueOfIgnoreCase((Class) eClass, o.toString());
        }
        if (o instanceof CharSequence) {
            @Nullable CharSequence cs = (CharSequence) o;
            if (Character.class.equals(eClass)) {
                if (cs.length() > 0)
                    return (E) (Character) cs.charAt(0);
                else
                    return null;
            }
            @NotNull String s = cs.toString();
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
        if (Character.class == eClass) {
            String s = o.toString();
            if (s.length() == 1)
                return (E) (Character) s.charAt(0);
            if (s.isEmpty())
                return (E) Character.valueOf((char) 0);
        }
        if (CharSequence.class.isAssignableFrom(eClass)) {
            try {
                return (E) PARSER_CL.get(eClass).apply(o.toString());

            } catch (Exception e) {
                throw asCCE(e);
            }
        }
        throw new ClassCastException("Unable to convert " + o.getClass() + " " + o + " to " + eClass);
    }

    @NotNull
    public static ClassCastException asCCE(Exception e) {
        @NotNull ClassCastException cce = new ClassCastException();
        cce.initCause(e);
        return cce;
    }

    @NotNull
    private static <E> E convertToArray(@NotNull Class<E> eClass, Object o)
            throws IllegalArgumentException {
        int len = sizeOf(o);
        Object array = Array.newInstance(eClass.getComponentType(), len);
        Iterator iter = iteratorFor(o);
        Class elementType = elementType(eClass);
        for (int i = 0; i < len; i++) {
            @Nullable Object value = convertTo(elementType, iter.next());
            Array.set(array, i, value);
        }
        return (E) array;
    }

    private static <E> Class elementType(@NotNull Class<E> eClass) {
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
            @NotNull Number n = (Number) o;
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

    @NotNull
    public static <T> T newInstance(@NotNull Class<T> clazz) {
        Supplier cons = SUPPLIER_CLASS_LOCAL.get(clazz);
        return (T) cons.get();
    }

    public static <T> T[] addAll(@NotNull T first, @NotNull T... additional) {
        T[] interfaces;
        if (additional.length == 0) {
            interfaces = (T[]) Array.newInstance(first.getClass(), 1);
            interfaces[0] = first;
        } else {
            @NotNull List<T> objs = new ArrayList<>();
            objs.add(first);
            Collections.addAll(objs, additional);
            interfaces = objs.toArray((T[]) Array.newInstance(first.getClass(), objs.size()));
        }
        return interfaces;
    }

    public static boolean matchingClass(@NotNull Class base, @NotNull Class toMatch) {
        return base == toMatch ||
                Enum.class.isAssignableFrom(toMatch) && base.equals(toMatch.getEnclosingClass());
    }

    @NotNull
    public static <T> T printAll(@NotNull Class<T> tClass, Class... additional) throws IllegalArgumentException {
        return onMethodCall((method, args) -> {
            @NotNull String argsStr = args == null ? "()" : Arrays.toString(args);
            System.out.println(method.getName() + " " + argsStr);
            return defaultValue(method.getReturnType());
        }, tClass, additional);
    }

    public static Object defaultValue(Class<?> type) {
        return DEFAULT_MAP.get(type);
    }

    @NotNull
    public static <T> T onMethodCall(@NotNull BiFunction<Method, Object[], Object> biFunction, @NotNull Class<T> tClass, Class... additional) throws IllegalArgumentException {
        Class[] interfaces = addAll(tClass, additional);
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }
                return biFunction.apply(method, args);
            }
        });
    }

    @NotNull
    public static Class getTypeFor(@NotNull Class clazz, @NotNull Class interfaceClass) {
        return getTypeFor(clazz, interfaceClass, 0);
    }

    @NotNull
    public static Class getTypeFor(@NotNull Class clazz, @NotNull Class interfaceClass, int index) {
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                @NotNull ParameterizedType ptype = (ParameterizedType) type;
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

    public static boolean isConcreteClass(@NotNull Class tClass) {
        return (tClass.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) == 0;
    }

    public static Object readResolve(@NotNull Object o) {
        Method readResove = READ_RESOLVE.get(o.getClass());
        if (readResove == null)
            return o;
        try {
            return readResove.invoke(o);
        } catch (IllegalAccessException e) {
            throw Jvm.rethrow(e);
        } catch (InvocationTargetException e) {
            throw Jvm.rethrow(e.getCause());
        }
    }

    public enum Immutability {
        YES, NO, MAYBE
    }

    private static Boolean toBoolean(String s) {
        if (s == null)
            return null;
        s = s.trim();
        if (isTrue(s))
            return Boolean.TRUE;
        if (isFalse(s))
            return Boolean.FALSE;
        if (s.isEmpty())
            return null;
        Jvm.debug().on(ObjectUtils.class, "Treating '" + s + "' as false");
        return Boolean.FALSE;
    }

    private static class ConversionFunction implements Function<Class<?>, ThrowingFunction<String, Object, Exception>> {
        @Override
        public ThrowingFunction<String, Object, Exception> apply(@NotNull Class<?> c) {
            if (c == Class.class)
                return CLASS_ALIASES::forName;
            if (c == Boolean.class)
                return ObjectUtils::toBoolean;
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
                return constructor::newInstance;
            } catch (Exception e) {
                throw asCCE(e);
            }
        }
    }

    public static Class<?>[] getAllInterfaces(Object o) {
        Set<Class<?>> results = new HashSet<>();
        getAllInterfaces(o, results::add);
        return results.toArray(new Class<?>[results.size()]);
    }

    public static void getAllInterfaces(Object o, Function<Class<?>, Boolean> accumulator) {
        if (null == o)
            return;

        if(null == accumulator)
            throw new IllegalArgumentException("Accumulator cannot be null");

        if(o instanceof Class){
            Class clazz = (Class) o;

            if(clazz.isInterface()) {
                if(accumulator.apply((Class) o))
                {
                    for (Class aClass : clazz.getInterfaces()) {
                        getAllInterfaces(aClass, accumulator);
                    }
                }
            } else {
                if(null != clazz.getSuperclass())
                    getAllInterfaces(clazz.getSuperclass(), accumulator);

                for (Class aClass : clazz.getInterfaces()) {
                    getAllInterfaces(aClass, accumulator);
                }
            }
        } else {
            getAllInterfaces(o.getClass(), accumulator);
        }
    }
}
