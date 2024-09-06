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

package net.openhft.chronicle.core.internal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

/**
 * Utility class providing methods for working with Java reflection and method handles.
 * <p>
 * This class includes functionality for accessing private fields and methods, setting accessibility,
 * and working with reflection across different Java versions. It handles differences in reflection
 * behavior starting with Java 9, where stronger encapsulation rules were introduced.
 * </p>
 * <p>
 * The class uses {@link MethodHandle} to invoke {@code setAccessible0} method on accessible objects
 * when running on Java 9 or higher, allowing access to private members.
 * </p>
 * <p>
 * This class cannot be instantiated and is intended to provide static utility methods.
 * </p>
 */
public final class ClassUtil {

    // MethodHandle for invoking the setAccessible0 method reflectively in Java 9+
    public static final MethodHandle setAccessible0_Method = getSetAccessible0Method();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ClassUtil() {
    }

    /**
     * Retrieves a {@link MethodHandle} for the {@code setAccessible0} method, allowing access to private members
     * in Java 9 or higher. This method attempts to handle the reflection behavior changes in Java 9+.
     *
     * @return A {@link MethodHandle} for the {@code setAccessible0} method, or {@code null} if the JVM is below Java 9.
     */
    private static MethodHandle getSetAccessible0Method() {
        if (!Bootstrap.isJava9Plus()) {
            return null;
        }
        final MethodType signature = MethodType.methodType(boolean.class, boolean.class);
        try {
            // Access privateLookupIn() reflectively to support compilation with JDK 8
            Method privateLookupIn = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) privateLookupIn.invoke(null, AccessibleObject.class, MethodHandles.lookup());
            return lookup.findVirtual(AccessibleObject.class, "setAccessible0", signature);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 IllegalArgumentException e) {
            Logger logger = LoggerFactory.getLogger(ClassUtil.class);
            logger.error("Chronicle products require command line arguments to be provided for Java 11 and above. See https://chronicle.software/chronicle-support-java-17");
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Retrieves a declared field from the given class or its superclasses, and makes it accessible.
     *
     * @param clazz The class to search for the field.
     * @param name  The name of the field.
     * @param error If {@code true}, throws an {@link AssertionError} if the field is not found.
     * @return The {@link Field} if found, or {@code null} if not found and {@code error} is false.
     */
    public static Field getField0(@NotNull final Class<?> clazz,
                                  @NotNull final String name,
                                  final boolean error) {
        try {
            final Field field = clazz.getDeclaredField(name);
            setAccessible(field);
            return field;

        } catch (NoSuchFieldException e) {
            final Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                final Field field = getField0(superclass, name, false);
                if (field != null)
                    return field;
            }
            if (error)
                throw new AssertionError(e);
            return null;
        }
    }

    /**
     * Sets the accessible flag for the provided {@link AccessibleObject}, allowing it to be accessed even if private.
     * <p>
     * If running on Java 9 or higher, this method will use {@code setAccessible0} reflectively. On earlier versions,
     * it uses {@link AccessibleObject#setAccessible(boolean)} directly.
     * </p>
     *
     * @param accessibleObject The object to set as accessible.
     * @throws SecurityException If the request is denied by the security manager.
     */
    public static void setAccessible(@NotNull final AccessibleObject accessibleObject) {
        if (Bootstrap.isJava9Plus())
            try {
                boolean newFlag = (boolean) setAccessible0_Method.invokeExact(accessibleObject, true);
                assert newFlag;
            } catch (Throwable throwable) {
                throw new AssertionError(throwable);
            }
        else
            accessibleObject.setAccessible(true);
    }

    /**
     * Retrieves a declared method from the given class or its superclasses, and makes it accessible.
     *
     * @param clazz  The class to search for the method.
     * @param name   The name of the method.
     * @param args   The parameter types of the method.
     * @param first  If {@code true}, throws an {@link AssertionError} if the method is not found.
     * @return The {@link Method} if found, or {@code null} if not found and {@code first} is false.
     */
    public static Method getMethod0(@NotNull final Class<?> clazz,
                                    @NotNull final String name,
                                    final Class[] args,
                                    final boolean first) {
        try {
            final Method method = clazz.getDeclaredMethod(name, args);
            if (!Modifier.isPublic(method.getModifiers()) ||
                    !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
                setAccessible(method);
            return method;

        } catch (NoSuchMethodException e) {
            final Class<?> superclass = clazz.getSuperclass();
            if (superclass != null)
                try {
                    final Method m = getMethod0(superclass, name, args, false);
                    if (m != null)
                        return m;
                } catch (Exception ignored) {
                    // Ignore
                }
            if (first)
                throw new AssertionError(e);
            return null;
        }
    }
}
