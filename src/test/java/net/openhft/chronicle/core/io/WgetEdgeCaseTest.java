package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class WgetEdgeCaseTest {

    /* -------------------------------------------------
     * 1. Body exactly == limit  → should succeed
     * ------------------------------------------------- */
    @Test
    void body_equal_to_limit_is_allowed() throws IOException {
        byte[] five = "12345".getBytes(StandardCharsets.UTF_8);

        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(five))
                .maxResponseBytes(5)            // exactly the length
                .build();

        StringBuilder sb = new StringBuilder();
        wget.fetch("http://x", sb);

        assertEquals("12345", sb.toString());
    }

    /* -------------------------------------------------
     * 2. Body advertised as -1 (unknown), but stream too long
     *    — LimitedInputStream must still stop us.
     * ------------------------------------------------- */
    @Test
    void unknown_content_length_still_enforced() {
        InputStream neverEnding = new InputStream() {
            @Override public int read() { return 'A'; }  // never returns –1
        };

        Wget wget = new Wget.Builder()
                .connectionProvider(u -> neverEnding)
                .maxResponseBytes(128)          // tiny budget
                .build();

        assertThrows(IOException.class,
                () -> wget.fetch("http://x", new StringBuilder()),
                "Size limit exceeded");
    }

    /* -------------------------------------------------
     * 3. ConnectionProvider itself fails
     * ------------------------------------------------- */
    @Test
    void IOException_from_connection_provider_bubbles_up() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> { throw new IOException("boom"); })
                .build();

        assertThrows(IOException.class,
                () -> wget.fetch("http://x", new StringBuilder()));
    }

    /* -------------------------------------------------
     * 4. CharsetDetector returns null – must default to UTF-8
     * ------------------------------------------------- */
    @Test
    void null_charset_detector_result_falls_back_to_utf8() throws IOException {
        byte[] cafe = "Café".getBytes(StandardCharsets.UTF_8);

        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(cafe))
                .charsetDetector((in, ct) -> null)   // malicious / buggy detector
                .build();

        StringBuilder sb = new StringBuilder();
        wget.fetch("http://x", sb);

        assertEquals("Café", sb.toString());
    }

    /* -------------------------------------------------
     * 5. Appendable throws – should propagate, not swallow
     * ------------------------------------------------- */
    @Test
    void appendable_exception_is_propagated() {
        Appendable broken = new Appendable() {
            @Override public Appendable append(char c) throws IOException {   // ← throw here
                throw new IOException("disk full");
            }
            @Override public Appendable append(CharSequence csq)              { return this; }
            @Override public Appendable append(CharSequence csq,int s,int e)  { return this; }
        };

        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes()))
                .build();

        assertThrows(IOException.class,
                () -> wget.fetch("http://x", broken));
    }

    /* -------------------------------------------------
     * 6. Extremely long URL (>2 048 chars) hits static facade
     * ------------------------------------------------- */
    @Test
    void static_url_method_rejects_oversized_url() {
        StringBuilder veryLong = new StringBuilder("http://a");
        while (veryLong.length() <= 2100) veryLong.append('b');

        assertThrows(IllegalArgumentException.class,
                () -> Wget.url(veryLong.toString(), new StringBuilder()));
    }

    /* -------------------------------------------------
     * 7. Same Wget instance reused concurrently
     * ------------------------------------------------- */
    @Test
    void fetch_is_thread_safe_when_instance_is_shared() throws Exception {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("ok".getBytes()))
                .build();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger ok = new AtomicInteger();

        Callable<Void> task = () -> {
            StringBuilder sb = new StringBuilder();
            wget.fetch("http://x", sb);
            if ("ok".contentEquals(sb)) ok.incrementAndGet();
            return null;
        };

        for (int i = 0; i < 20; i++) pool.submit(task);
        pool.shutdown();
        assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));
        assertEquals(20, ok.get());
    }

    /* -------------------------------------------------
     * 8. LimitedInputStream must return -1 after limit,
     *    allowing callers that rely on EOF instead of exception.
     * ------------------------------------------------- */
    @Test
    void limited_stream_behaves_like_eof_after_budget() throws IOException {
        byte[] data = "abc".getBytes();
        LimitedInputStream lim =
                new LimitedInputStream(new ByteArrayInputStream(data), 3);

        ByteArrayOutputStream copy = new ByteArrayOutputStream();
        for (int b; (b = lim.read()) != -1; ) copy.write(b);

        assertArrayEquals(data, copy.toByteArray());
        assertEquals(-1, lim.read());           // second read() must now signal EOF
    }

    /* -------------------------------------------------
     * 9. Zero-byte budget:
     *    – empty body is OK;
     *    – any data triggers IOException.
     * ------------------------------------------------- */
    @Test
    void zero_budget_allows_empty_body_but_blocks_data() throws IOException {
        // (a) empty body → fine
        Wget emptyOk = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(new byte[0]))
                .maxResponseBytes(0)
                .build();
        StringBuilder sb = new StringBuilder();
        emptyOk.fetch("http://x", sb);
        assertEquals("", sb.toString());

        // (b) non-empty body → fail
        Wget shouldFail = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes()))
                .maxResponseBytes(0)
                .build();
        assertThrows(IOException.class,
                () -> shouldFail.fetch("http://x", new StringBuilder()),
                "Size limit exceeded");
    }

    /* 10. negative budget – builder must reject */
    @Test
    void negative_budget_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Wget.Builder().maxResponseBytes(-1).build());
    }

    /* 11. byte-array read honours limit and then fails */
    @Test
    void limited_stream_byte_array_path_respects_limit() throws IOException {
        byte[] data = "abcdef".getBytes(StandardCharsets.UTF_8);

        LimitedInputStream lim =
                new LimitedInputStream(new ByteArrayInputStream(data), 4);

        byte[] buf = new byte[10];
        int n = lim.read(buf);                   // first read
        assertEquals(4, n);
        assertEquals("abcd", new String(buf, 0, n, StandardCharsets.UTF_8));

        /* next read must throw because more data is still present */
        assertThrows(IOException.class, () -> lim.read(buf));
    }


    /* -------------------------------------------------
     * 12. CharsetDetector throwing is propagated.
     * ------------------------------------------------- */
    @Test
    void charset_detector_exception_bubbles_up() {
        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream("x".getBytes()))
                .charsetDetector((in, ct) -> { throw new RuntimeException("boom"); })
                .build();

        assertThrows(RuntimeException.class,
                () -> wget.fetch("http://x", new StringBuilder()),
                "boom");
    }

    /* -------------------------------------------------
     * 13. connectTimeoutMs / readTimeoutMs are honoured
     *     by the default ConnectionProvider.
     * ------------------------------------------------- */
    @Test
    void default_provider_sets_timeouts() throws Exception {

        /* we'll capture the actual values the provider applies */
        AtomicInteger seenConnect = new AtomicInteger(-1);
        AtomicInteger seenRead    = new AtomicInteger(-1);

        /* a URL with an in-JVM handler that just records the two calls */
        URL url = new URL(null, "http://timeout.test",
                new java.net.URLStreamHandler() {
                    @Override protected URLConnection openConnection(URL u) {
                        return new URLConnection(u) {
                            @Override public void connect() { /* no real I/O */ }

                            @Override public void setConnectTimeout(int v) {
                                seenConnect.set(v);
                            }
                            @Override public void setReadTimeout(int v) {
                                seenRead.set(v);
                            }
                            @Override public InputStream getInputStream() {
                                return new ByteArrayInputStream(new byte[0]);
                            }
                        };
                    }
                });

        int connect = 1_234;
        int read    = 5_678;

        /* build Wget with the desired time-outs */
        Wget wget = new Wget.Builder()
                .connectTimeoutMs(connect)
                .readTimeoutMs(read)
                .build();

        /* ---- fetch the private ConnectionProvider via reflection -------- */
        java.lang.reflect.Field f = Wget.class.getDeclaredField("connectionProvider");
        f.setAccessible(true);
        Wget.ConnectionProvider provider = (Wget.ConnectionProvider) f.get(wget);
        /* ----------------------------------------------------------------- */

        /* open the stream once – that's enough to trigger the setters */
        provider.open(url).close();

        assertEquals(connect, seenConnect.get(), "connectTimeoutMs not applied");
        assertEquals(read,    seenRead.get(),    "readTimeoutMs not applied");
    }

}
