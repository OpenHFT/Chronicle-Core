package net.openhft.chronicle.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <h2>Minimal HTTP‑GET helper with a hard response cap</h2>
 *
 * <p>{@code Wget} is intentionally simple and dependency‑free.  It opens a connection to
 * an {@code http://} or {@code https://} URL, honours user‑defined time‑outs, and streams
 * at most <em>N</em> bytes into a supplied {@link Appendable}.  Everything that touches
 * the outside world can be replaced via the {@link Builder} for predictable tests.</p>
 */
public final class Wget {

    /** Opens a (potentially mocked) connection for the given URL. */
    @FunctionalInterface
    public interface ConnectionProvider { InputStream open(URL url) throws IOException; }

    /** Decides which charset to use when decoding the response body. */
    @FunctionalInterface
    public interface CharsetDetector   { Charset detect(InputStream response, String contentTypeHeader); }

    public static final class Builder {
        private static final ConnectionProvider DEFAULT_PROVIDER = URL::openStream;

        private ConnectionProvider connectionProvider = DEFAULT_PROVIDER;
        private CharsetDetector    charsetDetector    = (in, ct) -> StandardCharsets.UTF_8;
        private int  connectTimeoutMs = 10_000;
        private int  readTimeoutMs    = 10_000;
        private long maxResponseBytes = 10L << 20; // 10 MiB

        public Builder connectionProvider(final ConnectionProvider p) { this.connectionProvider = Objects.requireNonNull(p); return this; }
        public Builder charsetDetector   (final CharsetDetector d)    { this.charsetDetector    = Objects.requireNonNull(d); return this; }
        public Builder connectTimeoutMs  (final int v)                { this.connectTimeoutMs   = v; return this; }
        public Builder readTimeoutMs     (final int v)                { this.readTimeoutMs      = v; return this; }
        public Builder maxResponseBytes  (final long v)               { if (v < 0) throw new IllegalArgumentException("maxResponseBytes must be ≥ 0"); this.maxResponseBytes = v; return this; }

        /** Creates a {@link Wget} with defaults or caller‑supplied overrides. */
        public Wget build() {
            ConnectionProvider cp = this.connectionProvider;
            if (cp == DEFAULT_PROVIDER) {                      // wrap default provider to apply time‑outs
                final int ct = connectTimeoutMs;
                final int rt = readTimeoutMs;
                cp = url -> {
                    URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(ct);
                    conn.setReadTimeout(rt);
                    if (conn instanceof HttpURLConnection)
                        ((HttpURLConnection) conn).setInstanceFollowRedirects(false);
                    return conn.getInputStream();
                };
            }
            return new Wget(cp, charsetDetector, maxResponseBytes);
        }
    }

    private final ConnectionProvider connectionProvider;
    private final CharsetDetector    charsetDetector;
    private final long               maxResponseBytes;

    private Wget(final ConnectionProvider cp,
                 final CharsetDetector cd,
                 final long maxBytes) {
        this.connectionProvider = cp;
        this.charsetDetector    = cd;
        this.maxResponseBytes   = maxBytes;
    }

    private static final int MAX_URL_LENGTH = 2_048;

    /** Shortcut that uses the default configuration. */
    public static void url(final String url, final StringBuilder sb) throws IOException {
        if (url.length() > MAX_URL_LENGTH)
            throw new IllegalArgumentException("URL too long (" + url.length() + ")");
        new Builder().build().fetch(url, sb);
    }

    public void fetch(final String url, final Appendable out) throws IOException {
        Objects.requireNonNull(out, "out");

        final URL u = new URL(url);
        final String scheme = u.getProtocol();
        if (!"http".equals(scheme) && !"https".equals(scheme))
            throw new MalformedURLException("Only http/https allowed, not " + scheme);

        try (InputStream raw     = connectionProvider.open(u);
             InputStream limited = new LimitedInputStream(raw, maxResponseBytes)) {

            Charset cs = charsetDetector.detect(raw, null);
            if (cs == null) cs = StandardCharsets.UTF_8;

            Reader reader = new BufferedReader(new InputStreamReader(limited, cs));
            for (int ch; (ch = reader.read()) != -1; )
                out.append((char) ch);
        }
    }
}
