/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.util.ClassLocal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Utility class to provide package name functionality.
 */
public final class PackageNameUtil {

    private PackageNameUtil() {
    }

    // Function to determine the package name of a class.
    private static final Function<Class<?>, String> PACKAGE_NAME_FUNCTION;

    static {
        Function<Class<?>, String> packageNameFunction = null;
        if (Jvm.isJava9Plus()) {
            try {
                Method getPackageNameMethod = Class.class.getMethod("getPackageName");
                MethodHandle methodHandle = MethodHandles.lookup().unreflect(getPackageNameMethod);
                packageNameFunction = clazz -> {
                    try {
                        return (String) methodHandle.invokeExact(clazz);
                    } catch (Throwable e) {
                        throw Jvm.rethrow(e);
                    }
                };
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // ignored
            }
        }

        if (packageNameFunction == null) {
            final ClassLocal<String> packageNameCache = ClassLocal.withInitial(clazz -> {
                String className = clazz.getName();
                int lastDotIndex = className.lastIndexOf('.');
                return lastDotIndex < 0 ? "" : className.substring(0, lastDotIndex);
            });
            packageNameFunction = packageNameCache::get;
        }

        PACKAGE_NAME_FUNCTION = packageNameFunction;
    }

    /**
     * Returns the package name of the specified class.
     * <p>
     * This method uses {@code Class.getPackageName()} if running on Java 9 or newer.
     * For older versions, it uses a cached value determined by the class name.
     * </p>
     *
     * @param clazz the class whose package name is to be determined
     * @return the package name of the specified class
     */
    public static String getPackageName(Class<?> clazz) {
        return PACKAGE_NAME_FUNCTION.apply(clazz);
    }
}
