package net.openhft.chronicle.core;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class ClassLocalGptTest {
    @Test
    void testWithInitial() {
        ClassLocal<String> classLocal = ClassLocal.withInitial(Class::getSimpleName);
        assertEquals("String", classLocal.get(String.class));
        assertEquals("Integer", classLocal.get(Integer.class));
    }

    @Test
    void testComputeValue() {
        ClassLocal<String> classLocal = ClassLocal.withInitial(Class::getSimpleName);
        assertEquals("String", classLocal.computeValue(String.class));
        assertEquals("Integer", classLocal.computeValue(Integer.class));
    }
}
