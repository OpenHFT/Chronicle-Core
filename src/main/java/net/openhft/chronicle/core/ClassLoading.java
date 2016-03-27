/*
 * Copyright 2016 higherfrequencytrading.com
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

package net.openhft.chronicle.core;

/**
 * Utility class to create classes from a byte[]
 */
public enum ClassLoading {
    ;

    /**
     * Define a class into the current class loader
     *
     * @param className of the class to define.
     * @param bytes     byte code for the class
     * @return the class loaded.
     */
    public static Class defineClass(String className, byte[] bytes) {
        return defineClass(Thread.currentThread().getContextClassLoader(), className, bytes);
    }

    /**
     * Define a class into the current class loader
     *
     * @param classLoader to load the class.
     * @param className   of the class to define.
     * @param bytes       byte code for the class
     * @return the class loaded.
     */
    private static Class defineClass(ClassLoader classLoader, String className, byte[] bytes) {
        return UnsafeMemory.UNSAFE.defineClass(className, bytes, 0, bytes.length, classLoader, null);
    }
}
