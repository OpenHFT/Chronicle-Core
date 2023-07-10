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

import net.openhft.affinity.Affinity;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.values.IntArrayValues;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.security.SecureRandom;

@Deprecated(/* to be removed in x.25 */)
public class ThreadIndexAssigner {
    private final IntArrayValues values;

    public ThreadIndexAssigner(IntArrayValues values) {
        this.values = values;
    }

    public int getId() throws IllegalStateException, BufferOverflowException, BufferUnderflowException {
        int threadId = Affinity.getThreadId();
        int size = (int) values.getCapacity();
        values.setMaxUsed(size);
        // already assigned?
        for (int i = 0; i < size; i++) {
            int value = values.getVolatileValueAt(i);
            if (value == threadId)
                return i;
        }

        int index = nextIndex(size);
        for (int i = 0; i < size * 2; i++) {
            int value = values.getVolatileValueAt(index);
            boolean processAlive = Jvm.isProcessAlive(value);
            if ((value == 0 || !processAlive) && (values.compareAndSet(index, value, threadId)))
                    return index;
            index++;
            if (index >= size) {
                Thread.yield();
                index = 0;
            }
        }
        throw new IllegalStateException("Unable to acquire an id as all ids are taken");
    }

    protected int nextIndex(int size) {
        return new SecureRandom().nextInt(size);
    }
}
