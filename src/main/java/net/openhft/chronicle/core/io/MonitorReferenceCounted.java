package net.openhft.chronicle.core.io;

public interface MonitorReferenceCounted extends ReferenceCountedTracer {
    void unmonitored(boolean unmonitored);

    boolean unmonitored();
}
