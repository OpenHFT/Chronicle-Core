package net.openhft.chronicle.core.cleaner.spi;

import java.nio.ByteBuffer;

public interface ByteBufferCleanerService {
    int NO_IMPACT = 0;
    int LITTLE_IMPACT = 1;
    int SOME_IMPACT = 2;

    void clean(final ByteBuffer buffer);

    int impact();
}
