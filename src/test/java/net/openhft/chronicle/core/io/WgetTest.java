package net.openhft.chronicle.core.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class WgetTest {

    /* -------------------------------------------------
     * Helpers
     * ------------------------------------------------- */

    /** Returns a Wget instance whose “HTTP response” is a fixed byte array. */
    private static Wget stubbed(String body) {
        return new Wget.Builder()
                .connectionProvider(u ->
                        new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    /* -------------------------------------------------
     * Happy-path behaviour
     * ------------------------------------------------- */

    @Test
    void fetch_appends_response_body() throws IOException {
        StringBuilder sb = new StringBuilder();

        stubbed("hello world").fetch("http://does.not.matter", sb);

        assertEquals("hello world", sb.toString());
    }

    /* -------------------------------------------------
     * CharsetDetector is consulted
     * ------------------------------------------------- */

    @Test
    void custom_charset_detector_is_used() throws IOException {
        // Body encoded in ISO-8859-1 so the final byte sequence is different to UTF-8.
        String original = "Café";
        byte[] isoBytes = original.getBytes("ISO-8859-1");

        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(isoBytes))
                .charsetDetector((in, ct) -> java.nio.charset.StandardCharsets.ISO_8859_1)
                .build();

        StringBuilder sb = new StringBuilder();
        wget.fetch("http://anything", sb);

        assertEquals(original, sb.toString());
    }

    /* -------------------------------------------------
     * Size limits
     * ------------------------------------------------- */

    @Test
    void oversized_body_triggers_IOException() {
        byte[] tenBytes = new byte[10];

        Wget wget = new Wget.Builder()
                .connectionProvider(u -> new ByteArrayInputStream(tenBytes))
                .maxResponseBytes(5)          // ridiculously small
                .build();

        assertThrows(IOException.class,
                () -> wget.fetch("http://x", new StringBuilder()),
                "Size limit exceeded");
    }

    /* -------------------------------------------------
     * URL validation
     * ------------------------------------------------- */

    @Test
    void invalid_scheme_throws_MalformedURLException() {
        Wget wget = stubbed("");     // CP will never be called

        assertThrows(MalformedURLException.class,
                () -> wget.fetch("ftp://example.com", new StringBuilder()));
    }

    /* -------------------------------------------------
     * Null-safety
     * ------------------------------------------------- */

    @Test
    void null_appendable_throws_NullPointerException() {
        Wget wget = stubbed("");

        assertThrows(NullPointerException.class,
                () -> wget.fetch("http://x", null));
    }

    /* -------------------------------------------------
     * Collaborator interaction
     * ------------------------------------------------- */

    @Test
    void connectionProvider_receives_the_same_URL() throws IOException {
        AtomicReference<URL> seen = new AtomicReference<>();

        Wget wget = new Wget.Builder()
                .connectionProvider(u -> {
                    seen.set(u);
                    return new ByteArrayInputStream("ok".getBytes(StandardCharsets.UTF_8));
                })
                .build();

        wget.fetch("http://example.com/resource", new StringBuilder());

        assertEquals("http://example.com/resource", seen.get().toString());
    }
}
