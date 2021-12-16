package net.openhft.chronicle.core.threads;

import net.openhft.chronicle.core.Jvm;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ThreadLocalProperties extends Properties {
    static final boolean THREAD_LOCAL_PROPERTIES = Jvm.getBoolean("threadLocal.properties");
    final ThreadLocal<Properties> tl = ThreadLocal.withInitial(() -> new Properties(defaults));

    public ThreadLocalProperties(Properties defaults) {
        super(defaults);
    }

    public static void forSystemProperties() {
        forSystemProperties(THREAD_LOCAL_PROPERTIES);
    }

    public static void forSystemProperties(boolean enabled) {
        if (!enabled)
            return;
        synchronized (System.class) {
            Properties properties = System.getProperties();
            if (properties instanceof ThreadLocalProperties)
                return;
            System.setProperties(new ThreadLocalProperties(properties));
        }
    }

    @Override
    public Object setProperty(String key, String value) {
        return tl.get().setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return tl.get().getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return tl.get().getProperty(key, defaultValue);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return tl.get().propertyNames();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return tl.get().stringPropertyNames();
    }

    @Override
    public Object get(Object key) {
        return tl.get().get(key);
    }

    @Override
    protected void rehash() {
        // no implemented
    }

    @Override
    public Object put(Object key, Object value) {
        return tl.get().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return tl.get().remove(key);
    }

    @Override
    public void clear() {
        tl.get().clear();
    }

    @Override
    public Object clone() {
        final ThreadLocalProperties clone = new ThreadLocalProperties(defaults);
        clone.putAll(tl.get());
        return clone;
    }

    @Override
    public String toString() {
        return tl.get().toString();
    }

    @NotNull
    @Override
    public Set<Object> keySet() {
        return tl.get().keySet();
    }

    @NotNull
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return tl.get().entrySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return tl.get().values();
    }

    @Override
    public boolean equals(Object o) {
        return tl.get().equals(o);
    }

    @Override
    public int hashCode() {
        return tl.get().hashCode();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return tl.get().getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super Object, ? super Object> action) {
        tl.get().forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        tl.get().replaceAll(function);
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        return tl.get().putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return tl.get().remove(key, value);
    }

    @Override
    public boolean replace(Object key, Object oldValue, Object newValue) {
        return tl.get().replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(Object key, Object value) {
        return tl.get().replace(key, value);
    }

    @Override
    public Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        return tl.get().computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return tl.get().computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return tl.get().compute(key, remappingFunction);
    }

    @Override
    public Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return tl.get().merge(key, value, remappingFunction);
    }

    @Override
    public synchronized int size() {
        return tl.get().size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return tl.get().isEmpty();
    }
}
