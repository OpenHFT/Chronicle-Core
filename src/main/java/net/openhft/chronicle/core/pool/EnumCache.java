package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.ClassLocal;

/*
 * Created by peter.lawrey@chronicle.software on 28/07/2017
 */
public abstract class EnumCache<E extends Enum<E>> {
    private static final ClassLocal<EnumCache> ENUM_CACHE_CL = ClassLocal.withInitial(
            eClass -> DynamicEnum.class.isAssignableFrom(eClass)
                    ? new DynamicEnumClass(eClass)
                    : new StaticEnumClass(eClass));
    protected final Class<E> eClass;

    protected EnumCache(Class<E> eClass) {
        this.eClass = eClass;
    }

    public static <E extends Enum<E>> EnumCache<E> of(Class<E> eClass) {
        return ENUM_CACHE_CL.get(eClass);
    }

    public abstract E valueOf(String name);
}
