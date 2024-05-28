/*
 * Copyright 2016-2021 chronicle.software
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

import net.openhft.chronicle.core.internal.ClassUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class that facilitates the dynamic loading and debugging of Java classes.
 * <p>
 * This class provides a method for defining a new class in the context of the specified class loader,
 * using an array of bytes in the form of an ongoing Java class file containing a class or interface
 * definition.
 * <p>
 * Note that this class is intended for use cases where there is a need to load classes into the JVM
 * programmatically during runtime.
 */
public final class CompilerUtils {
    private static final Method DEFINE_CLASS_METHOD;

    static {
        try {
            DEFINE_CLASS_METHOD = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class);
            ClassUtil.setAccessible(DEFINE_CLASS_METHOD);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private CompilerUtils() {
    }

    /**
     * Dynamically defines a new Java class in the context of the specified class loader, using
     * an array of bytes containing the class file.
     *
     * @param classLoader The class loader into which the class should be loaded.
     * @param className   The expected fully qualified name of the class.
     * @param bytes       The bytes comprising the ongoing Java class file in the form of a class
     *                    or interface definition.
     * @return The {@link Class} object representing the defined class.
     * @throws AssertionError if the class could not be defined for any reason.
     */
    public static Class<?> defineClass(
            @NotNull ClassLoader classLoader, @NotNull String className, byte @NotNull [] bytes) {
        try {
            return (Class<?>) DEFINE_CLASS_METHOD
                    .invoke(classLoader, className, bytes, 0, bytes.length);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
            throw new AssertionError(e.getCause());
        }
    }
}
