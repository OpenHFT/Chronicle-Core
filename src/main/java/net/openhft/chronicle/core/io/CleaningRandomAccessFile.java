/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * {@link RandomAccessFile} that quietly closes itself during finalisation if it
 * has not already been closed. This guards against leaks when a test forgets to
 * close the file, although callers should still use try-with-resources wherever
 * possible.
 * <p>
 * Finalisation is deprecated in recent JDKs so this class is only a safety net
 * for legacy code.
 */
public class CleaningRandomAccessFile extends RandomAccessFile {
    public CleaningRandomAccessFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    public CleaningRandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    /**
     * Best-effort safety net for a file that was not closed explicitly.
     * <p>
     * Finalisation is unreliable and deprecated for removal, but deleting this method before a
     * tested replacement exists would silently remove the fallback close and permit descriptor leaks.
     * See {@code src/main/docs/finalisation-migration.adoc}.
     *
     * @throws Throwable if an error occurs during finalization.
     */
    @SuppressWarnings({"deprecation", "removal", "java:S1113"})
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Closeable.closeQuietly(this);
    }
}
