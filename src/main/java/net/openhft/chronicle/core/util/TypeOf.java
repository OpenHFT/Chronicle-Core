package net.openhft.chronicle.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeOf<T> {
    private final Type type = extractType();

    public Type type() {
        return type;
    }

    private Type extractType() {
        Type t = getClass().getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
            throw new RuntimeException("must specify type parameters");
        }
        ParameterizedType pt = (ParameterizedType) t;
        if (pt.getRawType() != TypeOf.class) {
            throw new RuntimeException("must directly extend TypeOf");
        }
        return pt.getActualTypeArguments()[0];
    }
}
