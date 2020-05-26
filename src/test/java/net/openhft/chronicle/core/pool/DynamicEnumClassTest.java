package net.openhft.chronicle.core.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DynamicEnumClassTest {
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

}