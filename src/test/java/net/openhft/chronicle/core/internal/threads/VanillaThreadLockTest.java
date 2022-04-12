package net.openhft.chronicle.core.internal.threads;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.threads.InterruptedRuntimeException;
import net.openhft.chronicle.core.values.LongValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

//@Ignore("causes link errors on build server - https://github.com/OpenHFT/Chronicle-Core/issues/326")
public class VanillaThreadLockTest extends CoreTestCommon {

    @Test
    public void gettid() {
        assumeFalse(Jvm.isArm() || OS.isMacOSX());
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 1000);
        assertTrue(lock.gettid() > 0);
        assertTrue(lock.tryLock());
        lock.unlock();
        lock.lock();
        lock.unlock();
    }

    @Test
    public void lockUnlock() {
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 1000);
        lock.lock(1);
        lock.unlock(1);

        assertTrue(lock.tryLock(2));
        assertFalse(lock.tryLock(3));

        lock.unlock(2);

        assertEquals(2L << 32, value.getValue());
    }

    @Test
    public void duplicateLock() {
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 1000);
        lock.lock(1);
        try {
            lock.lock(1);
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
        lock.unlock(1);
        assertEquals(1L << 32, value.getValue());
    }

    @Test
    public void duplicateUnlock() {
        VanillaThreadLock lock = new VanillaThreadLock(new VanillaLongValue(), 1000);
        lock.lock(1);
        lock.unlock(1);
        try {
            lock.unlock(1);
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Lock already unlocked by threadId 1", e.getMessage());
            // expected
        }
    }

    @Test
    public void wrongUnlock() {
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 1000);
        lock.lock(1);

        expectException("Lock held by another thread 1 not mine 2");
        lock.unlock(2);

        lock.unlock(1);
        assertEquals(1L << 32, value.getValue());
    }

    @Test
    public void lockTimeOut() {
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 50);
        lock.lock(-1);

        ignoreException("ThreadId -1 died while holding a lock");
        expectException("Successfully forced an unlock for threadId: 2, previous thread held by: -1, status: "); // dead or unknown
        lock.lock(2);

        lock.unlock(2);

        expectException("Lock previously held by another thread 2 not mine -1");
        lock.unlock(-1);

        assertEquals(2L << 32, value.getValue());
    }

    @Test
    public void lockTimeOut1() {
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 50);
        lock.lock(1);

        ignoreException("ThreadId 1 is running while still holding a lock after ");
        expectException("Successfully forced an unlock for threadId: 2, previous thread held by: 1, status: "); // running or unknown
        lock.lock(2);

        expectException("Lock held by another thread 2 not mine 1");
        lock.unlock(1);

        assertEquals(0x100000002L, value.getValue());
    }

    @Test
    public void raceConditions() {
        CheckingLongValue value = new CheckingLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 100);

        value.getValues.add(0L);
        // changed
        value.getValues.add(2L);
        // unlocked
        value.getValues.add(2L << 32);
        value.getValues.add(2L << 32);

        lock.lock(1);
        assertEquals(0, value.getValues.size());

        assertEquals("200000001", value.setValues.stream().map(Long::toHexString).collect(Collectors.joining(", ")));
        value.setValues.clear();

        value.getValues.add((2L << 32) | 1);
        // forced unlock mid-flight
        value.getValues.add((1L << 32));

        expectException("Failed to unlock");
        lock.unlock(1);

        assertEquals(0, value.getValues.size());
        assertEquals(0, value.setValues.size());
    }

    @Test
    public void raceConditions2() {
        CheckingLongValue value = new CheckingLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 100);
        lock.busyLoopCount = lock.busyLockSlowerCount = 1;

        // tryLock
        final long deadThreadId = 1L << 31;
        ignoreException("ThreadId -2147483648 died while holding a lock");
        value.getValues.add(deadThreadId);
        // busyLoop
        value.getValues.add(deadThreadId);
        // busyLockSlower
        value.getValues.add(deadThreadId);
        if (VanillaThreadLock.METRICS.supportsProc)
            value.getValues.add(deadThreadId);

        value.getValues.add(deadThreadId);

        // OOPS changed at the last nano-second
        ignoreException("Failed to forced an unlock for threadId: 1");
        value.getValues.add(deadThreadId << 32);

        // tryLock
        value.getValues.add(deadThreadId << 32);
        // or maybe not, tryLock fails
        value.getValues.add(deadThreadId);
        // busyLock
        value.getValues.add(deadThreadId);
        // busyLockSlower
        value.getValues.add(deadThreadId);
        if (VanillaThreadLock.METRICS.supportsProc) {
            value.getValues.add(deadThreadId << 32);
        }
        value.getValues.add(deadThreadId << 32);
        value.getValues.add(deadThreadId << 32);

        lock.lock(1);
        assertEquals(0, value.getValues.size());

        assertEquals("8000000000000001", value.setValues.stream().map(Long::toHexString).collect(Collectors.joining(", ")));
    }

    @Test
    public void interrupted() {
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 100);
        final Thread thread = Thread.currentThread();
        lock.lock(2);
        thread.interrupt();
        try {
            lock.lock(1);
            fail("Should have interrupted");
        } catch (InterruptedRuntimeException ire) {
            // expected
        }
        assertTrue(thread.isInterrupted());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidThreadId() {
        final VanillaLongValue value = new VanillaLongValue();
        VanillaThreadLock lock = new VanillaThreadLock(value, 100);
        lock.lock(0);
    }

    static class VanillaLongValue implements LongValue {
        long value;

        @Override
        public long getValue() throws IllegalStateException {
            return value;
        }

        @Override
        public void setValue(long value) throws IllegalStateException {
            this.value = value;
        }

        @Override
        public long getVolatileValue() throws IllegalStateException {
            return getValue();
        }

        @Override
        public void setVolatileValue(long value) throws IllegalStateException {
            setValue(value);
        }

        @Override
        public void setOrderedValue(long value) throws IllegalStateException {
            setValue(value);
        }

        @Override
        public long addValue(long delta) throws IllegalStateException {
            throw new AssertionError();
        }

        @Override
        public long addAtomicValue(long delta) throws IllegalStateException {
            throw new AssertionError();
        }

        @Override
        public boolean compareAndSwapValue(long expected, long value) throws IllegalStateException {
            if (getValue() == expected) {
                setValue(value);
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return getClass().getName() + "@" + Integer.toHexString(hashCode());
        }
    }

    static class CheckingLongValue extends VanillaLongValue {
        List<Long> getValues = new ArrayList<>();
        List<Long> setValues = new ArrayList<>();

        @Override
        public long getValue() throws IllegalStateException {
            return getValues.remove(0);
        }

        @Override
        public void setValue(long value) throws IllegalStateException {
            setValues.add(value);
        }
    }
}