package net.openhft.chronicle.core.pool;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class StaticEnumClassGptTest {

    @Test
    public void testType() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        assertEquals(Ecn.class, staticEnumClass.type());
    }

    @Test
    public void testValueOf() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        assertEquals(Ecn.RFX, staticEnumClass.get("RFX"));
        assertEquals(Ecn.RFX, staticEnumClass.valueOf("RFX"));
        assertNull(staticEnumClass.valueOf(null));
        assertNull(staticEnumClass.valueOf(""));
    }

    @Test
    public void testSize() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        assertEquals(21, staticEnumClass.size());
    }

    @Test
    public void testForIndex() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        assertEquals(Ecn.EBS_LIVE_NYK, staticEnumClass.forIndex(0));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testForIndexOutOfBound() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        staticEnumClass.forIndex(21); // Out of bounds
    }

    @Test
    public void testAsArray() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        Ecn[] values = staticEnumClass.asArray();
        assertArrayEquals(Ecn.values(), values);
    }

    @Test
    public void testCreateMap() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        Map<Ecn, String> map = staticEnumClass.createMap();
        assertTrue(map.isEmpty());
        map.put(Ecn.RFX, "test");
        assertEquals("test", map.get(Ecn.RFX));
    }

    @Test
    public void testCreateSet() {
        StaticEnumClass<Ecn> staticEnumClass = new StaticEnumClass<>(Ecn.class);
        Set<Ecn> set = staticEnumClass.createSet();
        assertTrue(set.isEmpty());
        set.add(Ecn.RFX);
        assertTrue(set.contains(Ecn.RFX));
    }
}
