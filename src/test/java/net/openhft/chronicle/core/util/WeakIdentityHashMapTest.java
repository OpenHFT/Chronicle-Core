package net.openhft.chronicle.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeakIdentityHashMapTest {
    @Test
    public void twoKeys() {
        String a1 = Character.toString('a');
        String a2 = Character.toString('a');
        WeakIdentityHashMap<String, Integer> map = new WeakIdentityHashMap<>();
        map.put(a1, 1);
        map.put(a2, 2);
        assertEquals(2, map.size());
        map.clear();
        assertTrue(map.isEmpty());
    }
}