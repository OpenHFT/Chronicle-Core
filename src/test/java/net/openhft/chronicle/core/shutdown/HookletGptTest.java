package net.openhft.chronicle.core.shutdown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HookletGptTest {

    @Test
    public void testPriorityOrder() {
        Hooklet hooklet1 = Hooklet.of(10, () -> System.out.println("Hooklet1"));
        Hooklet hooklet2 = Hooklet.of(20, () -> System.out.println("Hooklet2"));

        assertTrue(hooklet1.compareTo(hooklet2) < 0);
    }

    @Test
    public void testIdentity() {
        Hooklet hooklet1 = Hooklet.of(30, () -> System.out.println("Hooklet3"));
        Hooklet hooklet2 = Hooklet.of(30, () -> System.out.println("Hooklet4"));

        assertNotEquals(hooklet1.identity(), hooklet2.identity());
    }

    @Test
    public void testHashCode() {
        Hooklet hooklet1 = Hooklet.of(40, () -> System.out.println("Hooklet5"));
        Hooklet hooklet2 = Hooklet.of(40, () -> System.out.println("Hooklet6"));

        assertNotEquals(hooklet1.hashCode(), hooklet2.hashCode());
    }

    @Test
    public void testEquals() {
        Runnable runnable = () -> System.out.println("Hooklet7");
        Hooklet hooklet1 = Hooklet.of(50, runnable);
        Hooklet hooklet2 = Hooklet.of(50, () -> System.out.println("Hooklet8"));
        Hooklet hooklet1b = Hooklet.of(50, runnable);

        assertNotEquals(hooklet1, hooklet2);

        // Same instance should be equal
        assertEquals(hooklet1, hooklet1);
        assertEquals(hooklet1, hooklet1b);
    }

    @Test
    public void testToString() {
        Runnable runnable = () -> System.out.println("Hooklet7");
        Hooklet hooklet1 = Hooklet.of(50, runnable);
        Hooklet hooklet1b = Hooklet.of(50, runnable);

        assertEquals(hooklet1.toString(), hooklet1b.toString());
        assertEquals(hooklet1.hashCode(), hooklet1b.hashCode());
    }

    @Test
    public void testHashCodeEqual() {
        Runnable runnable = () -> System.out.println("Hooklet7");
        Hooklet hooklet1 = Hooklet.of(50, runnable);
        Hooklet hooklet1b = Hooklet.of(50, runnable);

        assertEquals(hooklet1.hashCode(), hooklet1b.hashCode());
    }
}
