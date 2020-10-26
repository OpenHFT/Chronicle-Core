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
public interface IntArrayValues extends Closeable {
    long getCapacity();

    long getUsed();

    void setMaxUsed(long usedAtLeast);

    int getValueAt(long index) throws BufferUnderflowException;

    void setValueAt(long index, int value) throws IllegalArgumentException, BufferOverflowException;

    int getVolatileValueAt(long index) throws BufferUnderflowException;

    void setOrderedValueAt(long index, int value) throws IllegalArgumentException, BufferOverflowException;

    boolean compareAndSet(long index, int expected, int value) throws IllegalArgumentException, BufferOverflowException;

    void bindValueAt(long index, IntValue value);

    long sizeInBytes(long capacity);

    boolean isNull();

    void reset();
}
