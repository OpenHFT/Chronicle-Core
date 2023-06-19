package net.openhft.chronicle.core.pool;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringBuilderPoolTest {

    @Test
    public void acquireStringBuilderTest() {
        // Initialize the StringBuilderPool
        StringBuilderPool pool = new StringBuilderPool();

        // Acquire a StringBuilder and append a string
        StringBuilder sb = pool.acquireStringBuilder();
        sb.append("Hello, World!");

        // Verify that the StringBuilder contains the correct string
        assertEquals("Hello, World!", sb.toString());

        // Acquire the same StringBuilder again (since it's the same thread)
        StringBuilder sb2 = pool.acquireStringBuilder();

        // Since the StringBuilder is reset upon acquisition, the StringBuilder from the pool should be empty
        assertEquals(0, sb2.length());

        // Confirm that both StringBuilder variables are pointing to the same instance
        assertTrue(sb == sb2);
    }
}
