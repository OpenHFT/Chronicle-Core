package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.UnsafeMemory;
import sun.misc.Unsafe;

public abstract class UnsafeCloseable extends AbstractCloseable {
    protected long address;
    protected Unsafe unsafe = null;

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

    @Override
    protected boolean threadSafetyCheck(boolean isUsed) {
        return true;
    }

}
