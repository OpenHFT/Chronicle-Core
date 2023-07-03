package net.openhft.chronicle.core.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ObjectUtilsGptTest {

    @Test
    public void testAsCCE() {
        Exception exception = new Exception("Test");
        ClassCastException cce = ObjectUtils.asCCE(exception);
        assertEquals(exception, cce.getCause(), "Cause of ClassCastException should be the original exception");
    }

    @Test
    public void testConvertToArray() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Integer[] array = ObjectUtils.convertToArray(Integer[].class, list);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, array, "Array should be equal to the original list");
    }

    @Test
    public void testElementType() {
        Class<?> elementType = ObjectUtils.elementType(Integer[].class);
        assertEquals(Integer.class, elementType, "Element type of Integer[] should be Integer");
    }

    @Test
    public void testIteratorFor() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5};
        Iterable<Integer> iterable = Arrays.asList(array);
        assertEquals(5, ObjectUtils.sizeOf(iterable), "Size of the iterable should be 5");
    }

    @Test
    public void testSizeOf() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5};
        assertEquals(5, ObjectUtils.sizeOf(array), "Size of the array should be 5");
    }

    @Test
    public void testConvertToNumber() {
        BigDecimal converted = (BigDecimal) ObjectUtils.convertToNumber(BigDecimal.class, "3.14");
        assertEquals(new BigDecimal("3.14"), converted, "String should be converted to BigDecimal");
    }
}
