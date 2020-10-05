package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.pool.EnumCache;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class DynamicEnumMapTest {
    static <T extends CoreDynamicEnum<T>> void bash(Class<T> enumClass) {
        T[] universe = EnumCache.of(enumClass).asArray();

        int numItr = 100;

        // Linked List test
        for (int i = 0; i < numItr; i++) {
            int mapSize = universe.length * 7 / 8;

            // Build the linked list
            Map<T, T> m = new DynamicEnumMap<>(enumClass);
            if (!m.isEmpty())
                fail("New instance non empty.");
            T[] perm = universe.clone();
            Collections.shuffle(Arrays.asList(perm));
            T head = perm[0];
            for (int j = 0; j < mapSize; j++)
                m.put(perm[j], perm[j + 1]);
            T nil = perm[mapSize];

            if (m.size() != mapSize)
                fail("Size not as expected.");

            Map<T, T> tm = new TreeMap<>(m);

            if (m.hashCode() != tm.hashCode())
                fail("Incorrect hashCode computation.");
            if (!m.toString().equals(tm.toString()))
                fail("Incorrect toString computation.");
            if (!tm.equals(m))
                fail("Incorrect equals (1).");
            if (!m.equals(tm))
                fail("Incorrect equals (2).");

            Map<T, T> m2 = new DynamicEnumMap<>(enumClass);
            m2.putAll(m);
            m2.values().removeAll(m.keySet());
            if (m2.size() != 1 || !m2.containsValue(nil))
                fail("Collection views test failed.");

            int j = 0;
            while (head != nil) {
                if (!m.containsKey(head))
                    fail("Linked list doesn't contain a link.");
                T newHead = m.get(head);
                if (newHead == null)
                    fail("Could not retrieve a link.");
                m.remove(head);
                head = newHead;
                j++;
            }
            if (!m.isEmpty())
                fail("Map nonempty after removing all links.");
            if (j != mapSize)
                fail("Linked list size not as expected.");
        }

        DynamicEnumMap<T, T> m = new DynamicEnumMap<>(enumClass);
        for (int i = 0; i < universe.length; i += 2) {
            if (m.put(universe[i], universe[i]) != null)
                fail("put returns a non-null value erroneously.");
        }
        for (int i = 0; i < universe.length; i++)
            if (m.containsValue(universe[i]) != (i % 2 == 0))
                fail("contains value " + i);
        if (m.put(universe[0], universe[0]) == null)
            fail("put returns a null value erroneously.");

        Map<T, T> m2 = m.clone();
        cloneTest(m, m2);

        m2 = new DynamicEnumMap<>(enumClass);
        m2.putAll(m);
        cloneTest(m, m2);

        if (!m.equals(m2))
            fail("Clone not equal to original. (1)");
        if (!m2.equals(m))
            fail("Clone not equal to original. (2)");

        Set<Map.Entry<T, T>> s = m.entrySet(), s2 = m2.entrySet();

        if (!s.equals(s2))
            fail("Clone not equal to original. (3)");
        if (!s2.equals(s))
            fail("Clone not equal to original. (4)");
        if (!s.containsAll(s2))
            fail("Original doesn't contain clone!");
        if (!s2.containsAll(s))
            fail("Clone doesn't contain original!");

        s2.removeAll(s);
        if (!m2.isEmpty()) {
            System.out.println(m2.size());
            System.out.println(m2);
            fail("entrySet().removeAll failed.");
        }

        m2.putAll(m);
        m2.clear();
        assertTrue("clear failed.", m2.isEmpty());

        for (Iterator<Map.Entry<T, T>> i = m.entrySet().iterator(); i.hasNext(); ) {
            i.next();
            i.remove();
        }
        assertTrue("Iterator.remove() failed", m2.isEmpty());
    }

    // Done inefficiently so as to exercise various functions
    static <K, V> void cloneTest(Map<K, V> m, Map<K, V> clone) {
        if (!m.equals(clone))
            fail("Map not equal to copy.");
        if (!clone.equals(m))
            fail("Copy not equal to map.");
        if (!m.entrySet().containsAll(clone.entrySet()))
            fail("Set does not contain copy.");
        if (!clone.entrySet().containsAll(m.entrySet()))
            fail("Copy does not contain set.");
        if (!m.entrySet().equals(clone.entrySet()))
            fail("Set not equal clone set");
        if (!clone.entrySet().equals(m.entrySet()))
            fail("Clone set not equal set");
    }

    @Test
    public void main3() {
        bash(Silly3.class);
    }

    @Test
    public void main16() {
        bash(Silly16.class);
    }

    @Test
    public void main31() {
        bash(Silly31.class);
    }

    @Test
    public void main32() {
        bash(Silly32.class);
    }

    @Test
    public void main33() {
        bash(Silly33.class);
    }

    @Test
    public void main63() {
        bash(Silly63.class);
    }

    @Test
    public void main64() {
        bash(Silly64.class);
    }

    @Test
    public void main65() {
        bash(Silly65.class);
    }

    @Test
    public void main127() {
        bash(Silly127.class);
    }

    @Test
    public void main128() {
        bash(Silly128.class);
    }

    @Test
    public void main129() {
        bash(Silly129.class);
    }

    @Test
    public void main500() {
        bash(Silly500.class);
    }

    @Test
    public void distinctEntrySet() {
        Map<TestEnum, String> enumMap = new DynamicEnumMap<>(TestEnum.class);

        for (TestEnum e : TestEnum.values()) {
            enumMap.put(e, e.name());
        }

        Set<Map.Entry<TestEnum, String>> entrySet = enumMap.entrySet();
        Set<Map.Entry<TestEnum, String>> hashSet = new LinkedHashSet<>(entrySet);

        assertEquals(hashSet, entrySet);
        assertArrayEquals(hashSet.toArray(), entrySet.toArray());
        assertEquals(hashSet.toString(), entrySet.toString());
        assertEquals(hashSet.hashCode(), entrySet.hashCode());
    }

    @Test
    public void dynamicTest() {
        Map<DTestEnum, String> map1 = new DynamicEnumMap<>(DTestEnum.class);
        map1.put(DTestEnum.e00, "zero");
        Map<DTestEnum, String> map2 = new DynamicEnumMap<>(DTestEnum.class);
        map2.put(DTestEnum.e01, "one");
        map2.put(EnumCache.of(DTestEnum.class).valueOf("e03"), "three");
        assertEquals("[e00]", map1.keySet().toString());
        assertEquals("[e01, e03]", map2.keySet().toString());
        map1.putAll(map2);
        assertEquals("[e00=zero, e01=one, e03=three]", map1.entrySet().toString());
        assertEquals("[zero, one, three]", map1.values().toString());
    }

    @Test
    public void dynamicClassTest() {
        Map<DTestClass, String> map1 = new DynamicEnumMap<>(DTestClass.class);
        map1.put(DTestClass.e00, "zero");
        Map<DTestClass, String> map2 = new DynamicEnumMap<>(DTestClass.class);
        map2.put(DTestClass.e01, "one");
        map2.put(EnumCache.of(DTestClass.class).valueOf("e03"), "three");
        assertEquals("[e00]", map1.keySet().toString());
        assertEquals("[e01, e03]", map2.keySet().toString());
        map1.putAll(map2);
        assertEquals("[e00=zero, e01=one, e03=three]", map1.entrySet().toString());
        assertEquals("[zero, one, three]", map1.values().toString());
    }

    enum TestEnum implements CoreDynamicEnum<TestEnum> {e00, e01, e02}

    enum DTestEnum implements CoreDynamicEnum<DTestEnum> {e00, e01, e02}

    static class DTestClass implements CoreDynamicEnum<DTestClass> {
        static final DTestClass e00 = new DTestClass("e00", 0);
        static final DTestClass e01 = new DTestClass("e01", 1);
        static final DTestClass e02 = new DTestClass("e02", 2);
        final String name;
        final int ordinal;

        public DTestClass(String name, int ordinal) {
            this.name = name;
            this.ordinal = ordinal;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int ordinal() {
            return ordinal;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
