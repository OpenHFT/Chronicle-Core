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

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.util.ClassNotFoundRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A class responsible for looking up classes and associating them with aliases for
 * more convenient referencing. ClassAliasPool supports custom class loaders and allows
 * for modification of its lookup data without affecting parent lookups.
 */
public class ClassAliasPool implements ClassLookup {

    public static final ClassAliasPool CLASS_ALIASES = new ClassAliasPool(null).defaultAliases();
    static final ThreadLocal<CAPKey> CAP_KEY_TL = ThreadLocal.withInitial(() -> new CAPKey(null));

    private final ClassLookup parent; // Parent ClassLookup used as a fallback if class isn't found
    private final ClassLoader classLoader; // ClassLoader for loading classes
    private final Map<CAPKey, Class<?>> stringClassMap = new ConcurrentHashMap<>(); // Map of class names to classes
    private final Map<CAPKey, Class<?>> stringClassMap2 = new ConcurrentHashMap<>(); // Secondary map for class name lookups
    private final Map<Class<?>, String> classStringMap = new ConcurrentHashMap<>(); // Map of classes to their string aliases

    /**
     * Constructs a new ClassAliasPool with the specified parent ClassLookup and ClassLoader.
     *
     * @param parent      The parent ClassLookup that can be consulted if a class cannot be found
     *                    in this ClassAliasPool.
     * @param classLoader The ClassLoader to be used for loading classes.
     */
    ClassAliasPool(ClassLookup parent, ClassLoader classLoader) {
        this.parent = parent;
        this.classLoader = classLoader;
    }

    /**
     * Constructs a new ClassAliasPool with the specified parent ClassLookup. The ClassLoader
     * is derived from the parent if it is non-null, otherwise it is derived from this class.
     *
     * @param parent The parent ClassLookup that can be consulted if a class cannot be found
     *               in this ClassAliasPool.
     */
    ClassAliasPool(ClassLookup parent) {
        this.parent = parent;
        this.classLoader = (parent == null ? this : parent).getClass().getClassLoader();
    }

    /**
     * Throws an assertion error with the provided class.
     *
     * @param clazz The class to include in the assertion error message.
     */
    public static void a(Class<?> clazz) {
        throw Jvm.rethrow(new AssertionError(clazz));
    }

    /**
     * Checks if the class belongs to a specific package.
     *
     * @param pkgName The package name to check against.
     * @param clazz   The class to verify.
     * @return True if the class belongs to the specified package, false otherwise.
     */
    protected static boolean testPackage(String pkgName, Class<?> clazz) {
        Package aPackage = clazz.getPackage();
        return aPackage != null && aPackage.getName().startsWith(pkgName);
    }

    /**
     * Populates this ClassAliasPool with a default set of aliases for commonly used classes.
     *
     * @return The ClassAliasPool instance populated with default aliases.
     */
    @NotNull
    private ClassAliasPool defaultAliases() {
        addAlias(Set.class, "!set, Set");
        addAlias(BitSet.class, "!bitset, BitSet");
        addAlias(SortedSet.class, "!oset, SortedSet");
        addAlias(List.class, "!seq, List");
        addAlias(Map.class, "!map, Map");
        addAlias(SortedMap.class, "!omap, SortedMap");
        addAlias(String.class, "String, !str");
        addAlias(CharSequence.class);
        addAlias(Byte.class, "byte, int8, Byte");
        addAlias(Short.class, "short, int16, Short");
        addAlias(Character.class, "Char, Character");
        addAlias(Integer.class, "int, int32, Integer");
        addAlias(Long.class, "long, int64, Long");
        addAlias(Float.class, "Float32, Float");
        addAlias(Double.class, "Float64, Double");
        addAlias(LocalDate.class, "Date, LocalDate");
        addAlias(LocalDateTime.class, "DateTime, LocalDateTime");
        addAlias(LocalTime.class, "Time, LocalTime");
        addAlias(ZonedDateTime.class, "ZonedDateTime");
        addAlias(TimeUnit.class, "TimeUnit");
        addAlias(String[].class, "String[]");
        for (@NotNull Class prim : new Class[]{boolean.class, byte.class, short.class, char.class, int.class, long.class, float.class, double.class})
            addAlias(Array.newInstance(prim, 0).getClass(), prim.getName() + "[]");
        // byte[] gets in before camel cased Byte[]
        addAlias(Byte[].class, "Byte[]");
        addAlias(Class.class, "type, class, Class");
        addAlias(void.class, "!null");

        return this;
    }

    /**
     * Removes classes from the lookup which are not loaded by the default class loaders.
     * This is used to clean up the ClassAliasPool.
     */
    public void clean() {
        clean(stringClassMap.values());
        clean(stringClassMap2.values());
        clean(classStringMap.keySet());
    }

    /**
     * Cleans the provided collection of classes by removing those not loaded
     * by the default class loader.
     *
     * @param coll Collection of classes to be cleaned.
     */
    private void clean(@NotNull Iterable<Class<?>> coll) {
        ClassLoader classLoader2 = ClassAliasPool.class.getClassLoader();
        for (Iterator<Class<?>> iter = coll.iterator(); iter.hasNext(); ) {
            Class<?> clazz = iter.next();
            ClassLoader cl = clazz.getClassLoader();
            if (cl == null || cl == classLoader2)
                continue;
            iter.remove(); // Remove classes not loaded by the default class loader
        }
    }

    /**
     * Finds the class corresponding to the provided name.
     *
     * @param name The name of the class to find.
     * @return The class corresponding to the name.
     * @throws ClassNotFoundRuntimeException if the class cannot be found.
     */
    @Override
    @NotNull
    public Class<?> forName(@NotNull CharSequence name) throws ClassNotFoundRuntimeException {
        Objects.requireNonNull(name);
        CAPKey key = CAP_KEY_TL.get();
        key.value = name;
        Class<?> clazz = stringClassMap.get(key);
        if (clazz != null)
            return clazz;
        clazz = stringClassMap2.get(key);
        if (clazz != null) return clazz;
        return forName0(key);
    }

    /**
     * Looks up the class by its name, handling cases where the class
     * might not be initially found in the primary or secondary maps.
     *
     * @param key The key representing the class name.
     * @return The class corresponding to the key.
     * @throws ClassNotFoundRuntimeException if the class cannot be found.
     */
    @NotNull
    private synchronized Class<?> forName0(@NotNull CAPKey key) throws ClassNotFoundRuntimeException {
        Class<?> clazz = stringClassMap2.get(key);
        if (clazz != null) return clazz;
        String name0 = key.toString();
        CAPKey key2 = new CAPKey(name0);

        clazz = OS.isWindows() || OS.isMacOSX() ? doLookupWindowsOSX(name0) : doLookup(name0);

        stringClassMap2.put(key2, clazz);
        return clazz;
    }

    /**
     * On Windows & OSX, if you ask for a class with the wrong case, it will throw
     * this instead of ClassNotFoundException.
     *
     * @param name The class name to lookup
     * @return The resolved class
     * @throws ClassNotFoundRuntimeException if the class can't be loaded
     */
    private Class<?> doLookupWindowsOSX(String name) {
        try {
            return doLookup(name);
        } catch (NoClassDefFoundError e) {
            // Wraps the NoClassDefFoundError in a ClassNotFoundRuntimeException
            throw new ClassNotFoundRuntimeException(new ClassNotFoundException(e.getMessage(), e));
        }
    }

    /**
     * Attempts to find and load the class with the specified name.
     *
     * @param name The fully qualified name of the class to look up
     * @return The class object for the specified name
     * @throws ClassNotFoundRuntimeException if the class cannot be found or is banned
     */
    private Class<?> doLookup(String name) {
        if (banned(name))
            // Throws an exception if the class is banned
            throw new ClassNotFoundRuntimeException(new ClassNotFoundException(name + " not available"));
        try {
            // Attempts to load the class using the class loader
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            // Fallback to parent ClassLookup if available
            if (parent != null)
                return parent.forName(name);
            throw new ClassNotFoundRuntimeException(e);
        }
    }

    /**
     * Determines if the class with the specified name is banned from loading.
     *
     * @param name The name of the class to check
     * @return true if the class is banned, false otherwise
     */
    private boolean banned(String name) {
        if (name.isEmpty())
            return true;
        switch (name.charAt(0)) {
            case 'c':
                return name.startsWith("com.sun.")
                        || name.startsWith("com.oracle");
            case 'j':
                return name.startsWith("jdk.");
            case 's':
                return name.startsWith("sun.");
            default:
                return false;
        }
    }

    /**
     * Gets the name associated with a class.
     *
     * @param clazz The class whose name is to be retrieved
     * @return The name of the class
     * @throws IllegalArgumentException if the class is a lambda class
     */
    @Override
    public String nameFor(Class<?> clazz) throws IllegalArgumentException {
        if (Jvm.isLambdaClass(clazz))
            // Lambda classes do not have meaningful names
            throw new IllegalArgumentException("Class name for " + clazz + " isn't meaningful.");
        String name = classStringMap.get(clazz);
        if (name != null)
            return name;
        if (parent != null)
            return parent.nameFor(clazz);
        return nameFor0(clazz);
    }

    /**
     * Retrieves or computes the canonical name for the specified class.
     *
     * @param clazz The class whose name is to be determined
     * @return The canonical name of the class
     */
    private String nameFor0(Class<?> clazz) {
        if (Enum.class.isAssignableFrom(clazz)) {
            Class<?> clazz2 = clazz.getSuperclass();
            if (clazz2 != null && clazz2 != Enum.class && Enum.class.isAssignableFrom(clazz2)) {
                String alias = classStringMap.get(clazz2);
                if (alias != null) {
                    classStringMap.putIfAbsent(clazz, alias);
                    return alias;
                }
                return clazz2.getName();
            }
        }
        return clazz.getName();
    }

    /**
     * Removes all classes from the lookup maps that belong to the specified package.
     *
     * @param pkgName The package name to remove
     */
    public void removePackage(String pkgName) {
        stringClassMap.entrySet().removeIf(e -> testPackage(pkgName, e.getValue()));
        stringClassMap2.entrySet().removeIf(e -> testPackage(pkgName, e.getValue()));
        classStringMap.entrySet().removeIf(e -> testPackage(pkgName, e.getKey()));
    }

    /**
     * Adds the specified classes to the alias lookup maps.
     *
     * @param classes The classes to add
     */
    @Override
    public void addAlias(@NotNull Class<?>... classes) {
        for (@NotNull Class<?> clazz : classes) {
            Class<?> prev = stringClassMap.putIfAbsent(new CAPKey(clazz.getName()), clazz);
            warnIfChanged(prev, clazz, "Did not replace by name");
            prev = stringClassMap2.putIfAbsent(new CAPKey(clazz.getSimpleName()), clazz);
            warnIfChanged(prev, clazz, "Did not replace by simpleName");
            stringClassMap2.putIfAbsent(new CAPKey(toCamelCase(clazz.getSimpleName())), clazz);
            classStringMap.computeIfAbsent(clazz, Class::getSimpleName);
        }
    }

    /**
     * Converts the given string to lower camel case.
     *
     * @param name The string to convert
     * @return The string converted to lower camel case
     */
    @NotNull
    private String toCamelCase(@NotNull String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Adds the specified class to the alias lookup maps with the given alias names.
     *
     * @param clazz The class to add
     * @param names The alias names for the class
     */
    @Override
    public void addAlias(Class<?> clazz, @NotNull String names) {
        for (@NotNull String name : names.split(", ?")) {
            Class<?> prev = stringClassMap.put(new CAPKey(name), clazz);
            warnIfChanged(prev, clazz, "Replaced");
            stringClassMap2.putIfAbsent(new CAPKey(toCamelCase(name)), clazz);
            classStringMap.putIfAbsent(clazz, name);
            Class<?> prev1 = stringClassMap.putIfAbsent(new CAPKey(clazz.getName()), clazz);
            warnIfChanged(prev1, clazz, "Did not replace by name");
        }
    }

    /**
     * Logs a warning if the previous class associated with an alias was replaced.
     *
     * @param prev  The previous class associated with the alias
     * @param clazz The new class associated with the alias
     * @param msg   The warning message
     */
    private void warnIfChanged(Class<?> prev, Class<?> clazz, String msg) {
        if (prev != null && prev != clazz)
            Jvm.warn().on(getClass(), msg + " " + prev + " with " + clazz);
    }

    /**
     * A key class for class alias lookups that implements CharSequence.
     */
    static final class CAPKey implements CharSequence {
        CharSequence value;

        /**
         * Constructs a new CAPKey with the specified value.
         *
         * @param name The value of the key
         */
        CAPKey(String name) {
            value = name;
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) throws IndexOutOfBoundsException {
            return value.charAt(index);
        }

        @NotNull
        @Override
        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public int hashCode() {
            if (value instanceof String)
                return value.hashCode();
            int h = 0;
            for (int i = 0; i < value.length(); i++) {
                h = 31 * h + charAt(i);
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CharSequence))
                return false;

            CharSequence cs = (CharSequence) obj;
            if (length() != cs.length())
                return false;
            for (int i = 0; i < length(); i++)
                if (charAt(i) != cs.charAt(i))
                    return false;
            return true;
        }
    }

/* \u002f
 * public static void a\u202e(Class... classes) {
 * CLASS_ALIASES.addAlias(classes);
 * }
 */
 \u002f**/
}
