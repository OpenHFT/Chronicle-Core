package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.StackTrace;

public interface CloseableTracer extends Closeable {
    /* to be moved in x.22 to ManagedCloseable*/
    StackTrace createdHere();
}
