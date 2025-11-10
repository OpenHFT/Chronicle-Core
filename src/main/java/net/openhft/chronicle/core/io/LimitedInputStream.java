//
// Copyright 2016-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.chronicle.core.io;

import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * <p>An {@link InputStream} decorator that enforces an upper bound on the number of bytes
 * that can be consumed from the underlying stream.  Once the budget is exhausted:</p>
 * <ul>
 *   <li>If the wrapped stream is at true end-of-file, {@code -1} is returned (normal EOF).</li>
 *   <li>If additional data is still available, an {@link IOException} with the message
 *       <q>Size limit exceeded</q> is thrown.  This ensures the caller can never read past
 *       the configured limit.</li>
 * </ul>
 *
 * <p>The class is package-private on purpose; use it through public APIs such as
 * {@link Wget} instead of referencing it directly.</p>
 */
final class LimitedInputStream extends FilterInputStream {

    /** Remaining budget in bytes. */
    private long remainingBytes;

    /**
     * Creates a new wrapper.
     *
     * @param in        source stream (non-null)
     * @param maxBytes  maximum number of bytes that may be read &gt;=0
     */
    LimitedInputStream(final InputStream in, final long maxBytes) {
        super(Objects.requireNonNull(in, "in"));
        if (maxBytes < 0)
            throw new IllegalArgumentException("maxBytes must be >= 0");
        this.remainingBytes = maxBytes;
    }

    @Override
    public int read() throws IOException {
        if (remainingBytes == 0) {
            int b = super.read();                      // probe underlying stream
            return (b == -1) ? -1 : throwExceeded();   // EOF vs. overflow
        }
        int b = super.read();
        if (b != -1)
            --remainingBytes;
        return b;
    }

    @Override
    public int read(final byte @NotNull [] buf, final int off, final int len) throws IOException {
        // Classic Java-8 bounds checks
        Objects.requireNonNull(buf, "buffer");
        if (off < 0 || len < 0 || len > buf.length - off)
            throw new IndexOutOfBoundsException();
        if (len == 0)
            return 0;

        if (remainingBytes == 0) {
            int n = super.read(buf, off, 1);           // detect real EOF
            return (n == -1) ? -1 : throwExceeded();
        }

        int allowed = (int) Math.min(len, remainingBytes);
        int n = super.read(buf, off, allowed);
        if (n != -1)
            remainingBytes -= n;
        return n;
    }

    private static int throwExceeded() throws IOException {
        throw new IOException("Size limit exceeded");
    }
}
