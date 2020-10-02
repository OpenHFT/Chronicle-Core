package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Maths;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DynamicEnumPooledClassTest {
    @Test
    public void additionalEnum() {
        EnumCache<YesNo> yesNoEnumCache = EnumCache.of(YesNo.class);
        assertEquals(YesNo.Yes, yesNoEnumCache.valueOf("Yes"));
        assertEquals(YesNo.No, yesNoEnumCache.valueOf("No"));

        YesNo maybe = yesNoEnumCache.valueOf("Maybe");
        assertEquals("Maybe", maybe.name());
        assertEquals(2, maybe.ordinal());

        YesNo unknown = yesNoEnumCache.valueOf("Unknown");
        assertEquals("Unknown", unknown.name());
        assertEquals(3, unknown.ordinal());
    }

    @Test
    public void testInitialSize() {
        EnumCache<EcnDynamic> ecnEnumCache = EnumCache.of(EcnDynamic.class);
        assertEquals(32, Maths.nextPower2(ecnEnumCache.size(), 1));
    }
}