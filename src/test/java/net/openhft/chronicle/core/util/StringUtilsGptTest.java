package net.openhft.chronicle.core.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsGptTest {
    @Test
    public void testSetLength() {
        StringBuilder sb = new StringBuilder("Hello, World!");

        // Reducing the length
        StringUtils.setLength(sb, 5);
        assertEquals("Hello", sb.toString());

        // Setting length longer than the original content doesn't alter the original content
        StringUtils.setLength(sb, 10);
        assertEquals("Hello, Wor", sb.toString());

        // Setting length to 0
        StringUtils.setLength(sb, 0);
        assertEquals("", sb.toString());

        // Attempt to set negative length, should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> StringUtils.setLength(sb, -1));
    }

    @Test
    public void testSet() {
        StringBuilder sb = new StringBuilder("Hello, World!");

        // Setting content to "Sample"
        StringUtils.set(sb, "Sample");
        assertEquals("Sample", sb.toString());

        // Setting content to empty string
        StringUtils.set(sb, "");
        assertEquals("", sb.toString());

        // Setting content to non-empty after empty
        StringUtils.set(sb, "Back to text");
        assertEquals("Back to text", sb.toString());

        // Setting content with another StringBuilder
        StringUtils.set(sb, new StringBuilder("StringBuilder Text"));
        assertEquals("StringBuilder Text", sb.toString());
    }


    @Test
    public void testEndsWith() {
        // Positive test cases
        assertTrue(StringUtils.endsWith("Hello, World!", "World!"));
        assertTrue(StringUtils.endsWith("sentence", "ence"));
        assertTrue(StringUtils.endsWith("case-insensitive", "InSeNsItIvE"));
        assertTrue(StringUtils.endsWith("A", "a"));
        assertTrue(StringUtils.endsWith("Hello, World!", "WORLD!"));

        // Negative test cases
        assertFalse(StringUtils.endsWith("sentence", "sent"));
        assertFalse(StringUtils.endsWith("case-insensitive", "InSeNsIt"));
        assertFalse(StringUtils.endsWith("A", "AB"));
    }

    @Test
    public void testStartsWith() {
        // Positive test cases
        assertTrue(StringUtils.startsWith("Hello, World!", "Hello"));
        assertTrue(StringUtils.startsWith("sentence", "sent"));
        assertTrue(StringUtils.startsWith("case-insensitive", "CaSe-InSe"));
        assertTrue(StringUtils.startsWith("A", "a"));
        assertTrue(StringUtils.startsWith("Hello, World!", "HELLO"));

        // Negative test cases
        assertFalse(StringUtils.startsWith("sentence", "ence"));
        assertFalse(StringUtils.startsWith("case-insensitive", "InSeNsItIvE"));
        assertFalse(StringUtils.startsWith("A", "AB"));
    }

    @Test
    public void testIsEqual() {
        // Positive test cases
        assertTrue(StringUtils.isEqual("Hello", "Hello"));
        assertTrue(StringUtils.isEqual("sample", "sample"));
        assertTrue(StringUtils.isEqual(new StringBuilder("Hello"), "Hello"));
        assertTrue(StringUtils.isEqual(null, null));
        assertTrue(StringUtils.isEqual(new StringBuilder("sample"), new StringBuilder("sample")));

        // Negative test cases
        assertFalse(StringUtils.isEqual("Hello", "hello"));
        assertFalse(StringUtils.isEqual("sample", "samples"));
        assertFalse(StringUtils.isEqual(new StringBuilder("Hello"), "World"));
        assertFalse(StringUtils.isEqual(null, "not null"));
        assertFalse(StringUtils.isEqual("not null", null));
        assertFalse(StringUtils.isEqual(new StringBuilder("sample"), new StringBuilder("samples")));
    }


    @Test
    public void testEqualsCaseIgnore() {
        // Positive test cases
        assertTrue(StringUtils.equalsCaseIgnore("HELLO", "hello"));
        assertTrue(StringUtils.equalsCaseIgnore("SaMple", "sAmPLe"));
        assertTrue(StringUtils.equalsCaseIgnore(new StringBuilder("HeLLo"), "hello"));

        // Negative test cases
        assertFalse(StringUtils.equalsCaseIgnore("Hello", "World"));
        assertFalse(StringUtils.equalsCaseIgnore("sample", "samples"));
        assertFalse(StringUtils.equalsCaseIgnore(null, "not null"));
        assertFalse(StringUtils.equalsCaseIgnore(new StringBuilder("sample"), new StringBuilder("samples")));
    }

    @Test
    public void testToString() {
        // Positive test cases
        assertEquals("Hello", StringUtils.toString("Hello"));
        assertEquals("123", StringUtils.toString(123));
        assertEquals("true", StringUtils.toString(true));
        assertEquals(null, StringUtils.toString(null));
    }


    @Test
    public void testToTitleCase() {
        // Positive test cases
        assertEquals("HELLO_WORLD", StringUtils.toTitleCase("helloWorld"));
        assertEquals("MY_NAME_IS", StringUtils.toTitleCase("myNameIs"));
        assertEquals("JAVA", StringUtils.toTitleCase("java"));
        assertEquals(null, StringUtils.toTitleCase(null));
        assertEquals("", StringUtils.toTitleCase(""));
    }

    @Test
    public void testReverse() {
        StringBuilder text;

        // Test case 1
        text = new StringBuilder("Hello World!");
        StringUtils.reverse(text, 0);
        assertEquals("!dlroW olleH", text.toString());

        // Test case 2
        text = new StringBuilder("abcdef");
        StringUtils.reverse(text, 2);
        assertEquals("abfedc", text.toString());

        // Test case 3
        text = new StringBuilder("abcdefghijklmnopqrstuvwxyz");
        StringUtils.reverse(text, 3);
        assertEquals("abczyxwvutsrqponmlkjihgfed", text.toString());
        StringUtils.reverse(text, 3);
        assertEquals("abcdefghijklmnopqrstuvwxyz", text.toString());

        // Test case 4
        StringUtils.reverse(text, 4);
        assertEquals("abcdzyxwvutsrqponmlkjihgfe", text.toString());
        StringUtils.reverse(text, 4);
        assertEquals("abcdefghijklmnopqrstuvwxyz", text.toString());
    }


    @Test
    public void testForInputString() {
        CharSequence input = "notANumber";
        try {
            throw StringUtils.forInputString(input);
        } catch (NumberFormatException e) {
            assertEquals("For input string: \"notANumber\"", e.getMessage());
        }
    }

    @Test
    public void testForRadix() {
        int lowerRadix = 1;
        int higherRadix = 40;

        // Testing with radix less than Character.MIN_RADIX
        try {
            throw StringUtils.forRadix(lowerRadix, true);
        } catch (NumberFormatException e) {
            assertEquals("radix 1 less than Character.MIN_RADIX", e.getMessage());
        }

        // Testing with radix greater than Character.MAX_RADIX
        try {
            throw StringUtils.forRadix(higherRadix, false);
        } catch (NumberFormatException e) {
            assertEquals("radix 40 greater than Character.MAX_RADIX", e.getMessage());
        }
    }
}
