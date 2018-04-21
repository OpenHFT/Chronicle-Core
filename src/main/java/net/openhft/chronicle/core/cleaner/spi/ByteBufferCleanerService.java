package net.openhft.chronicle.core.cleaner.spi;

import java.nio.ByteBuffer;

public interface ByteBufferCleanerService {
    int NO_IMPACT = 0;
    int SOME_IMPACT = 1;

    void clean(final ByteBuffer buffer);

    int impact();
}
