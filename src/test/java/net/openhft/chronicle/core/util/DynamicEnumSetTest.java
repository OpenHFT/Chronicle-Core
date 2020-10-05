package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.pool.EnumCache;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;

public class DynamicEnumSetTest {
/*
    public static void main(String[] args) {
        for (int i = 0; i < 256; i++) {
            System.out.print(Integer.toHexString(0xe00 + i) + ", ");
        }
    }
*/

    @Test
    public void test0() {
        doTest(Test0.class, false);
    }

    @Test
    public void test0b() {
        doTest(Test0b.class, true);
    }

    @Test
    public void test63() {
        doTest(Test63.class, false);
    }

    @Test
    public void test255() {
        doTest(Test255.class, false);
    }

    private <E extends CoreDynamicEnum<E>> void doTest(Class<E> testClass, boolean flip) {
        EnumCache<E> cache = EnumCache.of(testClass);
        int size = cache.size();
        DynamicEnumSet<E> set;
        if (flip)
            set = DynamicEnumSet.complementOf(DynamicEnumSet.noneOf(testClass));
        else
            set = DynamicEnumSet.allOf(testClass);
        doTest2(testClass, set);
    }

    private <E extends CoreDynamicEnum<E>> void doTest2(Class testClass, Set<E> set) {
        EnumCache<E> cache = EnumCache.of(testClass);
        int size = cache.size();
        assertEquals(size, set.size());
        assertEquals(EnumSet.allOf(testClass).toString(),
                set.toString());
        DynamicEnumSet<E> set2 = DynamicEnumSet.complementOf(set);
        assertEquals(0, set2.size());

        E one = cache.valueOf("one");
        assertEquals(size + 1, set.size());
        assertTrue(set.contains(one));
        set.remove(one);

        assertEquals(0, set2.size());

        assertEquals(size, set.size());
        E two = cache.valueOf("two");
        assertEquals(size + 1, set.size());
        assertTrue(set.contains(two));
        set.remove(two);

//        assertNotNull(set.toArray());
        assertNotNull(set.toString());
    }

    @Test
    public void removeIf() {
        DynamicEnumSet<Test63b> set = DynamicEnumSet.allOf(Test63b.class);
        EnumCache<Test63b> cache = EnumCache.of(Test63b.class);
        cache.valueOf("E64");
        cache.valueOf("E65");
        cache.valueOf("E66");
        cache.valueOf("E67");
        set.removeIf(e -> e.ordinal() % 2 == 0);
        set.removeIf(e -> e.ordinal() % 3 == 0);
        assertEquals("[e01, e05, e07, e0b, e0d, e11, e13, e17, e19, e1d, e1f, e23, e25, e29, e2b, e2f, e31, e35, e37, e3b, e3d, E66]", set.toString());
    }

    enum Test0 implements CoreDynamicEnum<Test0> {
    }

    enum Test0b implements CoreDynamicEnum<Test0b> {
    }

    enum Test63b implements CoreDynamicEnum<Test63b> {
        e00, e01, e02, e03, e04, e05, e06, e07, e08, e09, e0a, e0b, e0c, e0d, e0e, e0f,
        e10, e11, e12, e13, e14, e15, e16, e17, e18, e19, e1a, e1b, e1c, e1d, e1e, e1f,
        e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e2a, e2b, e2c, e2d, e2e, e2f,
        e30, e31, e32, e33, e34, e35, e36, e37, e38, e39, e3a, e3b, e3c, e3d, e3e
    }

    enum Test63 implements CoreDynamicEnum<Test63> {
        e00, e01, e02, e03, e04, e05, e06, e07, e08, e09, e0a, e0b, e0c, e0d, e0e, e0f,
        e10, e11, e12, e13, e14, e15, e16, e17, e18, e19, e1a, e1b, e1c, e1d, e1e, e1f,
        e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e2a, e2b, e2c, e2d, e2e, e2f,
        e30, e31, e32, e33, e34, e35, e36, e37, e38, e39, e3a, e3b, e3c, e3d, e3e
    }

    enum Test255 implements CoreDynamicEnum<Test255> {
        e00, e01, e02, e03, e04, e05, e06, e07, e08, e09, e0a, e0b, e0c, e0d, e0e, e0f,
        e10, e11, e12, e13, e14, e15, e16, e17, e18, e19, e1a, e1b, e1c, e1d, e1e, e1f,
        e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e2a, e2b, e2c, e2d, e2e, e2f,
        e30, e31, e32, e33, e34, e35, e36, e37, e38, e39, e3a, e3b, e3c, e3d, e3e, e3f,
        e40, e41, e42, e43, e44, e45, e46, e47, e48, e49, e4a, e4b, e4c, e4d, e4e, e4f,
        e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e5a, e5b, e5c, e5d, e5e, e5f,
        e60, e61, e62, e63, e64, e65, e66, e67, e68, e69, e6a, e6b, e6c, e6d, e6e, e6f,
        e70, e71, e72, e73, e74, e75, e76, e77, e78, e79, e7a, e7b, e7c, e7d, e7e, e7f,
        e80, e81, e82, e83, e84, e85, e86, e87, e88, e89, e8a, e8b, e8c, e8d, e8e, e8f,
        e90, e91, e92, e93, e94, e95, e96, e97, e98, e99, e9a, e9b, e9c, e9d, e9e, e9f,
        ea0, ea1, ea2, ea3, ea4, ea5, ea6, ea7, ea8, ea9, eaa, eab, eac, ead, eae, eaf,
        eb0, eb1, eb2, eb3, eb4, eb5, eb6, eb7, eb8, eb9, eba, ebb, ebc, ebd, ebe, ebf,
        ec0, ec1, ec2, ec3, ec4, ec5, ec6, ec7, ec8, ec9, eca, ecb, ecc, ecd, ece, ecf,
        ed0, ed1, ed2, ed3, ed4, ed5, ed6, ed7, ed8, ed9, eda, edb, edc, edd, ede, edf,
        ee0, ee1, ee2, ee3, ee4, ee5, ee6, ee7, ee8, ee9, eea, eeb, eec, eed, eee, eef,
        ef0, ef1, ef2, ef3, ef4, ef5, ef6, ef7, ef8, ef9, efa, efb, efc, efd, efe, eff,
    }
}
