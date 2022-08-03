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

package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.UnsafeMemory;
import sun.misc.Unsafe;

public abstract class UnsafeCloseable extends AbstractCloseable {
    protected long address;
    protected Unsafe unsafe = null;

    protected UnsafeCloseable() {
        singleThreadedCheckDisabled(true);
    }

    protected void address(long address) {
        this.address = address;
        unsafe = UnsafeMemory.UNSAFE;
    }

    @Override
    protected void performClose() throws IllegalStateException {
        unsafe = null;
    }

    public long getLong() throws IllegalStateException {
        try {
            return unsafe.getLong(address);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    public void setLong(long value) throws IllegalStateException {
        try {
            unsafe.putLong(address, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    public long getVolatileLong() throws IllegalStateException {
        try {
            return unsafe.getLongVolatile(null, address);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    public void setVolatileLong(long value) throws IllegalStateException {
        try {
            unsafe.putLongVolatile(null, address, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    public long getVolatileLong(long closedLong) {
        if (isClosed())
            return closedLong;
        try {
            return getVolatileLong();
        } catch (Exception e) {
            return closedLong;
        }
    }

    public void setOrderedLong(long value) throws IllegalStateException {
        try {
            unsafe.putOrderedLong(null, address, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    public long addLong(long delta) throws IllegalStateException {
        try {
            return unsafe.getAndAddLong(null, address, delta) + delta;
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    public long addAtomicLong(long delta) throws IllegalStateException {
        try {
            return addLong(delta);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }

    public boolean compareAndSwapLong(long expected, long value) throws IllegalStateException {
        try {
            return unsafe.compareAndSwapLong(null, address, expected, value);
        } catch (NullPointerException e) {
            throwExceptionIfClosed();
            throw e;
        }
    }
}
