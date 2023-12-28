package net.openhft.chronicle.core.io;

import net.openhft.chronicle.core.io.Wget;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class WgetTest {

    @Test
    public void testHttpGetRequest() throws IOException {
        String testUrl = "http://example.com";
        StringBuilder sb = new StringBuilder();

        Wget.url(testUrl, sb);

        assertFalse(sb.toString().isEmpty());
    }

    @Test
    public void testDeprecatedMethod() throws IOException {
        String testUrl = "http://example.com";

        CharSequence result = Wget.url(testUrl);

        assertNotNull(result);
        assertFalse(result.toString().isEmpty());
    }

    @Test
    public void testHttpGetRequestWithInvalidUrl() {
        String invalidUrl = "http://invalid.url";
        StringBuilder sb = new StringBuilder();

        assertThrows(IOException.class, () -> Wget.url(invalidUrl, sb));
    }
}
