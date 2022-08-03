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

package net.openhft.chronicle.core.values;

/**
 * Ignores attempts to set it and always returns the default unless a default is provided.
 * <p>
 * This can be used instead of setting a LongValue to null.
 */
public class UnsetLongValue implements LongValue {
    private final long value;

    public UnsetLongValue(long value) {
        this.value = value;
    }

    @Override
    public long getValue() throws IllegalStateException {
        return value;
    }

    @Override
    public void setValue(long value) throws IllegalStateException {
        // ignored
    }

    @Override
    public long getVolatileValue(long closedValue) throws IllegalStateException {
        return closedValue;
    }

    @Override
    public long addValue(long delta) throws IllegalStateException {
        return value;
    }

    @Override
    public boolean compareAndSwapValue(long expected, long value) throws IllegalStateException {
        return true;
    }
}
