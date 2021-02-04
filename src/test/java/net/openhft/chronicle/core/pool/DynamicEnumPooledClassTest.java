package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.Maths;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DynamicEnumPooledClassTest {
    @Test
    public void additionalEnum() {
        EnumCache<YesNo> yesNoEnumCache = EnumCache.of(YesNo.class);
        assertEquals(YesNo.Yes, yesNoEnumCache.valueOf("Yes"));
        assertEquals(YesNo.No, yesNoEnumCache.valueOf("No"));
        assertEquals("[Yes, No]", Arrays.toString(yesNoEnumCache.asArray()));

        YesNo maybe = yesNoEnumCache.valueOf("Maybe");
        assertEquals("Maybe", maybe.name());
        assertEquals(2, maybe.ordinal());
        assertEquals("[Yes, No, Maybe]", Arrays.toString(yesNoEnumCache.asArray()));

        YesNo unknown = yesNoEnumCache.valueOf("Unknown");
        assertEquals("Unknown", unknown.name());
        assertEquals(3, unknown.ordinal());
        assertEquals("[Yes, No, Maybe, Unknown]", Arrays.toString(yesNoEnumCache.asArray()));

        // check that asArray returns YesNo instances
        for (YesNo yesNo : yesNoEnumCache.asArray())
            assertEquals(yesNo.name(), yesNo.toString());
    }

    @Test
    public void testInitialSize() throws IllegalArgumentException {
        EnumCache<EcnDynamic> ecnEnumCache = EnumCache.of(EcnDynamic.class);
        assertEquals(32, Maths.nextPower2(ecnEnumCache.size(), 1));
    }
}