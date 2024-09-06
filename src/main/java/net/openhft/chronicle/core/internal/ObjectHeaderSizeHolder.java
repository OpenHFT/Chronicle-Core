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

import net.openhft.chronicle.core.UnsafeMemory;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
/**
 * Provides methods to obtain the size of the object header and the base offset for arrays in the JVM.
 * <p>
 * The object header size is essential for low-level memory operations, and this class uses the {@link Unsafe} API
 * to calculate these values. The object header size is typically platform and JVM-specific, making this class
 * useful for applications requiring direct memory manipulation or optimization.
 * </p>
 * <p>
 * The class is not intended to be instantiated and provides static methods for accessing object header size
 * information.
 * </p>
 */
public final class ObjectHeaderSizeHolder {

    // The size of the object header, initialized at runtime.
    private static final int OBJECT_HEADER_SIZE;

    static {
        try {
            // Retrieve the offset of the first field in this class to calculate the object header size.
            final Field aField = ObjectHeaderSizeHolder.class.getDeclaredField("firstField");
            OBJECT_HEADER_SIZE = (int) UnsafeMemory.INSTANCE.getFieldOffset(aField);
        } catch (NoSuchFieldException e) {
            // If the field is not found, throw an assertion error.
            throw new AssertionError(e);
        }
    }

    @UsedViaReflection
    int firstField; // Dummy field used to calculate the object header size.

    // Private constructor to prevent instantiation of this utility class.
    private ObjectHeaderSizeHolder() {
    }

    /**
     * Returns the size of the object header in the JVM.
     * <p>
     * This size is calculated based on the offset of the first field of this class, and reflects the platform-specific
     * size of an object's internal header (e.g., metadata about the object like its class pointer and other information).
     * </p>
     *
     * @return The size of the object header in bytes.
     */
    public static int getSize() {
        return OBJECT_HEADER_SIZE;
    }

    /**
     * Calculates the object header size for a given class type.
     * <p>
     * If the provided class is an array type, this method returns the base offset of the array in memory.
     * Otherwise, it returns the size of the object header. This distinction is necessary because arrays have
     * additional memory layout requirements compared to regular objects.
     * </p>
     *
     * @param type The class for which the object header size is to be calculated.
     * @return The object header size in bytes, or the base offset for arrays.
     */
    public static int objectHeaderSize(Class<?> type) {
        // Return array base offset for array types, object header size for non-arrays.
        return type.isArray() ? UnsafeMemory.UNSAFE.arrayBaseOffset(type) : OBJECT_HEADER_SIZE;
    }
}
