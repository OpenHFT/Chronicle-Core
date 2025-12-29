/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Wget class, which provides a simple way to fetch content from URLs.
 */
@SuppressWarnings("deprecation")
class WgetTest {

    private static java.nio.charset.Charset nullDetector(InputStream in, String contentType) {
        return null;
    }

    private static java.nio.charset.Charset throwingDetector(InputStream in, String contentType) {
        throw new RuntimeException("charset detector failure occurred");
    }

    @DisplayName("fetch appends response body to appendable")
    @Test
    void fetch_appends_response_body() throws IOException {
        String expected = "hello world";
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)))
                .build();
        StringBuilder sb = new StringBuilder();
        wget.fetch("http://does.not.matter", sb);
        assertEquals(expected, sb.toString(), "Response body should be appended to StringBuilder");
    }

    @DisplayName("invalid scheme rejects non-http URL requests")
    @Test
    void invalid_scheme_throws_MalformedURLException() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .build();
        assertThrows(IOException.class, () -> wget.fetch("ftp://example.com", new StringBuilder()),
                "fetch should reject unsupported URL schemes");
    }

    @DisplayName("null appendable throws NullPointerException as expected behaviour under expected input and output conditions")
    @Test
    void null_appendable_throws() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .build();
        assertThrows(NullPointerException.class, () -> wget.fetch("http://x", null),
                "fetch should throw for null appendable");
    }

    @DisplayName("body equal to limit is allowed")
    @Test
    void body_equal_to_limit_is_allowed() throws IOException {
        byte[] five = "12345".getBytes(StandardCharsets.UTF_8);
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(five))
                .maxResponseBytes(5)
                .build();
        StringBuilder sb = new StringBuilder();
        wget.fetch("http://x", sb);
        assertEquals("12345", sb.toString(), "Response body exactly matching maxResponseBytes limit should be allowed");
    }

    @DisplayName("unknown content length limit is enforced")
    @Test
    void unknown_content_length_still_enforced() throws IOException {
        try (InputStream neverEnding = new InputStream() {
            @Override
            public int read() {
                return 'A';
            }
        }) {
            Wget wget = new Wget.Builder()
                    .connectionProvider(u -> neverEnding)
                    .maxResponseBytes(128)
                    .build();
            assertThrows(IOException.class, () -> wget.fetch("http://x", new StringBuilder()),
                    "fetch should enforce limits when content length is unknown");
        }
    }

    @DisplayName("connection provider IOException bubbles up unchanged")
    @Test
    void ioExceptionFromConnectionProviderBubblesUp() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> {
                    throw new IOException("connection provider failure occurred");
                })
                .build();
        assertThrows(IOException.class, () -> wget.fetch("http://x", new StringBuilder()),
                "connection provider IOException should bubble up");
    }

    @DisplayName("null charset detector falls back to UTF-8")
    @Test
    void null_charset_detector_result_falls_back_to_utf8() throws IOException {
        byte[] cafe = "Café".getBytes(StandardCharsets.UTF_8);
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(cafe))
                .charsetDetector(WgetTest::nullDetector)
                .build();
        StringBuilder sb = new StringBuilder();
        wget.fetch("http://x", sb);
        assertEquals("Café", sb.toString(), "Null charset detector result should fall back to UTF-8 for decoding");
    }

    @DisplayName("appendable exception is propagated to caller")
    @Test
    void appendable_exception_is_propagated() {
        Appendable broken = new Appendable() {
            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException("disk full while appending response");
            }

            @Override
            public Appendable append(CharSequence csq) {
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int s, int e) {
                return this;
            }
        };
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)))
                .build();
        assertThrows(IOException.class, () -> wget.fetch("http://x", broken),
                "appendable IOException should propagate from fetch");
    }

    @DisplayName("static url method rejects oversized URL")
    @Test
    void static_url_method_rejects_oversized_url() {
        StringBuilder sbUrl = new StringBuilder("http://a");
        while (sbUrl.length() <= 2100) sbUrl.append('b');
        assertThrows(IllegalArgumentException.class, () -> Wget.url(sbUrl.toString(), new StringBuilder()),
                "Wget.url should reject oversized URL inputs");
    }

    @DisplayName("fetch is thread safe when shared instance")
    @Test
    void fetch_is_thread_safe_when_instance_is_shared() throws Exception {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("ok".getBytes(StandardCharsets.UTF_8)))
                .build();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger successes = new AtomicInteger();
        Callable<Void> task = () -> {
            StringBuilder sb = new StringBuilder();
            wget.fetch("http://x", sb);
            if ("ok".contentEquals(sb)) successes.incrementAndGet();
            return null;
        };
        java.util.List<java.util.concurrent.Future<Void>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futures.add(pool.submit(task));
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS), "Thread pool should terminate within 2 seconds when tasks complete");
        for (java.util.concurrent.Future<Void> future : futures) {
            future.get();
        }
        assertEquals(20, successes.get(), "All 20 concurrent fetch operations should complete successfully when sharing a single Wget instance");
    }

    @DisplayName("limited stream behaves like EOF after budget")
    @Test
    void limited_stream_behaves_like_eof_after_budget() throws IOException {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        LimitedInputStream lim = new LimitedInputStream(new ByteArrayInputStream(data), 3);
        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        for (int b; (b = lim.read()) != -1; ) copy.write(b);
        assertArrayEquals(data, copy.toByteArray(), "LimitedInputStream should read all bytes up to the specified budget");
        assertEquals(-1, lim.read(), "LimitedInputStream should return -1 (EOF) after exhausting the byte budget");
    }

    @DisplayName("zero budget allows empty body but blocks data")
    @Test
    void zero_budget_allows_empty_body_but_blocks_data() throws IOException {
        Wget empty = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .maxResponseBytes(0)
                .build();
        StringBuilder sb = new StringBuilder();
        empty.fetch("http://x", sb);
        assertEquals("", sb.toString(), "Zero byte budget should allow empty response bodies without throwing");

        Wget tooMuch = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)))
                .maxResponseBytes(0)
                .build();
        assertThrows(IOException.class, () -> tooMuch.fetch("http://x", new StringBuilder()),
                "zero budget should reject non-empty responses");
    }

    @DisplayName("negative byte budget is rejected safely")
    @Test
    void negative_budget_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new Wget.Builder().maxResponseBytes(-1).build(),
                "negative budget should be rejected by builder");
    }

    @DisplayName("limited stream byte array path respects limit")
    @Test
    void limited_stream_byte_array_path_respects_limit() throws IOException {
        byte[] data = "abcdef".getBytes(StandardCharsets.UTF_8);
        LimitedInputStream lim = new LimitedInputStream(new ByteArrayInputStream(data), 4);
        byte[] buf = new byte[10];
        int n = lim.read(buf);
        assertEquals(4, n, "LimitedInputStream should read exactly 4 bytes when limit is 4");
        assertEquals("abcd", new String(buf, 0, n, StandardCharsets.UTF_8), "LimitedInputStream byte array read should respect the byte limit and return only the first 4 bytes");
        assertThrows(IOException.class, () -> lim.read(buf),
                "LimitedInputStream should throw once byte limit is exceeded");
    }

    @DisplayName("charset detector exception bubbles up unchanged")
    @Test
    void charset_detector_exception_bubbles_up() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)))
                .charsetDetector(WgetTest::throwingDetector)
                .build();
        assertThrows(RuntimeException.class, () -> wget.fetch("http://x", new StringBuilder()),
                "charset detector exception should bubble up");
    }

    @DisplayName("default connection provider applies timeouts correctly")
    @Test
    void default_provider_sets_timeouts() throws Exception {
        AtomicInteger seenConnect = new AtomicInteger(-1);
        AtomicInteger seenRead = new AtomicInteger(-1);

        URL synthetic = new URL(null, "http://timeout.test", new java.net.URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return new URLConnection(u) {
                    @Override
                    public void connect() {
                    }

                    @Override
                    public void setConnectTimeout(int v) {
                        seenConnect.set(v);
                    }

                    @Override
                    public void setReadTimeout(int v) {
                        seenRead.set(v);
                    }

                    @Override
                    public InputStream getInputStream() {
                        return new ByteArrayInputStream(new byte[0]);
                    }
                };
            }
        });

        int ct = 1_234, rt = 5_678;
        Wget wget = new Wget.Builder().connectTimeoutMs(ct).readTimeoutMs(rt).build();
        java.lang.reflect.Field f = Wget.class.getDeclaredField("connectionProvider");
        f.setAccessible(true);
        Wget.ConnectionProvider provider = (Wget.ConnectionProvider) f.get(wget);
        provider.open(synthetic).close();
        assertEquals(ct, seenConnect.get(), "Default connection provider should apply the configured connect timeout");
        assertEquals(rt, seenRead.get(), "Default connection provider should apply the configured read timeout");
    }
}
