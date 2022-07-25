package net.openhft.chronicle.core.io;

/**
 * Pass a hint that now would be a good time to sync to underlying media if supported.
 */
public interface Syncable {
    /**
     * Perform a sync up to the point that this handle has read or written. There might be data beyond this point that isn't sync-ed.
     * It might not do anything depending on whether this is supported or turned off through configuration.
     */
    void sync();

    static void syncIfAvailable(Object o) {
        if (o instanceof Syncable)
            ((Syncable) o).sync();
    }
}
