/*
 * Copyright 2016-2020 chronicle.software
 *
 * https://chronicle.software
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

package net.openhft.chronicle.core.values;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.io.Closeable;

public interface LongValue extends Closeable {
    long getValue() throws IllegalStateException;

    void setValue(long value) throws IllegalStateException;

    long getVolatileValue() throws IllegalStateException;

    void setVolatileValue(long value) throws IllegalStateException;

    /**
     * Value to return if the underlying resource isn't available.
     */
    default long getVolatileValue(long closedValue) throws IllegalStateException {
        return getVolatileValue();
    }

    void setOrderedValue(long value) throws IllegalStateException;

    long addValue(long delta) throws IllegalStateException;

    long addAtomicValue(long delta) throws IllegalStateException;

    boolean compareAndSwapValue(long expected, long value) throws IllegalStateException;

    default void setMaxValue(long value) throws IllegalStateException {
        for (; ; ) {
            long pos = getVolatileValue();
            if (pos >= value)
                break;
            if (compareAndSwapValue(pos, value))
                break;
            Jvm.nanoPause();
        }
    }

    default void setMinValue(long value) throws IllegalStateException {
        for (; ; ) {
            long pos = getVolatileValue();
            if (pos <= value)
                break;
            if (compareAndSwapValue(pos, value))
                break;
            Jvm.nanoPause();
        }
    }

    @Override
    default boolean isClosed() {
        return false;
    }

    @Override
    default void close() {
    }
}
