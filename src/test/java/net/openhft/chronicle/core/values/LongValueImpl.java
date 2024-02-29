package net.openhft.chronicle.core.values;

import net.openhft.chronicle.core.io.SimpleCloseable;

public class LongValueImpl extends SimpleCloseable implements LongValue {
    private long value = 0;

    @Override
    public long getValue() throws IllegalStateException {
        return value;
    }

    @Override
    public void setValue(long value) throws IllegalStateException {
        this.value = value;
    }

    @Override
    public long addValue(long delta) throws IllegalStateException {
        return value += delta;
    }

    @Override
    public boolean compareAndSwapValue(long expected, long value) throws IllegalStateException {
        if (this.value == expected) {
            this.value = value;
            return true;
        }
        return false;
    }
}
