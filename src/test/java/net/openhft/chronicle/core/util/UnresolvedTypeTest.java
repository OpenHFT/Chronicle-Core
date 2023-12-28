package net.openhft.chronicle.core.util;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

class UnresolvedTypeTest {

    @Test
    void constructorShouldInitializeTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName());
    }

    @Test
    void factoryMethodShouldCreateUnresolvedType() {
        String expectedTypeName = "MyType";
        Type type = UnresolvedType.of(expectedTypeName);

        assertTrue(type instanceof UnresolvedType);
        assertEquals(expectedTypeName, type.getTypeName());
    }

    @Test
    void getTypeNameShouldReturnCorrectTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.getTypeName());
    }

    @Test
    void toStringShouldReturnTypeName() {
        String expectedTypeName = "MyType";
        UnresolvedType unresolvedType = new UnresolvedType(expectedTypeName);

        assertEquals(expectedTypeName, unresolvedType.toString());
    }
}
