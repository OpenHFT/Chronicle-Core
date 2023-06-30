package net.openhft.chronicle.core.pool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnumInternerGptTest {
    enum TestEnum { FIRST, SECOND, THIRD }

    private EnumInterner<TestEnum> interner;

    @BeforeEach
    public void setup() {
        interner = new EnumInterner<>(TestEnum.class);
    }

    @Test
    public void testIntern() {
        // Initial intern should return the correct Enum.
        assertEquals(TestEnum.FIRST, interner.intern("FIRST"));

        // Intern again, it should return the same object (referential equality).
        assertSame(TestEnum.FIRST, interner.intern("FIRST"));
    }

    @Test
    public void testInternNonExistent() {
        // Attempting to intern a non-existent enum should throw an IllegalArgumentException.
        assertThrows(IllegalArgumentException.class, () -> interner.intern("NON_EXISTENT"));
    }

    @Test
    public void testInternNull() {
        // Attempting to intern a null value should throw a NullPointerException.
        assertThrows(NullPointerException.class, () -> interner.intern(null));
    }

    @Test
    public void testInternCaseSensitivity() {
        // The interner should be case-sensitive.
        assertEquals(TestEnum.FIRST, interner.intern("FIRST"));
        assertThrows(IllegalArgumentException.class, () -> interner.intern("first"));
    }
}
