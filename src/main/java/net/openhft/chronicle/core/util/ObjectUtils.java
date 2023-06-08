/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
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

import static net.openhft.chronicle.core.internal.util.MapUtil.entry;
import static net.openhft.chronicle.core.internal.util.MapUtil.ofUnmodifiable;
import static net.openhft.chronicle.core.pool.ClassAliasPool.CLASS_ALIASES;
import static net.openhft.chronicle.core.util.ObjectUtils.Immutability.MAYBE;
import static net.openhft.chronicle.core.util.ObjectUtils.Immutability.NO;

public final class ObjectUtils {

    // Suppresses default constructor, ensuring non-instantiability.
    private ObjectUtils() {
    }

    static final Map<Class<?>, Class<?>> PRIM_MAP = ofUnmodifiable(
            entry(boolean.class, Boolean.class),
            entry(byte.class, Byte.class),
            entry(char.class, Character.class),
            entry(short.class, Short.class),
            entry(int.class, Integer.class),
            entry(float.class, Float.class),
            entry(long.class, Long.class),
            entry(double.class, Double.class),
            entry(void.class, Void.class)
    );

    static final Map<Class<?>, Object> DEFAULT_MAP = ofUnmodifiable(
            entry(boolean.class, false),
            entry(byte.class, (byte) 0),
            entry(short.class, (short) 0),
            entry(char.class, (char) 0),
            entry(int.class, 0),
            entry(long.class, 0L),
            entry(float.class, 0.0f),
            entry(double.class, 0.0d)
    );

    static final ClassLocal<ThrowingFunction<String, Object, Exception>> PARSER_CL = ClassLocal.withInitial(new ConversionFunction());
    static final ClassLocal<Map<String, Enum<?>>> CASE_IGNORE_LOOKUP = ClassLocal.withInitial(ObjectUtils::caseIgnoreLookup);
    static final ClassValue<Method> READ_RESOLVE = ClassLocal.withInitial(c -> {
        try {
            Method m = c.getDeclaredMethod("readResolve");
            Jvm.setAccessible(m);
            return m;
        } catch (NoSuchMethodException expected) {
            return null;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    });
    private static final Map<Class<?>, Immutability> IMMUTABILITY_MAP = new ConcurrentHashMap<>();

    // these should only ever be changed on startup.
    private static volatile ClassLocal<Class<?>> interfaceToDefaultClass = ClassLocal.withInitial(ObjectUtils::lookForImplEnum);
    private static volatile ClassLocal<Supplier<?>> supplierClassLocal = ClassLocal.withInitial(ObjectUtils::supplierForClass);

    private static Supplier<?> supplierForClass(Class<?> c) {
        if (c == null)
            throw new NullPointerException();
        Package pkg = c.getPackage();
        if (pkg != null) {
            String name = pkg.getName();
            if ((name.startsWith("com.sun.") || name.startsWith("java")) && name.contains(".internal"))
                return () -> {
                    throw new IllegalArgumentException("Cannot create objects in JVM internal packages");
                };
        }
        if (c.isPrimitive())
            Jvm.rethrow(new IllegalArgumentException("primitive: " + c.getName()));
        if (c.isInterface()) {
            return () -> {
                Class<?> aClass = ObjectUtils.interfaceToDefaultClass.get(c);
                if (aClass == null || aClass == c)
                    Jvm.rethrow(new IllegalArgumentException("interface: " + c.getName()));
                return supplierForClass(aClass);
            };
        }
        if (c.isEnum())
            return () -> {
                try {
                    return OS.memory().allocateInstance(c);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            };
        if (Modifier.isAbstract(c.getModifiers()))
            Jvm.rethrow(new IllegalArgumentException("abstract class: " + c.getName()));
        try {
            Constructor<?> constructor = c.getDeclaredConstructor();
            Jvm.setAccessible(constructor);
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
    }

    public static void immutabile(final Class<?> clazz, final boolean isImmutable) {
        IMMUTABILITY_MAP.put(clazz, isImmutable ? Immutability.YES : Immutability.NO);
    }

    public static Immutability isImmutable(@NotNull final Class<?> clazz) {
        final Immutability immutability = IMMUTABILITY_MAP.get(clazz);
        if (immutability == null)
            return Comparable.class.isAssignableFrom(clazz) ? MAYBE : NO;
        return immutability;
    }

    public static boolean isTrue(CharSequence s) {
        if (s == null)
            return false;
        switch (s.length()) {
            case 1:
                try {
                    char ch = Character.toLowerCase(s.charAt(0));
                    return ch == 't' || ch == 'y';
                } catch (IndexOutOfBoundsException e) {
                    throw new AssertionError(e);
                }
            case 3:
                return equalsCaseIgnore(s, "yes");
            case 4:
                return equalsCaseIgnore(s, "true");
            default:
                return false;
        }
    }

    public static boolean isFalse(CharSequence s) {
        if (s == null)
            return false;
        switch (s.length()) {
            case 1:
                try {
                    char ch = Character.toLowerCase(s.charAt(0));
                    return ch == 'f' || ch == 'n';
                } catch (IndexOutOfBoundsException e) {
                    throw new AssertionError(e);
                }
            case 2:
                return equalsCaseIgnore(s, "no");
            case 5:
                return equalsCaseIgnore(s, "false");
            default:
                return false;
        }
    }

    private static boolean equalsCaseIgnore(CharSequence cs, String s) {
        if (cs instanceof String)
            return ((String) cs).equalsIgnoreCase(s);
        return StringUtils.equalsCaseIgnore(cs, s);
    }

    /**
     * If the class is a primitive type, change it to the equivalent wrapper.
     *
     * @param eClass to check
     * @return the wrapper class if eClass is a primitive type, or the eClass if not.
     */
    public static Class primToWrapper(Class<?> eClass) {
        final Class<?> clazz0 = PRIM_MAP.get(eClass);
        if (clazz0 != null)
            eClass = clazz0;
        return eClass;
    }

    @Nullable
    public static <E> E convertTo(@Nullable Class<E> eClass, @Nullable Object o) throws ClassCastException, IllegalArgumentException {
        // shorter path.
        return eClass == null || o == null || eClass.isInstance(o)
                ? (E) o
                : convertTo0(eClass, o);
    }

    @NotNull
    private static Map<String, Enum<?>> caseIgnoreLookup(@NotNull Class<?> c) {
        @NotNull Map<String, Enum<?>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Object o : c.getEnumConstants()) {
            @NotNull Enum<?> e = (Enum<?>) o;
            map.put(e.name().toUpperCase(), e);
        }
        return map;
    }

    @NotNull
    public static <E extends Enum<E>> E valueOfIgnoreCase(@NotNull Class<E> eClass, @NotNull String name) {
        final Map<String, Enum<?>> map = CASE_IGNORE_LOOKUP.get(eClass);
        if (name.startsWith("{") && name.endsWith("}"))
            return getSingletonForEnum(eClass);
        @SuppressWarnings("unchecked")
        @NotNull final E anEnum = (E) map.get(name);
        return anEnum == null ? EnumCache.of(eClass).valueOf(name) : anEnum;
    }

    public static <E extends Enum<E>> E getSingletonForEnum(Class<E> eClass) {
        E[] enumConstants = eClass.getEnumConstants();
        if (enumConstants.length == 0)
            throw new AssertionError("Cannot convert marshallable to " + eClass + " as it doesn't have any instances");
        if (enumConstants.length > 1)
            Jvm.warn().on(ObjectUtils.class, eClass + " has multiple INSTANCEs, picking the first one");
        return enumConstants[0];
    }

    // throws ClassCastException, IllegalArgumentException
    static <E> E convertTo0(Class<E> eClass, @Nullable Object o) throws NumberFormatException {
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
                    try {
                        return (E) (Character) cs.charAt(0);
                    } catch (IndexOutOfBoundsException e) {
                        throw new AssertionError(e);
                    }
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
            return (E) new LinkedHashSet<>((Collection) o);
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
        if (Date.class == eClass && o instanceof Long) {
            return (E) new Date((Long) o);
        }
        throw new ClassCastException("Unable to convert " + o.getClass() + " " + o + " to " + eClass);
    }

    @NotNull
    public static ClassCastException asCCE(Exception e) {
        @NotNull ClassCastException cce = new ClassCastException();
        cce.initCause(e);
        return cce;
    }

    // throws IllegalArgumentException
    @NotNull
    private static <E> E convertToArray(@NotNull Class<E> eClass, Object o) {
        final int len = sizeOf(o);
        final Object array = Array.newInstance(eClass.getComponentType(), len);
        final Iterator<?> iter = iteratorFor(o);
        final Class<?> elementType = elementType(eClass);
        try {
            for (int i = 0; i < len; i++) {
                @Nullable Object value = convertTo(elementType, iter.next());
                Array.set(array, i, value);
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
        return (E) array;
    }

    private static <E> Class<?> elementType(@NotNull Class<E> eClass) {
        if (Object[].class.isAssignableFrom(eClass))
            return eClass.getComponentType();
        return Object.class;
    }

    private static Iterator<?> iteratorFor(Object o) {
        if (o instanceof Iterable) {
            return ((Iterable<?>) o).iterator();
        }
        if (o instanceof Object[]) {
            return Arrays.asList((Object[]) o).iterator();
        }
        throw new UnsupportedOperationException();
    }

    // throws IllegalArgumentException
    private static int sizeOf(Object o) {
        if (o instanceof Collection)
            return ((Collection<?>) o).size();
        if (o instanceof Map)
            return ((Map<?, ?>) o).size();
        try {
            if (o.getClass().isArray())
                return Array.getLength(o);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
        throw new UnsupportedOperationException();
    }

    // throws NumberFormatException
    private static Number convertToNumber(Class<?> eClass, Object o) throws NumberFormatException {
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
    public static <T> T newInstance(@NotNull String className) {
        return newInstance((Class<T>) CLASS_ALIASES.forName(className));
    }

    @NotNull
    public static <T> T newInstance(@NotNull Class<T> clazz) {
        final Supplier<?> cons = supplierClassLocal.get(clazz);
        return (T) cons.get();
    }

    @Nullable
    public static Object newInstanceOrNull(final Class<?> type) {
        try {
            return newInstance(type);
        } catch (Exception e) {
            return null;
        }
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

    public static boolean matchingClass(@NotNull Class<?> base, @NotNull Class<?> toMatch) {
        return base == toMatch
                || base.isInterface() && interfaceToDefaultClass.get(base) == toMatch
                || Enum.class.isAssignableFrom(toMatch) && base.equals(toMatch.getEnclosingClass());
    }

    public static Object defaultValue(Class<?> type) {
        return DEFAULT_MAP.get(type);
    }

    @NotNull
    public static <T> T onMethodCall(@NotNull final BiFunction<Method, Object[], Object> biFunction,
                                     @NotNull final Class<T> tClass,
                                     final Class<?>... additional) throws IllegalArgumentException {
        final Class<?>[] interfaces = addAll(tClass, additional);
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

    public static boolean isConcreteClass(@NotNull Class<?> tClass) {
        return (tClass.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) == 0;
    }

    public static Object readResolve(@NotNull Object o) {
        Method readResove = READ_RESOLVE.get(o.getClass());
        if (readResove == null)
            return o;
        try {
            return readResove.invoke(o);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw Jvm.rethrow(e);
        } catch (InvocationTargetException e) {
            throw Jvm.rethrow(e.getCause());
        }
    }

    @Nullable
    public static Boolean toBoolean(@Nullable String s) {
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

    public static Class<?>[] getAllInterfaces(Object o) {
        try {
            Set<Class<?>> results = new HashSet<>();
            getAllInterfaces(o, results::add);
            return results.toArray(new Class<?>[results.size()]);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

    public static void getAllInterfaces(Object o, Function<Class<?>, Boolean> accumulator) throws IllegalArgumentException {
        if (null == o)
            return;

        if (null == accumulator)
            throw new IllegalArgumentException("Accumulator cannot be null");

        if (o instanceof Class) {
            getAllInterfacesForClass((Class<?>) o, accumulator);
        } else {
            getAllInterfaces(o.getClass(), accumulator);
        }
    }

    private static void getAllInterfacesForClass(Class<?> clazz, Function<Class<?>, Boolean> accumulator) {
        if (clazz.isInterface()) {
            if (Boolean.TRUE.equals(accumulator.apply(clazz))) {
                for (Class<?> aClass : clazz.getInterfaces()) {
                    getAllInterfaces(aClass, accumulator);
                }
            }
        } else {
            if (null != clazz.getSuperclass())
                getAllInterfaces(clazz.getSuperclass(), accumulator);

            for (Class<?> aClass : clazz.getInterfaces()) {
                getAllInterfaces(aClass, accumulator);
            }
        }
    }

    public static synchronized void defaultObjectForInterface(ThrowingFunction<Class<?>, Class<?>, ClassNotFoundException> defaultObjectForInterface) {
        interfaceToDefaultClass = ClassLocal.withInitial(c -> {
            Class<?> c2;
            try {
                c2 = defaultObjectForInterface.apply(c);
            } catch (ClassNotFoundException cne) {
                Jvm.warn().on(ObjectUtils.class, "Unable to find alias for " + c + " " + cne);
                c2 = c;
            }
            return lookForImplEnum(c2);
        });
        // need to reset any cached suppliers.
        supplierClassLocal = ClassLocal.withInitial(ObjectUtils::supplierForClass);
    }

    @NotNull
    static Class<?> lookForImplEnum(Class<?> c2) {
        if (c2.isInterface()) {
            try {
                final Class<?> c3 = ClassAliasPool.CLASS_ALIASES.forName(c2.getName() + "s");
                if (c2.isAssignableFrom(c3))
                    return c3;
            } catch (ClassNotFoundRuntimeException cne) {
                // ignored
            }
            if (c2 == Map.class)
                return LinkedHashMap.class;
            if (c2 == Set.class)
                return LinkedHashSet.class;
            if (c2 == List.class)
                return ArrayList.class;
        }
        return c2;
    }

    public static <T> Class<T> implementationToUse(Class<T> tClass) {
        if (tClass.isInterface()) {
            Class<?> class2 = interfaceToDefaultClass.get(tClass);
            if (class2 != null)
                return (Class<T>) class2;
        }
        return tClass;
    }

    public enum Immutability {
        YES, NO, MAYBE
    }

    private static final class ConversionFunction implements Function<Class<?>, ThrowingFunction<String, Object, Exception>> {
        @Override
        public ThrowingFunction<String, Object, Exception> apply(@NotNull Class<?> c) {
            if (c == Class.class)
                return CLASS_ALIASES::forName;
            if (c == Boolean.class)
                return ObjectUtils::toBoolean;
            if (c == UUID.class)
                return UUID::fromString;
            if (CoreDynamicEnum.class.isAssignableFrom(c))
                return EnumCache.of(c)::get;
            try {
                Method valueOf = c.getDeclaredMethod("valueOf", String.class);
                Jvm.setAccessible(valueOf);
                return s -> valueOf.invoke(null, s);
            } catch (NoSuchMethodException e) {
                // ignored
            }

            try {
                Method parse = c.getDeclaredMethod("parse", CharSequence.class);
                Jvm.setAccessible(parse);
                return s -> parse.invoke(null, s);

            } catch (NoSuchMethodException e) {
                // ignored
            }
            try {
                final Constructor<?> constructor = c.getDeclaredConstructor(String.class);
                Jvm.setAccessible(constructor);
                return constructor::newInstance;
            } catch (Exception e) {
                throw asCCE(e);
            }
        }
    }

    /**
     * Standard mechanism to determine objects as not null. Same method contract as {@link Objects#requireNonNull(Object)}
     * and also decorated with {@link NotNull} so that IntelliJ and other static analysis tools can work their magic.
     *
     * @param o reference to check for nullity
     * @throws NullPointerException if o is {@code null }
     */
    @SuppressWarnings("UnusedReturnValue")
    public static <T> T requireNonNull(@NotNull T o) {
        // see https://stackoverflow.com/questions/43115645/in-java-lambdas-why-is-getclass-called-on-a-captured-variable
        // Maybe calling Objects.requireNonNull is just as optimisable/intrinisfiable but I didn't do the research
        o.getClass();
        return o;
    }

}
