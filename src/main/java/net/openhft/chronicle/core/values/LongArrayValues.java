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

package net.openhft.chronicle.core.values;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * User: peter.lawrey Date: 10/10/13 Time: 07:11
 */
public interface LongArrayValues {
    long getCapacity();

    long getUsed();

    void setMaxUsed(long usedAtLeast);

    long getValueAt(long index) throws BufferUnderflowException;

    void setValueAt(long index, long value) throws IllegalArgumentException, BufferOverflowException;

    long getVolatileValueAt(long index) throws BufferUnderflowException;

    void setOrderedValueAt(long index, long value) throws IllegalArgumentException, BufferOverflowException;

    boolean compareAndSet(long index, long expected, long value) throws IllegalArgumentException, BufferOverflowException;

    void bindValueAt(int index, LongValue value);

    long sizeInBytes(long capacity);

    boolean isNull();

    void reset();
}
