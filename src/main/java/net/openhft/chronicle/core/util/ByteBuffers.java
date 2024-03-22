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

import net.openhft.chronicle.core.Jvm;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Utility class for working with {@link ByteBuffer} instances.
 * <p>
 * Provides functionality to directly set the address and capacity of a ByteBuffer.
 */
public final class ByteBuffers {
    private ByteBuffers() {
    }

    private static final Field ADDRESS;
    private static final Field CAPACITY;

    static {
        ByteBuffer direct = ByteBuffer.allocateDirect(0);
        ADDRESS = Jvm.getField(direct.getClass(), "address");
        CAPACITY = Jvm.getField(direct.getClass(), "capacity");
    }

    /**
     * Sets the memory address and capacity of a {@link ByteBuffer} instance directly.
     * <p>
     * This method uses reflection to access and modify the address and capacity fields of the ByteBuffer.
     * It should be used with caution as it bypasses the usual safety checks and can lead to undefined behavior
     * if used improperly.
     *
     * @param buffer   the ByteBuffer whose address and capacity are to be set
     * @param address  the memory address to be set
     * @param capacity the capacity to be set
     * @throws AssertionError if the operation fails due to IllegalAccessException or IllegalArgumentException
     */
    public static void setAddressCapacity(ByteBuffer buffer, long address, long capacity) {
        int cap = Math.toIntExact(capacity);
        try {
            ADDRESS.setLong(buffer, address);
            CAPACITY.setInt(buffer, cap);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AssertionError(e);
        }
    }
}
