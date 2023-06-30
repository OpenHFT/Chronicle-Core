package net.openhft.chronicle.core;

import net.openhft.chronicle.core.UnresolvedType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnresolvedTypeGptTest {
    @Test
    public void testGetTypeNam() {
        String typeName = "UnresolvedType";
        Type unresolvedType = UnresolvedType.of(typeName);
        assertEquals(typeName, unresolvedType.getTypeName());
    }

    @Test
    public void testToString() {
        String typeName = "UnresolvedType";
        Type unresolvedType = UnresolvedType.of(typeName);
        assertEquals(typeName, unresolvedType.toString());
    }
}