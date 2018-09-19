package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/*
 * Created by peter.lawrey@chronicle.software on 28/07/2017
 */
public class DynamicEnumClass<E extends Enum<E>> extends EnumCache<E> {
    final Map<String, E> eMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Field nameField;
    private final Field ordinalField;
    private final Function<String, E> create = this::create;

    DynamicEnumClass(Class<E> eClass) {
        super(eClass);
        for (E e : eClass.getEnumConstants()) {
            eMap.put(e.name(), e);
        }
        nameField = Jvm.getField(eClass, "name");
        ordinalField = Jvm.getField(eClass, "ordinal");
    }

    @Override
    public E valueOf(String name) {
        return eMap.computeIfAbsent(name, create);
    }

    // called while holding a lock on eMap
    private E create(String name) {
        try {
            E e = OS.memory().allocateInstance(eClass);
            nameField.set(e, name);
            ordinalField.set(e, eMap.size());
            return e;

        } catch (Exception e1) {
            throw new AssertionError(e1);
        }
    }
}
