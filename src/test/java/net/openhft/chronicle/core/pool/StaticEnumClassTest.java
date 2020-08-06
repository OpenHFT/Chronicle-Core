package net.openhft.chronicle.core.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StaticEnumClassTest {
    @Test
    public void testInitialSize() {
        EnumCache<Ecn> ecnEnumCache = EnumCache.of(Ecn.class);
        assertEquals(128, ecnEnumCache.initialSize());
    }
}
