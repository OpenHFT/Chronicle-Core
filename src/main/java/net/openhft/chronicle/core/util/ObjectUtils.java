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
import net.openhft.chronicle.core.internal.ClassUtil;
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

/**
 * A utility class providing various static methods to perform common operations on objects,
 * such as instantiation, type conversion, class/interface handling, and more.
 * This class cannot be instantiated and is meant to serve as a collection of utility functions.
 *
 * <p>Some of the key features provided by this utility class include:</p>
 * <ul>
 *     <li>Creating new instances of classes</li>
 *     <li>Retrieving default values for given types</li>
 *     <li>Checking if a class is concrete</li>
 *     <li>Performing conversions among types including numbers and booleans</li>
 *     <li>Handling interfaces and obtaining all interfaces implemented by given objects or classes</li>
 *     <li>Utility methods for handling exceptions</li>
 *     <li>Creating dynamic proxies for method calls</li>
 * </ul>
 *
 * <p>Note: This class is a part of the Chronicle core library and is intended to be used by
 * developers who need to perform various common operations on objects and classes within their
 * Java applications.</p>
 */
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
            ClassUtil.setAccessible(m);
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

    /**
     * Rethrows the given Throwable as a RuntimeException.
     *
     * @param throwable Throwable to rethrow.
     * @param <T>       The type of Throwable.
     * @return Nothing. This method always throws an exception.
     * @throws T The rethrown throwable.
     */
    private static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
        throw (T) throwable; // rely on vacuous cast
    }

    /**
     * Creates a supplier for the provided class.
     *
     * @param c The class to create a supplier for.
     * @return A supplier that creates instances of the provided class.
     */
    static Supplier<?> supplierForClass(Class<?> c) {
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
            rethrow(new IllegalArgumentException("primitive: " + c.getName()));
        if (c.isInterface()) {
            return () -> {
                Class<?> aClass = ObjectUtils.interfaceToDefaultClass.get(c);
                if (aClass == null || aClass == c)
                    rethrow(new IllegalArgumentException("interface: " + c.getName()));
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
            rethrow(new IllegalArgumentException("abstract class: " + c.getName()));
        try {
            Constructor<?> constructor = c.getDeclaredConstructor();
            ClassUtil.setAccessible(constructor);
            return ThrowingSupplier.asSupplier(constructor::newInstance);

        } catch (Exception e) {
            return () -> {
                try {
                    return OS.memory().allocateInstance(c);
                } catch (InstantiationException e1) {
                    throw rethrow(e1);
                }
            };
        }
    }

    /**
     * Registers the immutability status of a class.
     *
     * @param clazz       The class whose immutability status is to be registered.
     * @param isImmutable True if the class is immutable, false otherwise.
     */
    public static void immutable(final Class<?> clazz, final boolean isImmutable) {
        IMMUTABILITY_MAP.put(clazz, isImmutable ? Immutability.YES : Immutability.NO);
    }
    @Deprecated(/* to be removed x.26 */)
    public static void immutabile(final Class<?> clazz, final boolean isImmutable) {
        immutable(clazz, isImmutable);
    }

    /**
     * Checks if a class is immutable.
     *
     * @param clazz The class to check.
     * @return The immutability status of the class.
     */
    public static Immutability isImmutable(@NotNull final Class<?> clazz) {
        final Immutability immutability = IMMUTABILITY_MAP.get(clazz);
        if (immutability == null)
            return Comparable.class.isAssignableFrom(clazz) ? MAYBE : NO;
        return immutability;
    }

    /**
     * Checks if the given CharSequence is considered 'true'.
     *
     * @param s The CharSequence to check.
     * @return True if the CharSequence is considered 'true', false otherwise.
     */
    public static boolean isTrue(CharSequence s) {
        if (s == null)
            return false;
        switch (s.length()) {
            case 1:
                char ch = Character.toLowerCase(s.charAt(0));
                return ch == 't' || ch == 'y';
            case 3:
                return equalsCaseIgnore(s, "yes");
            case 4:
                return equalsCaseIgnore(s, "true");
            default:
                return false;
        }
    }

    /**
     * Checks if the given CharSequence is considered 'false'.
     *
     * @param s The CharSequence to check.
     * @return True if the CharSequence is considered 'false', false otherwise.
     */
    public static boolean isFalse(CharSequence s) {
        if (s == null)
            return false;
        switch (s.length()) {
            case 1:
                char ch = Character.toLowerCase(s.charAt(0));
                return ch == 'f' || ch == 'n';
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

    /**
     * Converts an object to the desired class if possible.
     *
     * @param eClass The target class to convert to.
     * @param o      The object to be converted.
     * @param <E>    The type of the target class.
     * @return The converted object or null if the input object is null.
     * @throws ClassCastException       if the object cannot be cast to the target class.
     * @throws IllegalArgumentException if an illegal argument is provided.
     */
    @Nullable
    public static <E> E convertTo(@Nullable Class<E> eClass, @Nullable Object o) throws ClassCastException, IllegalArgumentException {
        // shorter path.
        return eClass == null || o == null || eClass.isInstance(o)
                ? (E) o
                : convertTo0(eClass, o);
    }

    /**
     * Creates a map with keys as enum constants in uppercase and values as the enum constants themselves.
     *
     * @param c The enum class.
     * @return A map with enum constant names in uppercase as keys and enum constants as values.
     */
    @NotNull
     static Map<String, Enum<?>> caseIgnoreLookup(@NotNull Class<?> c) {
        @NotNull Map<String, Enum<?>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Object o : c.getEnumConstants()) {
            @NotNull Enum<?> e = (Enum<?>) o;
            map.put(e.name().toUpperCase(), e);
        }
        return map;
    }

    /**
     * Returns the enum constant of the specified enum class with the specified name, case is ignored.
     *
     * @param eClass The enum class.
     * @param name   The name of the enum constant to be returned.
     * @param <E>    The type of the enum.
     * @return The enum constant of the specified enum class with the specified name.
     */
    @NotNull
    public static <E extends Enum<E>> E valueOfIgnoreCase(@NotNull Class<E> eClass, @NotNull String name) {
        final Map<String, Enum<?>> map = CASE_IGNORE_LOOKUP.get(eClass);
        if (name.startsWith("{") && name.endsWith("}"))
            return getSingletonForEnum(eClass);
        @SuppressWarnings("unchecked")
        @NotNull final E anEnum = (E) map.get(name);
        return anEnum == null ? EnumCache.of(eClass).valueOf(name) : anEnum;
    }

    /**
     * Returns the single enum constant from the specified enum class, or the first one if there are multiple.
     *
     * @param eClass The enum class.
     * @param <E>    The type of the enum.
     * @return The single enum constant or the first one if multiple exist.
     */
    public static <E extends Enum<E>> E getSingletonForEnum(Class<E> eClass) {
        E[] enumConstants = eClass.getEnumConstants();
        if (enumConstants.length == 0)
            throw new AssertionError("Cannot convert marshallable to " + eClass + " as it doesn't have any instances");
        if (enumConstants.length > 1)
            Jvm.warn().on(ObjectUtils.class, eClass + " has multiple INSTANCEs, picking the first one");
        return enumConstants[0];
    }

    /**
     * Helper method to convert an object to the desired class.
     *
     * @param eClass The target class to convert to.
     * @param o      The object to be converted.
     * @param <E>    The type of the target class.
     * @return The converted object.
     * @throws NumberFormatException    if the object is a CharSequence and cannot be converted to a number.
     * @throws ClassCastException       if the object cannot be cast to the target class.
     * @throws IllegalArgumentException if an illegal argument is provided.
     */
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
                    return (E) (Character) cs.charAt(0);
                else
                    return null;
            }
            @NotNull String s = cs.toString();

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

    /**
     * Wraps the provided exception in a ClassCastException.
     *
     * @param e The exception to be wrapped.
     * @return A ClassCastException with the provided exception set as its cause.
     */
    @NotNull
    public static ClassCastException asCCE(Exception e) {
        @NotNull ClassCastException cce = new ClassCastException();
        cce.initCause(e);
        return cce;
    }

    /**
     * Converts an object to an array of a specified type.
     *
     * @param eClass The class object representing the desired array type.
     * @param o      The object to be converted to an array.
     * @param <E>    The type of the resulting array.
     * @return The object converted to an array.
     * @throws AssertionError if an array index is out of bounds or there is an illegal argument.
     */
    @NotNull
    static <E> E convertToArray(@NotNull Class<E> eClass, Object o) {
        final int len = sizeOf(o);
        final Object array = Array.newInstance(eClass.getComponentType(), len);
        final Iterator<?> iter = iteratorFor(o);
        final Class<?> elementType = elementType(eClass);
            for (int i = 0; i < len; i++) {
                @Nullable Object value = convertTo(elementType, iter.next());
                Array.set(array, i, value);
            }
        return (E) array;
    }

    /**
     * Retrieves the element type of array class or Object if it's not an array.
     *
     * @param eClass The class to retrieve the element type from.
     * @param <E>    The type of the class.
     * @return The element type of the array class or Object if it's not an array.
     */
    static <E> Class<?> elementType(@NotNull Class<E> eClass) {
        if (Object[].class.isAssignableFrom(eClass))
            return eClass.getComponentType();
        return Object.class;
    }

    /**
     * Returns an iterator for the given object if it's iterable or an array.
     *
     * @param o The object to get an iterator for.
     * @return An iterator for the given object.
     * @throws UnsupportedOperationException if the object is not iterable or an array.
     */
    private static Iterator<?> iteratorFor(Object o) {
        if (o instanceof Iterable) {
            return ((Iterable<?>) o).iterator();
        }
        if (o instanceof Object[]) {
            return Arrays.asList((Object[]) o).iterator();
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Determines the size of the given object if it's a collection, a map, or an array.
     *
     * @param o The object to determine the size of.
     * @return The size of the given object.
     * @throws AssertionError                if there is an illegal argument.
     * @throws UnsupportedOperationException if the object is not a collection, map, or array.
     */
    static int sizeOf(Object o) {
        if (o instanceof Collection)
            return ((Collection<?>) o).size();
        if (o instanceof Map)
            return ((Map<?, ?>) o).size();
        if (o.getClass().isArray())
            return Array.getLength(o);
        throw new UnsupportedOperationException();
    }

    /**
     * Converts an object to a number of the specified class.
     *
     * @param eClass The target class to convert to.
     * @param o      The object to be converted.
     * @return The object converted to a number of the specified class.
     * @throws NumberFormatException         if the object cannot be converted to a number.
     * @throws UnsupportedOperationException if the target class is not supported.
     */
    static Number convertToNumber(Class<?> eClass, Object o) throws NumberFormatException {
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

    /**
     * Creates a new instance of the class with the given class name.
     *
     * @param <T>       The type of the class to be instantiated.
     * @param className The fully qualified name of the class to be instantiated.
     * @return A new instance of the specified class.
     * @throws ClassCastException, if the class cannot be cast to the type T.
     */
    @NotNull
    public static <T> T newInstance(@NotNull String className) {
        return newInstance((Class<T>) CLASS_ALIASES.forName(className));
    }

    /**
     * Creates a new instance of the specified class.
     *
     * @param <T>   The type of the class to be instantiated.
     * @param clazz The class to be instantiated.
     * @return A new instance of the specified class.
     */
    @NotNull
    public static <T> T newInstance(@NotNull Class<T> clazz) {
        final Supplier<?> cons = supplierClassLocal.get(clazz);
        return (T) cons.get();
    }

    /**
     * Creates a new instance of the specified class, returning null if instantiation fails.
     *
     * @param type The class to be instantiated.
     * @return A new instance of the specified class or null if instantiation fails.
     */
    @Nullable
    public static Object newInstanceOrNull(final Class<?> type) {
        try {
            return newInstance(type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates an array containing the given element followed by the elements of the additional array.
     *
     * @param <T>        The type of the elements in the arrays.
     * @param first      The first element to be added to the array.
     * @param additional Additional elements to be added after the first element.
     * @return An array containing all the given elements.
     */
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

    /**
     * Checks if two classes are matching.
     *
     * @param base    The base class to be compared.
     * @param toMatch The class to be matched against the base class.
     * @return true if the classes match, false otherwise.
     */
    public static boolean matchingClass(@NotNull Class<?> base, @NotNull Class<?> toMatch) {
        return base == toMatch
                || base.isInterface() && interfaceToDefaultClass.get(base) == toMatch
                || Enum.class.isAssignableFrom(toMatch) && base.equals(toMatch.getEnclosingClass());
    }

    /**
     * Returns the default value for the given class.
     *
     * @param type The class for which to return the default value.
     * @return The default value for the given class.
     */
    public static Object defaultValue(Class<?> type) {
        return DEFAULT_MAP.get(type);
    }

    /**
     * Creates a dynamic proxy instance that delegates method calls to the provided BiFunction.
     *
     * @param <T>        The type of the proxy instance.
     * @param biFunction The BiFunction to which method calls will be delegated.
     * @param tClass     The primary interface to be implemented by the proxy instance.
     * @param additional Additional interfaces to be implemented by the proxy instance.
     * @return A dynamic proxy instance implementing the specified interfaces.
     * @throws IllegalArgumentException If the arguments are invalid.
     */
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

    /**
     * Checks if the given class is a concrete class (not abstract or interface).
     *
     * @param tClass The class to check.
     * @return true if the class is concrete, false otherwise.
     */
    public static boolean isConcreteClass(@NotNull Class<?> tClass) {
        return (tClass.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) == 0;
    }

/**
 * Invokes the readResolve method on the given object if it exists.
 *
 * @param o The object on which to invoke readResolve.
 * @return  The result of the readResolve method, or the original object if readResolve does not exist.
 */
    public static Object readResolve(@NotNull Object o) {
        Method readResove = READ_RESOLVE.get(o.getClass());
        if (readResove == null)
            return o;
        try {
            return readResove.invoke(o);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw rethrow(e);
        } catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        }
    }

/**
 * Converts a string to a Boolean.
 *
 * @param s The string to be converted.
 * @return  Boolean.TRUE if the string is "true", Boolean.FALSE if the string is "false", null otherwise.
 */
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
        if (Jvm.isDebugEnabled(ObjectUtils.class))
            Jvm.debug().on(ObjectUtils.class, "Treating '" + s + "' as false");
        return Boolean.FALSE;
    }
/**
 * Retrieves all the interfaces implemented by the given object or class.
 *
 * @param o The object or class for which to retrieve the implemented interfaces.
 * @return An array of Class objects representing all the interfaces implemented by the given object or class.
 * @throws AssertionError If an illegal argument is encountered.
 */
    public static Class<?>[] getAllInterfaces(Object o) {
        try {
            Set<Class<?>> results = new HashSet<>();
            getAllInterfaces(o, results::add);
            return results.toArray(new Class<?>[results.size()]);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }

/**
 * Recursively accumulates all interfaces implemented by the given object or class.
 *
 * @param o           The object or class for which to retrieve the implemented interfaces.
 * @param accumulator A function that accumulates the interfaces.
 * @throws IllegalArgumentException If the accumulator is null.
 */
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

/**
 * Recursively accumulates all interfaces for a given class.
 *
 * @param clazz       The class for which to retrieve the implemented interfaces.
 * @param accumulator A function that accumulates the interfaces.
 */
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
/**
 * Sets a default implementation to be used for interfaces.
 *
 * @param defaultObjectForInterface A function that takes a class and returns a default implementation for it.
 */
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

/**
 * Looks for a specific implementation of an interface and returns it.
 *
 * @param c2 The interface class for which to look for an implementation.
 * @return The implementation class, or the original class if no specific implementation is found.
 */
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

/**
 * Retrieves the implementation class to use for a given class.
 *
 * @param <T>     The type of the class.
 * @param tClass  The class for which to retrieve the implementation.
 * @return The implementation class to use.
 */
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
            if (c == byte[].class)
                return String::getBytes;
            if (CoreDynamicEnum.class.isAssignableFrom(c))
                return EnumCache.of(c)::get;
            try {
                Method valueOf = c.getDeclaredMethod("valueOf", String.class);
                ClassUtil.setAccessible(valueOf);
                return s -> valueOf.invoke(null, s);
            } catch (NoSuchMethodException e) {
                // ignored
            }

            try {
                Method parse = c.getDeclaredMethod("parse", CharSequence.class);
                ClassUtil.setAccessible(parse);
                return s -> parse.invoke(null, s);

            } catch (NoSuchMethodException e) {
                // ignored
            }
            try {
                final Constructor<?> constructor = c.getDeclaredConstructor(String.class);
                ClassUtil.setAccessible(constructor);
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
