/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <h2>Minimal HTTP-GET helper with a hard response size cap enforced</h2>
 *
 * <p>{@code Wget} is intentionally simple and dependency-free.  It opens a connection to
 * an {@code http://} or {@code https://} URL, honours user-defined time-outs, and streams
 * at most <em>N</em> bytes into a supplied {@link Appendable}.  Everything that touches
 * the outside world can be replaced via the {@link Builder} for predictable tests.</p>
 */
public final class Wget {

    private static final int MAX_URL_LENGTH = 2_048;
    private final ConnectionProvider connectionProvider;
    private final CharsetDetector charsetDetector;
    private final long maxResponseBytes;

    private Wget(final ConnectionProvider cp,
                 final CharsetDetector cd,
                 final long maxBytes) {
        this.connectionProvider = cp;
        this.charsetDetector = cd;
        this.maxResponseBytes = maxBytes;
    }

    /**
     * Shortcut that uses the default configuration.
     *
     * @param url address to fetch
     * @param sb  destination buffer
     * @throws IOException if the request fails
     */
    @Deprecated(/* to be removed in 2027, only used in tests */)
    public static void url(final String url, final StringBuilder sb) throws IOException {
        if (url.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("URL too long (" + url.length() + ")");
        }
        new Builder().build().fetch(url, sb);
    }

    /**
     * Retrieves the content of the given URL into the provided {@link Appendable}.
     *
     * @param url address to fetch
     * @param out destination to append content
     * @throws IOException on I/O errors or unsupported schemes
     */
    @SuppressWarnings("deprecation")
    public void fetch(final String url, final Appendable out) throws IOException {
        Objects.requireNonNull(out, "out");

        final URL u;
        try {
            u = URI.create(url).toURL();
        } catch (IllegalArgumentException e) {
            MalformedURLException exception = new MalformedURLException("Invalid URL: " + url);
            exception.initCause(e);
            throw exception;
        }
        final String scheme = u.getProtocol();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new MalformedURLException("Only http/https allowed, not " + scheme);
        }

        try (InputStream raw = connectionProvider.open(u);
             InputStream limited = new LimitedInputStream(raw, maxResponseBytes)) {

            Charset cs = charsetDetector.detect(raw, null);
            if (cs == null) {
                cs = StandardCharsets.UTF_8;
            }

            try (Reader reader = new BufferedReader(new InputStreamReader(limited, cs))) {
                for (int ch; (ch = reader.read()) != -1; ) {
                    out.append((char) ch);
                }
            }
        }
    }

    /**
     * Opens a (potentially mocked) connection for the given URL.
     */
    @FunctionalInterface
    public interface ConnectionProvider {
        /**
         * Opens a stream for the provided URL.
         *
         * @param url endpoint to open
         * @return input stream for the response body
         * @throws IOException if the connection fails
         */
        InputStream open(URL url) throws IOException;
    }

    /**
     * Decides which charset to use when decoding the response body.
     */
    @FunctionalInterface
    public interface CharsetDetector {
        /**
         * Detects the charset based on the response and headers.
         *
         * @param response          response body stream
         * @param contentTypeHeader content type header if present
         * @return detected charset or {@code null} to fall back to default
         */
        Charset detect(InputStream response, String contentTypeHeader);
    }

    /**
     * Configures {@link Wget} instances, primarily for tests where timeouts or providers are injected.
     */
    public static final class Builder {
        private static final ConnectionProvider DEFAULT_PROVIDER = URL::openStream;

        private ConnectionProvider connectionProvider = DEFAULT_PROVIDER;
        private CharsetDetector charsetDetector = (in, ct) -> StandardCharsets.UTF_8;
        private int connectTimeoutMs = 10_000;
        private int readTimeoutMs = 10_000;
        private long maxResponseBytes = 10L << 20; // 10 MiB

        /**
         * Overrides the connection provider for testing.
         *
         * @param provider supplier for URL connections
         * @return this builder
         */
        @Deprecated(/* to be removed in 2027, only used in tests */)
        public Builder connectionProvider(final ConnectionProvider provider) {
            this.connectionProvider = Objects.requireNonNull(provider);
            return this;
        }

        /**
         * Overrides the charset detector for testing.
         *
         * @param detector charset detector to use
         * @return this builder
         */
        @Deprecated(/* to be removed in 2027, only used in tests */)
        public Builder charsetDetector(final CharsetDetector detector) {
            this.charsetDetector = Objects.requireNonNull(detector);
            return this;
        }

        /**
         * Sets the connection timeout in milliseconds.
         *
         * @param timeoutMs timeout in milliseconds
         * @return this builder
         */
        @Deprecated(/* to be removed in 2027, only used in tests */)
        public Builder connectTimeoutMs(final int timeoutMs) {
            this.connectTimeoutMs = timeoutMs;
            return this;
        }

        /**
         * Sets the read timeout in milliseconds for HTTP connections.
         *
         * @param timeoutMs timeout in milliseconds
         * @return this builder
         */
        @Deprecated(/* to be removed in 2027, only used in tests */)
        public Builder readTimeoutMs(final int timeoutMs) {
            this.readTimeoutMs = timeoutMs;
            return this;
        }

        /**
         * Sets the maximum allowed response size in bytes (non-negative).
         *
         * @param maxResponseBytes maximum allowed bytes
         * @return this builder
         */
        @Deprecated(/* to be removed in 2027, only used in tests */)
        public Builder maxResponseBytes(final long maxResponseBytes) {
            if (maxResponseBytes < 0) {
                throw new IllegalArgumentException("maxResponseBytes must be >= 0");
            }
            this.maxResponseBytes = maxResponseBytes;
            return this;
        }

        /**
         * Creates a {@link Wget} with defaults or caller-supplied overrides.
         *
         * @return configured {@link Wget} instance
         */
        public Wget build() {
            ConnectionProvider cp = this.connectionProvider;
            if (cp == DEFAULT_PROVIDER) {                      // wrap default provider to apply time-outs
                final int ct = connectTimeoutMs;
                final int rt = readTimeoutMs;
                cp = url -> {
                    URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(ct);
                    conn.setReadTimeout(rt);
                    if (conn instanceof HttpURLConnection) {
                        ((HttpURLConnection) conn).setInstanceFollowRedirects(false);
                    }
                    return conn.getInputStream();
                };
            }
            return new Wget(cp, charsetDetector, maxResponseBytes);
        }
    }
}
