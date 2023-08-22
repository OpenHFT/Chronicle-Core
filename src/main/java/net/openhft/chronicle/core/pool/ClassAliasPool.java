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
    private final ClassLookup parent;
    private final ClassLoader classLoader;
    private final Map<CAPKey, Class<?>> stringClassMap = new ConcurrentHashMap<>();
    private final Map<CAPKey, Class<?>> stringClassMap2 = new ConcurrentHashMap<>();
    private final Map<Class<?>, String> classStringMap = new ConcurrentHashMap<>();

    /**
     * Constructs a new ClassAliasPool with the specified parent ClassLookup and ClassLoader.
     *
     * @param parent The parent ClassLookup that can be consulted if a class cannot be found
     *               in this ClassAliasPool.
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

    public static void a(Class<?> clazz) {
        throw Jvm.rethrow(new AssertionError(clazz));
    }

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

    private void clean(@NotNull Iterable<Class<?>> coll) {
        ClassLoader classLoader2 = ClassAliasPool.class.getClassLoader();
        for (Iterator<Class<?>> iter = coll.iterator(); iter.hasNext(); ) {
            Class<?> clazz = iter.next();
            ClassLoader cl = clazz.getClassLoader();
            if (cl == null || cl == classLoader2)
                continue;
            iter.remove();
        }
    }

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
     * this instead of ClassNotFoundException
     *
     * @param name The class name to lookup
     * @return The resolved class
     * @throws ClassNotFoundRuntimeException If the class can't be loaded
     */
    private Class<?> doLookupWindowsOSX(String name) {
        try {
            return doLookup(name);
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundRuntimeException(new ClassNotFoundException(e.getMessage(), e));
        }
    }

    private Class<?> doLookup(String name) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            if (parent != null)
                return parent.forName(name);
            throw new ClassNotFoundRuntimeException(e);
        }
    }

    @Override
    public String nameFor(Class<?> clazz) throws IllegalArgumentException {
        if (clazz.getName().contains("$$Lambda"))
            throw new IllegalArgumentException("Class name for " + clazz + " isn't meaningful.");
        String name = classStringMap.get(clazz);
        if (name != null)
            return name;
        if (parent != null)
            return parent.nameFor(clazz);
        return nameFor0(clazz);
    }

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

    public void removePackage(String pkgName) {
        stringClassMap.entrySet().removeIf(e -> testPackage(pkgName, e.getValue()));
        stringClassMap2.entrySet().removeIf(e -> testPackage(pkgName, e.getValue()));
        classStringMap.entrySet().removeIf(e -> testPackage(pkgName, e.getKey()));
    }

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

    // to lower camel case.
    @NotNull
    private String toCamelCase(@NotNull String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

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

    private void warnIfChanged(Class<?> prev, Class<?> clazz, String msg) {
        if (prev != null && prev != clazz)
            Jvm.warn().on(getClass(), msg + " " + prev + " with " + clazz);
    }

    static final class CAPKey implements CharSequence {
        CharSequence value;

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

/**\u002f
 public static void a\u202e(Class... classes) {
 CLASS_ALIASES.addAlias(classes);
 }
 \u002f**/
}
