package net.openhft.chronicle.core.pool;

import net.openhft.chronicle.core.util.CoreDynamicEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DynamicEnumClassGptTest {

    enum TestEnum implements CoreDynamicEnum<TestEnum> { FIRST, SECOND, THIRD }

    private DynamicEnumClass<TestEnum> dynamicEnumClass;

    @BeforeEach
    public void setUp() {
        dynamicEnumClass = new DynamicEnumClass<>(TestEnum.class);
    }

    @Test
    public void testSize() {
        assertEquals(3, dynamicEnumClass.size());
        dynamicEnumClass.valueOf("NEW_VALUE");
        assertEquals(4, dynamicEnumClass.size());
    }

    @Test
    public void testForIndex() {
        TestEnum newEnum = dynamicEnumClass.valueOf("NEW_VALUE");
        assertEquals(newEnum, dynamicEnumClass.forIndex(3));
    }

    @Test
    public void testAsArray() {
        TestEnum newEnum = dynamicEnumClass.valueOf("NEW_VALUE");
        TestEnum[] enums = dynamicEnumClass.asArray();
        assertEquals(4, enums.length);
        assertEquals(newEnum, enums[3]);
    }

    @Test
    public void testCreateMap() {
        assertNotNull(dynamicEnumClass.createMap());
    }

    @Test
    public void testCreateSet() {
        assertNotNull(dynamicEnumClass.createSet());
    }

    @Test
    public void testReset() {
        dynamicEnumClass.valueOf("NEW_VALUE");
        assertEquals(4, dynamicEnumClass.size());
        dynamicEnumClass.reset();
        assertEquals(3, dynamicEnumClass.size());
    }
}
