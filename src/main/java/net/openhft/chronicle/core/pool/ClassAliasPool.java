package net.openhft.chronicle.core.pool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by peter on 12/06/15.
 */
public class ClassAliasPool {
    public static final ClassAliasPool CLASS_ALIASES = new ClassAliasPool().defaultAliases();

    private final Map<String, Class> stringClassMap = new ConcurrentHashMap<>();
    private final Map<String, Class> stringClassMap2 = new ConcurrentHashMap<>();
    private final Map<Class, String> classStringMap = new ConcurrentHashMap<>();

    public ClassAliasPool defaultAliases() {
        addAlias(Set.class);
        addAlias(String.class);
        addAlias(CharSequence.class);
        addAlias(Byte.class, "Byte, int8");
        addAlias(Character.class, "Char");
        addAlias(Integer.class, "int32");
        addAlias(Long.class, "Int, int64");
        addAlias(Float.class, "Float32");
        addAlias(Double.class, "Float64");
        addAlias(LocalDate.class, "Date");
        addAlias(LocalDateTime.class, "DateTime");
        addAlias(LocalTime.class, "Time");
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
        Iterator<Class> iter = coll.iterator();
        ClassLoader classLoader2 = ClassAliasPool.class.getClassLoader();
        while (iter.hasNext()) {
            Class clazz = iter.next();
            ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader == null || classLoader == classLoader2)
                continue;
            iter.remove();
        }
    }

    public Class forName(CharSequence name) throws IllegalArgumentException {
        String name0 = name.toString();
        return stringClassMap.getOrDefault(name0,
                stringClassMap2.computeIfAbsent(name0, n -> {
                    try {
                        return Class.forName(name0);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }
                }));
    }

    public String nameFor(Class clazz) {
        return classStringMap.computeIfAbsent(clazz, c -> c.getName());
    }

    public void addAlias(Class clazz) {
        stringClassMap.putIfAbsent(clazz.getName(), clazz);
        stringClassMap2.putIfAbsent(clazz.getSimpleName(), clazz);
        stringClassMap2.putIfAbsent(toCamelCase(clazz.getSimpleName()), clazz);
    }

    // to lower camel case.
    private String toCamelCase(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public void addAlias(Class clazz, String names) {
        for (String name : names.split(", ?")) {
            stringClassMap.put(name, clazz);
            stringClassMap2.putIfAbsent(toCamelCase(name), clazz);
            classStringMap.putIfAbsent(clazz, name);
            addAlias(clazz);
        }
    }
}
