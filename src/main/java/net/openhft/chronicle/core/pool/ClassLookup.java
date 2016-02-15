package net.openhft.chronicle.core.pool;

/**
 * Created by peter on 15/02/16.
 */
public interface ClassLookup {

    static ClassLookup create(ClassLoader loader) {
        return ClassAliasPool.CLASS_ALIASES.wrap(loader);
    }

    default ClassLookup wrap(ClassLoader loader) {
        return new ClassAliasPool(this, loader);
    }

    Class forName(CharSequence name) throws ClassNotFoundException;

    String nameFor(Class clazz);

    void addAlias(Class... classes);

    void addAlias(Class clazz, String names);
}
