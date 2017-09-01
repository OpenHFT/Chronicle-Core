module net.openhft.chronicle.core {
    requires chronicle.core;
    provides net.openhft.chronicle.core.cleaner.spi.ByteBufferCleanerService
            with net.openhft.chronicle.core.cleaner.impl.jdk9.Jdk9ByteBufferCleanerService;
}