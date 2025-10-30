package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for the Wget class, which provides a simple way to fetch content from URLs.
 */
class WgetTest {

    @Test
    void fetch_appends_response_body() throws IOException {
        String expected = "hello world";
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)))
                .build();
        StringBuilder sb = new StringBuilder();
        wget.fetch("http://does.not.matter", sb);
        assertEquals(expected, sb.toString());
    }

    @Test
    void invalid_scheme_throws_MalformedURLException() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .build();
        assertThrows(IOException.class, () -> wget.fetch("ftp://example.com", new StringBuilder()));
    }

    @Test
    void null_appendable_throws() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .build();
        assertThrows(NullPointerException.class, () -> wget.fetch("http://x", null));
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
        assertEquals("12345", sb.toString());
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
            assertThrows(IOException.class, () -> wget.fetch("http://x", new StringBuilder()));
        }
    }

    @Test
    void IOException_from_connection_provider_bubbles_up() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> {
                    throw new IOException("boom");
                })
                .build();
        assertThrows(IOException.class, () -> wget.fetch("http://x", new StringBuilder()));
    }

    @Test
    void null_charset_detector_result_falls_back_to_utf8() throws IOException {
        byte[] cafe = "Café".getBytes(StandardCharsets.UTF_8);
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(cafe))
                .charsetDetector((in, ct) -> null)
                .build();
        StringBuilder sb = new StringBuilder();
        wget.fetch("http://x", sb);
        assertEquals("Café", sb.toString());
    }

    @Test
    void appendable_exception_is_propagated() {
        Appendable broken = new Appendable() {
            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException("disk full");
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
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes()))
                .build();
        assertThrows(IOException.class, () -> wget.fetch("http://x", broken));
    }

    @Test
    void static_url_method_rejects_oversized_url() {
        StringBuilder sbUrl = new StringBuilder("http://a");
        while (sbUrl.length() <= 2100) sbUrl.append('b');
        assertThrows(IllegalArgumentException.class, () -> Wget.url(sbUrl.toString(), new StringBuilder()));
    }

    @Test
    void fetch_is_thread_safe_when_instance_is_shared() throws Exception {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("ok".getBytes()))
                .build();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger successes = new AtomicInteger();
        Callable<Void> task = () -> {
            StringBuilder sb = new StringBuilder();
            wget.fetch("http://x", sb);
            if ("ok".contentEquals(sb)) successes.incrementAndGet();
            return null;
        };
        for (int i = 0; i < 20; i++) pool.submit(task);
        pool.shutdown();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
        assertEquals(20, successes.get());
    }

    @Test
    void limited_stream_behaves_like_eof_after_budget() throws IOException {
        byte[] data = "abc".getBytes();
        LimitedInputStream lim = new LimitedInputStream(new ByteArrayInputStream(data), 3);
        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        for (int b; (b = lim.read()) != -1; ) copy.write(b);
        assertArrayEquals(data, copy.toByteArray());
        assertEquals(-1, lim.read());
    }

    @Test
    void zero_budget_allows_empty_body_but_blocks_data() throws IOException {
        Wget empty = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .maxResponseBytes(0)
                .build();
        StringBuilder sb = new StringBuilder();
        empty.fetch("http://x", sb);
        assertEquals("", sb.toString());

        Wget tooMuch = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes()))
                .maxResponseBytes(0)
                .build();
        assertThrows(IOException.class, () -> tooMuch.fetch("http://x", new StringBuilder()));
    }

    @Test
    void negative_budget_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new Wget.Builder().maxResponseBytes(-1).build());
    }

    @Test
    void limited_stream_byte_array_path_respects_limit() throws IOException {
        byte[] data = "abcdef".getBytes(StandardCharsets.UTF_8);
        LimitedInputStream lim = new LimitedInputStream(new ByteArrayInputStream(data), 4);
        byte[] buf = new byte[10];
        int n = lim.read(buf);
        assertEquals(4, n);
        assertEquals("abcd", new String(buf, 0, n, StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> lim.read(buf));
    }

    @Test
    void charset_detector_exception_bubbles_up() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes()))
                .charsetDetector((in, ct) -> {
                    throw new RuntimeException("boom");
                })
                .build();
        assertThrows(RuntimeException.class, () -> wget.fetch("http://x", new StringBuilder()));
    }

    @Test
    void require_public_endpoints_blocks_loopback() {
        Wget wget = new Wget.Builder()
                .requirePublicEndpoints()
                .build();
        assertThrows(MalformedURLException.class, () -> wget.fetch("http://127.0.0.1", new StringBuilder()));
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
        assertEquals(ct, seenConnect.get());
        assertEquals(rt, seenRead.get());
    }
}
