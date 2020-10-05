package net.openhft.chronicle.core.util;/*
 * @test
 * @bug     4904135 4923181
 * @summary Unit test for DynamicEnumSet
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @author  Yo Ma Ma
 * @key randomness
 */

import net.openhft.chronicle.core.pool.EnumCache;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DynamicEnumSetBashTest {
    static Random rnd = new Random(1);

    static <T extends CoreDynamicEnum<T>> void bash(Class<T> enumClass) {
        bash2(enumClass, () -> DynamicEnumSet.noneOf(enumClass));
    }

    static <T extends CoreDynamicEnum<T>> void bash2(Class<T> enumClass, Supplier<Set<T>> supplier) {
        CoreDynamicEnum[] universe = EnumCache.of(enumClass).asArray();
        rnd.setSeed(universe.length);
        int len1 = universe.length + 1;
        int numItr = Math.min(len1 * len1, 1000);

        for (int i = 0; i < numItr; i++) {
            Set<T> s1 = supplier.get();
            Set<T> s2 = clone(s1, enumClass);
            addRandoms(s1, universe);
            addRandoms(s2, universe);

            Set<T> intersection = clone(s1, enumClass);
            intersection.retainAll(s2);
            Set<T> diff1 = clone(s1, enumClass);
            diff1.removeAll(s2);
            Set<T> diff2 = clone(s2, enumClass);
            diff2.removeAll(s1);
            Set<T> union = clone(s1, enumClass);
            union.addAll(s2);

            if (diff1.removeAll(diff2))
                fail("Set algebra identity 2 failed");
            if (diff1.removeAll(intersection))
                fail("Set algebra identity 3 failed");
            if (diff2.removeAll(diff1))
                fail("Set algebra identity 4 failed");
            if (diff2.removeAll(intersection))
                fail("Set algebra identity 5 failed");
            if (intersection.removeAll(diff1))
                fail("Set algebra identity 6 failed");
            if (intersection.removeAll(diff1))
                fail("Set algebra identity 7 failed");

            intersection.addAll(diff1);
            intersection.addAll(diff2);
            assertEquals("i: " + i, intersection.toString(), union.toString());
            if (!intersection.equals(union))
                fail("Set algebra identity 1 failed at i: " + i);

            if (new HashSet<T>(union).hashCode() != union.hashCode())
                fail("Incorrect hashCode computation.");

            Iterator e = union.iterator();
            while (e.hasNext())
                if (!intersection.remove(e.next()))
                    fail("Couldn't remove element from copy.");
            if (!intersection.isEmpty())
                fail("Copy nonempty after deleting all elements.");

            e = union.iterator();
            while (e.hasNext()) {
                Object o = e.next();
                if (!union.contains(o))
                    fail("Set doesn't contain one of its elements.");
                e.remove();
                if (union.contains(o))
                    fail("Set contains element after deletion.");
            }
            if (!union.isEmpty())
                fail("Set nonempty after deleting all elements.");

            s1.clear();
            if (!s1.isEmpty())
                fail("Set nonempty after clear.");
        }
    }

    // Done inefficiently so as to exercise various functions
    static <E extends CoreDynamicEnum<E>> Set<E> clone(Set<E> s, Class<E> cl) {
        DynamicEnumSet<E> clone = null;
        int method = rnd.nextInt(4);
        switch (method) {
            case 0:
                if (s instanceof DynamicEnumSet) {
                    clone = ((DynamicEnumSet<E>) s).clone();
                    break;
                }
                // fall through.
            case 1:
                clone = DynamicEnumSet.noneOf(cl);
                Collection arrayList = Arrays.asList(s.toArray());
                clone.addAll((Collection<E>) arrayList);
                break;
            case 2:
                if (s.isEmpty())
                    clone = DynamicEnumSet.noneOf(cl);
                else
                    clone = DynamicEnumSet.copyOf((Collection<E>) s);
                break;
            case 3:
                if (s.isEmpty())
                    clone = DynamicEnumSet.noneOf(cl);
                else
                    clone = DynamicEnumSet.copyOf(
                            (Collection<E>) (Collection)
                                    Arrays.asList(s.toArray()));
                break;
        }
        if (!s.equals(clone))
            fail("Set not equal to copy. " + method);
        if (!s.containsAll(clone))
            fail("Set does not contain copy. " + method);
        if (!clone.containsAll(s))
            fail("Copy does not contain set. " + method);
        if (clone.size() != s.size())
            fail("Size doesn't match. " + method);
        return clone;
    }

    static <T extends CoreDynamicEnum<T>> void addRandoms(Set<T> s, CoreDynamicEnum[] universe) {
        int size = 0;
        for (int i = 0; i < universe.length * 2 / 3; i++) {
            T e = (T) universe[rnd.nextInt(universe.length)];

            boolean prePresent = s.contains(e);
            int preSize = s.size();
            boolean added = s.add(e);
            if (!s.contains(e))
                fail("Element not present after addition for " + e);
            if (added == prePresent)
                fail("added == alreadyPresent for " + e);
            int postSize = s.size();
            if (added && preSize == postSize)
                fail("Add returned true, but size didn't change for " + e);
            if (!added && preSize != postSize)
                fail("Add returned false, but size changed for " + e);
            if (size > postSize)
                fail("Set shrank from " + size + " to " + postSize + " added=" + added);
            size = postSize;
        }
    }

    static <T extends Enum<T>> void testRange(T e0, T e1) {
        EnumSet<T> range = EnumSet.range(e0, e1);
        if (range.size() != e1.ordinal() - e0.ordinal() + 1)
            throw new RuntimeException(range.size() + " != " +
                    (e1.ordinal() - e0.ordinal() + 1));
    }

    @Test
    public void testMap63() {
        bash2(DynamicEnumSetTest.Test63.class, () ->
                Collections.newSetFromMap(new DynamicEnumMap<>(DynamicEnumSetTest.Test63.class)));
    }

    @Test
    public void testMap255() {
        bash2(DynamicEnumSetTest.Test255.class, () ->
                Collections.newSetFromMap(new DynamicEnumMap<>(DynamicEnumSetTest.Test255.class)));
    }

    @Test
    public void main0() {
        bash(Silly0.class);
    }

    @Test
    public void main1() {
        bash(Silly1.class);
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
    public void interatorRemove65() {
        final Set<Silly65> set = EnumSet.noneOf(Silly65.class);

        set.add(Silly65.e20);
        set.add(Silly65.e42);

        final Iterator<Silly65> iterator = set.iterator();

        int size = set.size();
        Silly65 element = iterator.next();

        iterator.remove();
        checkSetAfterRemoval(set, size, element);

        size = set.size();
        element = iterator.next();

        set.remove(element);
        checkSetAfterRemoval(set, size, element);

        // The Java API declares that the behaviour here - to call
        // iterator.remove() after the underlying collection has been
        // modified - is "unspecified".
        // However, in the case of iterators for EnumSet, it is easy to
        // implement their remove() operation such that the set is
        // unmodified if it is called for an element that has already been
        // removed from the set - this being the naturally "resilient"
        // behaviour.
        iterator.remove();
        checkSetAfterRemoval(set, size, element);
    }

    private void checkSetAfterRemoval(final Set<Silly65> set,
                                      final int origSize, final Silly65 removedElement) {
        if (set.size() != (origSize - 1)) {
            fail("Test FAILED: Unexpected set size after removal; expected '" + (origSize - 1) + "' but found '" + set.size() + "'");
        }
        if (set.contains(removedElement)) {
            fail("Test FAILED: Element returned from iterator unexpectedly still in set after removal.");
        }
    }

    @Test
    public void main127() {
        bash(Silly127.class);
    }

    @Test
    public void range127() {
        testRange(Silly127.e2, Silly127.e6);
        testRange(Silly127.e126, Silly127.e126);
        testRange(Silly127.e0, Silly127.e126);
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
        testRange(Silly500.e2, Silly500.e46);
        testRange(Silly500.e126, Silly500.e129);
        testRange(Silly500.e65, Silly500.e333);
        testRange(Silly500.e0, Silly500.e499);
    }

}
