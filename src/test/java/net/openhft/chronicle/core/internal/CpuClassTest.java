package net.openhft.chronicle.core.internal;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.Test;

import static org.junit.Assert.*;

public class CpuClassTest extends CoreTestCommon {

    @Test
    public void removingTag() {
        final String actual = CpuClass.removingTag().apply("tag: value");
        assertEquals("value", actual);
    }

}