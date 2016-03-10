/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.core.values;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * User: peter.lawrey Date: 10/10/13 Time: 07:11
 */
public interface LongArrayValues {
    long getCapacity();

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
