/*
 * Copyright 2016-2020 Chronicle Software
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

public interface TwoLongValue extends LongValue {
    long getValue2();

    void setValue2(long value2);

    long getVolatileValue2();

    void setVolatileValue2(long value);

    void setOrderedValue2(long value);

    long addValue2(long delta);

    long addAtomicValue2(long delta);

    boolean compareAndSwapValue2(long expected, long value);

    default void setMaxValue2(long value) {
        for (; ; ) {
            long pos = getVolatileValue2();
            if (pos >= value)
                break;
            if (compareAndSwapValue2(pos, value))
                break;
        }
    }

    default void setMinValue2(long value) {
        for (; ; ) {
            long pos = getVolatileValue2();
            if (pos <= value)
                break;
            if (compareAndSwapValue2(pos, value))
                break;
        }
    }

    default void setValues(long value1, long value2) {
        setValue2(value2);
        setOrderedValue(value1);
    }

    default void getValues(long[] values) {
        long value1 = getVolatileValue();
        long value2 = getValue2();
        while (true) {
            long value1b = getVolatileValue();
            long value2b = getValue2();
            if (value1 == value1b && value2 == value2b) {
                values[0] = value1;
                values[1] = value2;
                return;
            }
            value1 = value1b;
            value2 = value2b;
        }
    }
}
