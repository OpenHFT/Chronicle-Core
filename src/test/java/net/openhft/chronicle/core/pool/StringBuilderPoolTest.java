package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.pool.StringBuilderPool;
import net.openhft.chronicle.core.scoped.ScopedResourcePool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringBuilderPoolTest {

    @Test
    public void testAcquireStringBuilder() {
        StringBuilderPool pool = new StringBuilderPool();
        StringBuilder sb1 = pool.acquireStringBuilder();
        assertNotNull(sb1);
        assertEquals(0, sb1.length());
        assertEquals(128, sb1.capacity());

        sb1.append("test");

        StringBuilder sb2 = pool.acquireStringBuilder();
        assertSame(sb1, sb2);
        assertEquals(0, sb2.length());
    }
}
