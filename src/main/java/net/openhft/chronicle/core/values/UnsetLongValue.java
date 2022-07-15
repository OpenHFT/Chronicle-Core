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
