package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClassMetricsGptTest {
    @Test
    void testOffset() {
        ClassMetrics classMetrics = new ClassMetrics(10, 20);
        assertEquals(10, classMetrics.offset());
    }

    @Test
    void testLength() {
        ClassMetrics classMetrics = new ClassMetrics(10, 20);
        assertEquals(20, classMetrics.length());
    }

    @Test
    void testEquals() {
        ClassMetrics classMetrics1 = new ClassMetrics(10, 20);
        ClassMetrics classMetrics2 = new ClassMetrics(10, 20);
        ClassMetrics classMetrics3 = new ClassMetrics(20, 30);
        assertEquals(classMetrics1, classMetrics2);
        assertNotEquals(classMetrics1, classMetrics3);
    }

    @Test
    void testHashCode() {
        ClassMetrics classMetrics1 = new ClassMetrics(10, 20);
        ClassMetrics classMetrics2 = new ClassMetrics(10, 20);
        assertEquals(classMetrics1.hashCode(), classMetrics2.hashCode());
    }

    @Test
    void testToString() {
        ClassMetrics classMetrics = new ClassMetrics(10, 20);
        assertEquals("ClassMetrics{offset=10, length=20}", classMetrics.toString());
    }
}
