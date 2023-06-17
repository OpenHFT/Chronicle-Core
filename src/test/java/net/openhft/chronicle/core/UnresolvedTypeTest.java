package net.openhft.chronicle.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnresolvedTypeTest {
    @Test
    public void getTypeName() {
        assertEquals("MyType",
                UnresolvedType.of("MyType").getTypeName());
    }

    @Test
    public void testToString() {
        assertEquals("MyType",
                UnresolvedType.of("MyType").toString());
    }
}