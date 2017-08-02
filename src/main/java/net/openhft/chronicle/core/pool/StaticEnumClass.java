package net.openhft.chronicle.core.pool;

/*
 * Created by peter.lawrey@chronicle.software on 28/07/2017
 */
public class StaticEnumClass<E extends Enum<E>> extends EnumCache<E> {
    StaticEnumClass(Class<E> eClass) {
        super(eClass);
    }

    @Override
    public E valueOf(String name) {
        return Enum.valueOf(eClass, name);
    }
}
