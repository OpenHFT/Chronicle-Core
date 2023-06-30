package net.openhft.chronicle.core.pool;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringInternerGptTest {
    @Test
    public void testIntern() {
        StringInterner stringInterner = new StringInterner(128);
        String testString = "test";

        assertNull(stringInterner.intern(null));  // Test with null
        assertEquals(testString, stringInterner.intern(testString));  // Test with a string
    }

    @Test
    public void testCapacity() {
        int capacity = 128;
        StringInterner stringInterner = new StringInterner(capacity);
        assertEquals(capacity, stringInterner.capacity());
    }

    @Test
    public void testIndex() {
        StringInterner stringInterner = new StringInterner(128);
        StringInterner.Changed onChanged = (index, value) -> {};

        assertEquals(-1, stringInterner.index(null, onChanged));  // Test with null
        assertNotEquals(-1, stringInterner.index("test", onChanged));  // Test with a string
    }

    @Test
    public void testGet() {
        StringInterner stringInterner = new StringInterner(128);
        String testString = "test";
        int index = stringInterner.index(testString, null);

        assertEquals(testString, stringInterner.get(index));
    }

    @Test
    public void testValueCount() {
        StringInterner stringInterner = new StringInterner(128);

        assertEquals(0, stringInterner.valueCount());  // No values yet

        stringInterner.intern("test");

        assertEquals(1, stringInterner.valueCount());  // One value has been added
    }
}
