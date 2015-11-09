package net.openhft.chronicle.core.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Rob Austin.
 */
public class StringUtilsTest extends TestCase {

    @Test
    public void testParseDouble() throws IOException {
        for (double d : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double
                .POSITIVE_INFINITY, 0.0, -1.0, 1.0, 9999.0}) {
            assertEquals(d, StringUtils.parseDouble(Double.toString(d)), 0);
        }

        assertEquals(1.0, StringUtils.parseDouble("1"), 0);
        assertEquals(0.0, StringUtils.parseDouble("-0"), 0);
        assertEquals(123.0, StringUtils.parseDouble("123"), 0);
        assertEquals(-1.0, StringUtils.parseDouble("-1"), 0);
    }
}