package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.scoped.ScopedResource;
import net.openhft.chronicle.core.scoped.ScopedResourcePool;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class StringBuilderPoolTest extends CoreTestCommon {

    @Test
    public void buildersAreReusedWhenCapacityExceeded() {
        ScopedResourcePool<StringBuilder> pool = StringBuilderPool.createThreadLocal(2);

        StringBuilder b1;
        StringBuilder b2;
        StringBuilder b3;
        int id1;
        int id2;
        int id3;

        try (ScopedResource<StringBuilder> sr1 = pool.get();
             ScopedResource<StringBuilder> sr2 = pool.get();
             ScopedResource<StringBuilder> sr3 = pool.get()) {
            b1 = sr1.get();
            b2 = sr2.get();
            b3 = sr3.get();
            id1 = System.identityHashCode(b1);
            id2 = System.identityHashCode(b2);
            id3 = System.identityHashCode(b3);

            assertNotSame(b1, b2);
            assertNotSame(b1, b3);
            assertNotSame(b2, b3);

            b1.append("one");
            b2.append("two");
            b3.append("three");
        }

        // After closing all resources the pool should retain only the two oldest builders (b1 and b2)
        Set<Integer> originalIds = new HashSet<>(Arrays.asList(id1, id2));

        try (ScopedResource<StringBuilder> sr4 = pool.get()) {
            StringBuilder b4 = sr4.get();
            int id4 = System.identityHashCode(b4);
            assertEquals(0, b4.length());
            assertTrue(originalIds.contains(id4));
            assertNotEquals(id3, id4);

            try (ScopedResource<StringBuilder> sr5 = pool.get()) {
                StringBuilder b5 = sr5.get();
                int id5 = System.identityHashCode(b5);
                assertEquals(0, b5.length());
                assertTrue(originalIds.contains(id5));
                assertNotEquals(id3, id5);
                assertNotSame(b4, b5);
            }
        }
    }
}
