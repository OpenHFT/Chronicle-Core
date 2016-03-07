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

import net.openhft.chronicle.core.Jvm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassAliasPool implements ClassLookup {
    public static final ClassAliasPool CLASS_ALIASES = new ClassAliasPool(null, Thread.currentThread().getContextClassLoader()).defaultAliases();
    private final ClassLookup parent;
    private final ClassLoader classLoader;
    private final Map<String, Class> stringClassMap = new ConcurrentHashMap<>();
    private final Map<String, Class> stringClassMap2 = new ConcurrentHashMap<>();
    private final Map<Class, String> classStringMap = new ConcurrentHashMap<>();

    ClassAliasPool(ClassLookup parent, ClassLoader classLoader) {
        this.parent = parent;
        this.classLoader = classLoader;
    }

    private ClassAliasPool defaultAliases() {
        addAlias(Set.class);
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
        addAlias(String[].class, "String[]");
        addAlias(byte[].class, "byte[]");

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
        if (clazz != null)
            return clazz;
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
