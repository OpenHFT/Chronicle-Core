package net.openhft.chronicle.core.io;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.CharBuffer;
import java.util.Objects;

public final class Wget {

    /* ───────── Functional seams ───────── */

    @FunctionalInterface
    public interface ConnectionProvider {
        InputStream open(URL url) throws IOException;
    }

    @FunctionalInterface
    public interface CharsetDetector {
        Charset detect(InputStream response, String contentTypeHeader);
    }

    /* ───────── Builder ───────── */

    public static final class Builder {
        // if null, a default provider with timeouts will be created in build()
        private ConnectionProvider connectionProvider = null;
        private CharsetDetector charsetDetector = (in, ct) -> StandardCharsets.UTF_8;
        private int connectTimeoutMs = 10_000;
        private int readTimeoutMs = 10_000;
        private long maxResponseBytes = 10L << 20;   // 10 MiB

        public Builder connectionProvider(ConnectionProvider p) {
            this.connectionProvider = p;
            return this;
        }

        public Builder charsetDetector(CharsetDetector d) {
            this.charsetDetector = d;
            return this;
        }

        public Builder connectTimeoutMs(int v) {
            this.connectTimeoutMs = v;
            return this;
        }

        public Builder readTimeoutMs(int v) {
            this.readTimeoutMs = v;
            return this;
        }

        /**
         * Set the maximum response body size allowed when fetching. The default
         * value is {@code 10 MiB}.
         */
        public Builder maxResponseBytes(long v) {
            if (v < 0)
                throw new IllegalArgumentException("maxResponseBytes must be ≥ 0");
            this.maxResponseBytes = v;
            return this;
        }

        public Wget build() {
            ConnectionProvider cp = this.connectionProvider;
            if (cp == null) {
                final int ct = connectTimeoutMs;
                final int rt = readTimeoutMs;
                cp = url -> {
                    URLConnection uc = url.openConnection();
                    uc.setConnectTimeout(ct);
                    uc.setReadTimeout(rt);
                    if (uc instanceof HttpURLConnection)
                        ((HttpURLConnection) uc).setInstanceFollowRedirects(false);
                    return uc.getInputStream();
                };
            }
            return new Wget(cp, charsetDetector, maxResponseBytes);
        }
    }

    /* ───────── internal ───────── */

    private final ConnectionProvider connectionProvider;
    private final CharsetDetector charsetDetector;
    private final long maxResponseBytes;

    private Wget(ConnectionProvider cp,
                 CharsetDetector cd,
                 long maxBytes) {
        this.connectionProvider = cp;
        this.charsetDetector = cd;
        this.maxResponseBytes = maxBytes;
    }

    /* ───────── static façade ───────── */

    public static void url(String url, StringBuilder sb) throws IOException {
        final int MAX_URL_LENGTH = 2_048;
        if (url.length() > MAX_URL_LENGTH)
            throw new IllegalArgumentException("URL too long (" + url.length() + ')');
        new Builder().build().fetch(url, sb);
    }

    /* ───────── core logic ───────── */

    public void fetch(String url, Appendable out) throws IOException {
        Objects.requireNonNull(out, "out must not be null");

        URL u = new URL(url);
        if (!"http".equals(u.getProtocol()) && !"https".equals(u.getProtocol()))
            throw new MalformedURLException("Only http/https allowed");

        try (InputStream raw = connectionProvider.open(u);
             InputStream limited = new LimitedInputStream(raw, maxResponseBytes)) {

            Charset cs = charsetDetector.detect(raw, null);
            if (cs == null) cs = StandardCharsets.UTF_8;           // ← Java-8 safe fallback

            Reader reader = new BufferedReader(new InputStreamReader(limited, cs));
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1)
                out.append(java.nio.CharBuffer.wrap(buf, 0, n));
        }
    }
}
