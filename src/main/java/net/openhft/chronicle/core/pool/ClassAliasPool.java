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

package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Jvm;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClassAliasPool implements ClassLookup {
    public static final ClassAliasPool CLASS_ALIASES = new ClassAliasPool(null).defaultAliases();
    private final ClassLookup parent;
    private final ClassLoader classLoader;
    private final Map<String, Class> stringClassMap = new ConcurrentHashMap<>();
    private final Map<String, Class> stringClassMap2 = new ConcurrentHashMap<>();
    private final Map<Class, String> classStringMap = new ConcurrentHashMap<>();

    ClassAliasPool(ClassLookup parent, ClassLoader classLoader) {
        this.parent = parent;
        this.classLoader = classLoader;
    }

    ClassAliasPool(ClassLookup parent) {
        this.parent = parent;
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    private ClassAliasPool defaultAliases() {
        addAlias(Set.class, "!set");
        addAlias(SortedSet.class, "!oset");
        addAlias(List.class, "!seq");
        addAlias(Map.class, "!map");
        addAlias(SortedMap.class, "!omap");
        addAlias(String.class, "String, !str");
        addAlias(CharSequence.class);
        addAlias(Byte.class, "byte, int8");
        addAlias(Short.class, "short, int16");
        addAlias(Character.class, "Char");
        addAlias(Integer.class, "int, int32");
        addAlias(Long.class, "long, int64");
        addAlias(Float.class, "Float32");
        addAlias(Double.class, "Float64");
        addAlias(LocalDate.class, "Date");
        addAlias(LocalDateTime.class, "DateTime");
        addAlias(LocalTime.class, "Time");
        addAlias(ZonedDateTime.class, "ZonedDateTime");
        addAlias(String[].class, "String[]");
        for (Class prim : new Class[]{boolean.class, byte.class, short.class, char.class, int.class, long.class, float.class, double.class})
            addAlias(Array.newInstance(prim, 0).getClass(), prim.getName() + "[]");
        addAlias(Class.class, "type");
        addAlias(void.class, "!null");

        return this;
    }

    /**
     * remove classes which are not in the default class loaders.
     */
    public void clean() {
        clean(stringClassMap.values());
        clean(stringClassMap2.values());
        clean(classStringMap.keySet());
    }

    private void clean(Iterable<Class> coll) {
        ClassLoader classLoader2 = ClassAliasPool.class.getClassLoader();
        for (Iterator<Class> iter = coll.iterator(); iter.hasNext(); ) {
            Class clazz = iter.next();
            ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader == null || classLoader == classLoader2)
                continue;
            iter.remove();
        }
    }

    @Override
    public Class forName(CharSequence name) throws ClassNotFoundException {
        String name0 = name.toString();
        Class clazz = stringClassMap.get(name0);
        if (clazz != null)
            return clazz;
        clazz = stringClassMap2.get(name0);
        if (clazz != null) return clazz;
        return parent == null
                ? forName0(name, name0)
                : parent.forName(name);
    }

    private Class forName0(CharSequence name, String name0) {
        return stringClassMap2.computeIfAbsent(name0, n -> {
            try {
                return Class.forName(name0, true, classLoader);
            } catch (ClassNotFoundException e) {
                if (parent != null) {
                    try {
                        return parent.forName(name);
                    } catch (ClassNotFoundException e2) {
                        // ignored.
                    }
                }
                throw Jvm.rethrow(e);
            }
        });
    }

    @Override
    public String nameFor(Class clazz) {
        String name = classStringMap.get(clazz);
        return name == null
                ? parent == null
                ? nameFor0(clazz)
                : parent.nameFor(clazz)
                : name;
    }

    private String nameFor0(Class clazz) {
        return classStringMap.computeIfAbsent(clazz, (aClass) -> {
            if (Enum.class.isAssignableFrom(aClass)) {
                Class clazz2 = aClass.getSuperclass();
                if (clazz2 != null && clazz2 != Enum.class && Enum.class.isAssignableFrom(clazz2)) {
                    aClass = clazz2;
                    String alias = classStringMap.get(clazz2);
                    if (alias != null) return alias;
                }
            }
            return aClass.getName();
        });
    }

    @Override
    public void addAlias(Class... classes) {
        for (Class clazz : classes) {
            stringClassMap.putIfAbsent(clazz.getName(), clazz);
            stringClassMap2.putIfAbsent(clazz.getSimpleName(), clazz);
            stringClassMap2.putIfAbsent(toCamelCase(clazz.getSimpleName()), clazz);
            classStringMap.computeIfAbsent(clazz, Class::getSimpleName);
        }
    }

    // to lower camel case.
    private String toCamelCase(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public void addAlias(Class clazz, String names) {
        for (String name : names.split(", ?")) {
            stringClassMap.put(name, clazz);
            stringClassMap2.putIfAbsent(toCamelCase(name), clazz);
            classStringMap.putIfAbsent(clazz, name);
            addAlias(clazz);
        }
    }
}
