/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.core.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public enum GenericReflection {
    ;

    /**
     * Obtain the return types for method on this type
     *
     * @param type to scan
     * @return set of types
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
     * Obtain the return type of a method as defined by a class or interface
     *
     * @param method to lookup
     * @param type   to look for the definition.
     * @return the return type
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
        final Optional<? extends Type> extendsType = Stream.of(
                        Stream.of(getGenericSuperclass(type)), Stream.of(getGenericInterfaces(type)))
                .flatMap(s -> s)
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

    public static Type[] getParameterTypes(Method method, Type type) {
        final Type[] parameterTypes = method.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = findType(method, type, parameterTypes[i]);
        }
        return parameterTypes;
    }

    static Type[] getGenericInterfaces(Type forClass) {
        if (forClass instanceof Class)
            return ((Class) forClass).getGenericInterfaces();
        if (forClass instanceof ParameterizedType) {
            return new Type[]{forClass};
        }
        throw new UnsupportedOperationException();
    }

    static Type getGenericSuperclass(Type forClass) {
        if (forClass instanceof Class)
            return ((Class) forClass).getGenericSuperclass();
        if (forClass instanceof ParameterizedType) {
            return null;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Obatin the raw type if available.
     *
     * @param type to erase
     * @return raw class
     */
    public static Class<?> erase(Type type) {
        if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type;
            return erase(tv.getBounds()[0]);
        }
        if (type instanceof ParameterizedType)
            return erase(((ParameterizedType) type).getRawType());
        return (Class) type;
    }
}
