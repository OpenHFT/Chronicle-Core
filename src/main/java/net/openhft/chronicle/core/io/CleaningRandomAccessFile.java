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
     * Retained safety net: a best-effort attempt to close the file if the owner forgot to.
     * <p>
     * Kept deliberately even though {@code finalize()} is deprecated for removal (JEP&nbsp;421) and is
     * never guaranteed to run: an unclosed {@code RandomAccessFile} leaks a file descriptor, and this
     * net is the last line of defence against that leak in production code paths that skipped an
     * explicit close. Happy-path tests close explicitly and pass without it, but the leak it guards is
     * exactly the case tests do not exercise. When {@code finalize()} is finally removed this must be
     * re-homed on {@link java.lang.ref.Cleaner} to keep the descriptor-leak protection, not dropped.
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
