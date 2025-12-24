/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.io;

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
        throw new RuntimeException("charset detector threw exception");
    }

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

    @Test
    void invalid_scheme_throws_MalformedURLException() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .build();
        assertThrows(IOException.class, () -> wget.fetch("ftp://example.com", new StringBuilder()),
                "fetch should reject unsupported scheme");
    }

    @Test
    void null_appendable_throws() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .build();
        assertThrows(NullPointerException.class, () -> wget.fetch("http://x", null),
                "fetch should reject null appendable");
    }

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
                    "fetch should enforce max response bytes");
        }
    }

    @Test
    void ioExceptionFromConnectionProviderBubblesUp() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> {
                    throw new IOException("connection provider threw exception");
                })
                .build();
        assertThrows(IOException.class, () -> wget.fetch("http://x", new StringBuilder()),
                "fetch should propagate connection provider exception");
    }

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

    @Test
    void appendable_exception_is_propagated() {
        Appendable broken = new Appendable() {
            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException("disk is full now");
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
                "fetch should propagate appendable failure");
    }

    @Test
    void static_url_method_rejects_oversized_url() {
        StringBuilder sbUrl = new StringBuilder("http://a");
        while (sbUrl.length() <= 2100) sbUrl.append('b');
        assertThrows(IllegalArgumentException.class, () -> Wget.url(sbUrl.toString(), new StringBuilder()),
                "url should reject oversized input");
    }

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

    @Test
    void limited_stream_behaves_like_eof_after_budget() throws IOException {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        LimitedInputStream lim = new LimitedInputStream(new ByteArrayInputStream(data), 3);
        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        for (int b; (b = lim.read()) != -1; ) copy.write(b);
        assertArrayEquals(data, copy.toByteArray(), "limited stream reads full budget");
        assertEquals(-1, lim.read(), "budget end yields eof");
    }

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
                "fetch should reject body over zero budget");
    }

    @Test
    void negative_budget_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new Wget.Builder().maxResponseBytes(-1).build(),
                "maxResponseBytes should reject negative value");
    }

    @Test
    void limited_stream_byte_array_path_respects_limit() throws IOException {
        byte[] data = "abcdef".getBytes(StandardCharsets.UTF_8);
        LimitedInputStream lim = new LimitedInputStream(new ByteArrayInputStream(data), 4);
        byte[] buf = new byte[10];
        int n = lim.read(buf);
        assertEquals(4, n, "byte array read meets limit");
        assertEquals("abcd", new String(buf, 0, n, StandardCharsets.UTF_8), "buffer keeps first four");
        assertThrows(IOException.class, () -> lim.read(buf),
                "limited stream should enforce byte limit");
    }

    @Test
    void charset_detector_exception_bubbles_up() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)))
                .charsetDetector(WgetTest::throwingDetector)
                .build();
        assertThrows(RuntimeException.class, () -> wget.fetch("http://x", new StringBuilder()),
                "charset detector failure should propagate");
    }

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
                        // No-op: placeholder method
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
