/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

package net.openhft.chronicle.core.values;

import net.openhft.chronicle.core.io.Closeable;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * User: peter.lawrey Date: 10/10/13 Time: 07:11
 */
public interface LongArrayValues extends Closeable {
    long getCapacity() throws IllegalStateException;

    long getUsed() throws IllegalStateException, BufferUnderflowException;

    void setMaxUsed(long usedAtLeast) throws IllegalStateException, BufferUnderflowException;

    // throws BufferUnderflowException;
    long getValueAt(long index) throws IllegalStateException, BufferUnderflowException;

    // throws IllegalArgumentException, BufferOverflowException
    void setValueAt(long index, long value) throws IllegalStateException, BufferOverflowException;

    // throws BufferUnderflowException
    long getVolatileValueAt(long index) throws IllegalStateException, BufferUnderflowException;

    //  throws IllegalArgumentException, BufferOverflowException
    void setOrderedValueAt(long index, long value) throws IllegalStateException, BufferOverflowException;

    // throws IllegalArgumentException, BufferOverflowException
    boolean compareAndSet(long index, long expected, long value) throws IllegalStateException, BufferOverflowException;

    default void bindValueAt(int index, LongValue value) throws IllegalStateException, BufferOverflowException {
        bindValueAt((long) index, value);
    }

    void bindValueAt(long index, LongValue value) throws IllegalStateException, BufferOverflowException;

    long sizeInBytes(long capacity) throws IllegalStateException;

    boolean isNull() throws IllegalStateException;

    void reset() throws IllegalStateException;
}
