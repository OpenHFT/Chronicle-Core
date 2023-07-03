package net.openhft.chronicle.core.values;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class TwoLongValueTest {

    private TwoLongValue twoLongValue;

    @BeforeEach
    public void setup() {
        twoLongValue = Mockito.mock(TwoLongValue.class);
    }

    @Test
    public void testSetMaxValue2() {
        when(twoLongValue.getVolatileValue2()).thenReturn(5L, 7L, 8L);
        twoLongValue.setMaxValue2(7L);
        Mockito.verify(twoLongValue, Mockito.times(2)).compareAndSwapValue2(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void testSetMinValue2() {
        when(twoLongValue.getVolatileValue2()).thenReturn(8L, 7L, 5L);
        twoLongValue.setMinValue2(7L);
        Mockito.verify(twoLongValue, Mockito.times(2)).compareAndSwapValue2(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void testSetValues() {
        twoLongValue.setValues(3L, 4L);
        Mockito.verify(twoLongValue).setValue2(4L);
        Mockito.verify(twoLongValue).setOrderedValue(3L);
    }

    @Test
    public void testGetValues() {
        when(twoLongValue.getVolatileValue()).thenReturn(3L);
        when(twoLongValue.getValue2()).thenReturn(4L);

        long[] values = new long[2];
        twoLongValue.getValues(values);

        assertEquals(3L, values[0]);
        assertEquals(4L, values[1]);
    }
}
