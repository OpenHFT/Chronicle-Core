package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.StackTrace;

public interface CloseableTracer extends Closeable {
    StackTrace createdHere();
}
