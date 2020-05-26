package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.StackTrace;

import static net.openhft.chronicle.core.UnsafeMemory.UNSAFE;

public abstract class AbstractCloseable implements Closeable {
    private static final long CLOSED_OFFSET;

    static {
        try {
            CLOSED_OFFSET = UNSAFE.objectFieldOffset(AbstractCloseable.class.getField("closed"));

        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private transient volatile int closed = 0;
    private transient volatile StackTrace createdHere;
    private transient volatile StackTrace closedHere;

    protected AbstractCloseable() {
        createdHere = Jvm.isResourceTracing() ? new StackTrace("Created Here") : null;
    }

    /**
     * Close a resource so it cannot be used again.
     */
    @Override
    public final void close() {
        if (UNSAFE.getAndSetInt(this, CLOSED_OFFSET, 1) != 0) {
            return;
        }
        closedHere = Jvm.isResourceTracing() ? new StackTrace("Closed here") : null;
        performClose();
    }

    /**
     * Called when a resources needs to be open to use it.
     */
    protected void throwExceptionIfClosed() {
        if (isClosed())
            throw new IllegalStateException("Closed", closedHere);
    }

    /**
     * Called from finalise() implementations.
     */
    protected void warnIfNotClosed() {
        if (!isClosed()) {
            Jvm.warn().on(getClass(), "Discarded without closing", createdHere);
            close();
        }
    }

    /**
     * Call close() to ensure this is called exactly once.
     */
    protected abstract void performClose();

    @Override
    public boolean isClosed() {
        return closed != 0;
    }
}
