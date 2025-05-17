package net.openhft.chronicle.core.internal.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MapUtilTest {

    @Test
    public void entryShouldBeImmutable() {
        Map.Entry<String, String> entry = MapUtil.entry("k", "v");
        assertEquals("k", entry.getKey());
        assertEquals("v", entry.getValue());
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue("new"));
        assertEquals("v", entry.getValue());
    }

    @Test
    public void ofUnmodifiableShouldReturnUnmodifiableMap() {
        Map<String, String> map = MapUtil.ofUnmodifiable(
                MapUtil.entry("a", "1"),
                MapUtil.entry("b", "2"));

        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertThrows(UnsupportedOperationException.class, () -> map.put("c", "3"));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("a"));
    }

    @Test
    public void ofUnmodifiableShouldRejectNullArray() {
        assertThrows(NullPointerException.class,
                () -> MapUtil.ofUnmodifiable((Map.Entry<String, String>[]) null));
    }

    @Test
    public void ofUnmodifiableShouldRejectNullEntries() {
        assertThrows(NullPointerException.class,
                () -> MapUtil.ofUnmodifiable(MapUtil.entry("a", "1"), null));
    }
}
