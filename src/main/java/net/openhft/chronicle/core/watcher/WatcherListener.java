/*
 * Copyright (c) 2016-2019 Chronicle Software Ltd
 */

package net.openhft.chronicle.core.watcher;

public interface WatcherListener {
    /**
     * When a file or directory is added or modified.
     * @param filename of the
     * @param modified false is created, true if modified, null if bootstrapping.
     * @throws IllegalStateException when this listener is no longer valid
     */
    void onExists(String base, String filename, Boolean modified) throws IllegalStateException;

    /**
     * Notify that a file or directory was removed.
     * @param filename removed.
     * @throws IllegalStateException when this listener is no longer valid
     */
    void onRemoved(String base, String filename) throws IllegalStateException;
}
