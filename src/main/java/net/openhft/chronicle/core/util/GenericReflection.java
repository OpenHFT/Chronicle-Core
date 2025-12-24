/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides utility methods for reflective operations on generic types.
 *
 * <p>This enum serves as a utility class for obtaining generic type information of methods
 * and classes at runtime. This is especially useful for reflective operations that deal with
 * generic types, as Java utilizes type erasure.
 *
 * <p>Note: This is an enum with a single instance (a singleton), but used purely as a namespace
 * for utility methods, and cannot be instantiated.
 */
public enum GenericReflection {
    ;

    /**
     * Obtains the return types of all the methods in the specified type.
     *
     * @param type the {@link Type} to scan for methods.
     * @return a set of {@link Type} representing the return types of methods found in the specified type.
     * @throws UnsupportedOperationException if the provided type is not a {@link Class} or {@link ParameterizedType}.
     */
    public static Set<Type> getMethodReturnTypes(Type type) {
        Set<Type> types = new LinkedHashSet<>();
        if (type instanceof Class || type instanceof ParameterizedType) {
            for (Method method : erase(type).getMethods()) {
                types.add(getReturnType(method, type));
            }
            return types;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Obtains the return type of the specified method as defined by a class or interface.
     *
     * @param method the {@link Method} to lookup.
     * @param type   the {@link Type} in which to look for the method's definition.
     * @return the {@link Type} representing the return type of the method.
     */
    public static Type getReturnType(Method method, Type type) {
        final Type genericReturnType = method.getGenericReturnType();
        return findType(method, type, genericReturnType);
    }

    @Nullable
    private static Type findType(Method method, Type type, Type genericReturnType) {
        if (genericReturnType instanceof Class)
            return genericReturnType;
        final Class<?> declaringClass = method.getDeclaringClass();
        final Optional<? extends Type> extendsType = getGenericClassesSuperclassesAndInterfaces(type)
                .filter(t -> declaringClass.equals(erase(t)))
                .findFirst();
        final Type[] typeParameters = declaringClass.getTypeParameters();
        if (extendsType.isPresent() && extendsType.get() instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) extendsType.get();
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (int i = 0; i < typeParameters.length; i++)
                if (typeParameters[i].equals(genericReturnType))
                    return actualTypeArguments[i];
        }
        return genericReturnType;
    }

    /**
     * Obtains the parameter types of the specified method with generic type information.
     *
     * @param method the {@link Method} to lookup.
     * @param type   the {@link Type} in which to look for the method's definition.
     * @return an array of {@link Type} representing the parameter types of the method.
     */
    public static Type[] getParameterTypes(Method method, Type type) {
        final Type[] parameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Type typeI = findType(method, type, parameterTypes[i]);
            if (typeI instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) typeI;
                Type[] bounds = typeVariable.getBounds();
                if (bounds.length > 0) {
                    typeI = bounds[0];
                } else {
                    // If no bounds, use Object as a fallback
                    typeI = Object.class;
                }
            }
            parameterTypes[i] = typeI;
        }
        return parameterTypes;
    }

    static Stream<Type> getGenericClassesSuperclassesAndInterfaces(Type forClass) {
        Set<Type> result = new HashSet<>();
        getGenericClassesSuperclassesAndInterfaces(forClass, result);
        return result.stream();
    }

    private static void getGenericClassesSuperclassesAndInterfaces(Type forClass, Set<Type> collectedTypes) {
        if (forClass instanceof ParameterizedType) {
            collectedTypes.add(forClass);
            getGenericClassesSuperclassesAndInterfaces(((ParameterizedType) forClass).getRawType(), collectedTypes);
            return;
        }

        if (!(forClass instanceof Class))
            throw new UnsupportedOperationException();

        Class<?> forClass2 = (Class<?>) forClass;
        for (Type genericInterface : forClass2.getGenericInterfaces())
            getGenericClassesSuperclassesAndInterfaces(genericInterface, collectedTypes);

        Type superclass = forClass2.getGenericSuperclass();
        if (superclass != null)
            getGenericClassesSuperclassesAndInterfaces(superclass, collectedTypes);
    }

    /**
     * Obtains the raw type representation of the specified generic type. If the type is already
     * raw or if it's a type variable, the method returns the erasure of the type.
     *
     * @param type the {@link Type} to erase.
     * @return the raw {@link Class} representation of the specified type.
     */
    public static Class<?> erase(Type type) {
        if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            return erase(tv.getBounds()[0]);
        }
        if (type instanceof ParameterizedType)
            return erase(((ParameterizedType) type).getRawType());
        if (type instanceof Class<?>)
            return (Class<?>) type;
        throw new ClassCastException("Cannot erase non-class type " + type);
    }
}
