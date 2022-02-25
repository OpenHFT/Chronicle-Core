package net.openhft.chronicle.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Stream;

public enum GenericReflection {
    ;

    public static Type getReturnType(Method m, Class forClass) {
        final Type genericReturnType = m.getGenericReturnType();
        if (genericReturnType instanceof Class)
            return genericReturnType;
        final Class<?> declaringClass = m.getDeclaringClass();
        final Optional<? extends Type> extendsType = Stream.of(
                        Stream.of(forClass.getGenericSuperclass()), Stream.of(forClass.getGenericInterfaces()))
                .flatMap(s -> s)
                .filter(t -> declaringClass.equals(erase(t)))
                .findFirst();
        final Type[] typeParameters = declaringClass.getTypeParameters();
        if (extendsType.isPresent() && extendsType.get() instanceof ParameterizedType) {
            final ParameterizedType type = (ParameterizedType) extendsType.get();
            final Type[] actualTypeArguments = type.getActualTypeArguments();
            for (int i = 0; i < typeParameters.length; i++)
                if (typeParameters[i].equals(genericReturnType))
                    return actualTypeArguments[i];
        }
        return m.getGenericReturnType();
    }

    private static Class<?> erase(Type t) {
        return t instanceof ParameterizedType
                ? erase(((ParameterizedType) t).getRawType())
                : (Class) t;
    }
}
