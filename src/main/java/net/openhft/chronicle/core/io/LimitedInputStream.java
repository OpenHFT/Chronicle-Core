package net.openhft.chronicle.core.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Hard-limit wrapper for an {@link InputStream}.
 * <p>When the byte budget is exhausted it:
 * <ul>
 *   <li>returns {@code -1} (EOF) if the underlying stream is also at EOF;</li>
 *   <li>otherwise throws {@link IOException} “Size limit exceeded”.</li>
 * </ul>
 */
final class LimitedInputStream extends FilterInputStream {
    private long remaining;

    LimitedInputStream(InputStream in, long maxBytes) {
        super(in);
        this.remaining = maxBytes;
    }

    /* ----------------------------------------------------- read() ---- */

    @Override
    public int read() throws IOException {
        if (remaining == 0) {
            int b = super.read();               // peek source
            if (b == -1) return -1;             // true EOF
            throwExceeded();
        }
        int b = super.read();
        if (b != -1) remaining--;
        return b;
    }

    /* ------------------------------------------- read(byte[], …) ---- */

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        // --- Java-8 style bounds checks ---------------------------------
        if (buf == null)
            throw new NullPointerException("buffer");
        if (off < 0 || len < 0 || len > buf.length - off)
            throw new IndexOutOfBoundsException();
        // ----------------------------------------------------------------

        if (len == 0) return 0;

        if (remaining == 0) {
            int n = super.read(buf, off, 1);   // probe for real EOF
            if (n == -1) return -1;
            throwExceeded();
        }

        int allowed = (int) Math.min(len, remaining);
        int n = super.read(buf, off, allowed);
        if (n != -1) remaining -= n;
        return n;
    }

    /* ----------------------------------------------------- helpers -- */

    private static int throwExceeded() throws IOException {
        throw new IOException("Size limit exceeded");
    }
}
