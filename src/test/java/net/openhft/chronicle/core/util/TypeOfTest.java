package net.openhft.chronicle.core.util;

import org.junit.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class TypeOfTest {

    @Test
    public <T extends Number> void type() {
        assertEquals("java.util.List<?>",
                new TypeOf<List<?>>() {
                }.type().toString());
        assertEquals("java.util.List<java.lang.String>",
                new TypeOf<List<String>>() {
                }.type().toString());
        assertEquals("java.util.List<T>",
                new TypeOf<List<T>>() {
                }.type().toString());
        assertEquals("java.util.function.BiFunction<java.util.List<java.lang.String>, java.lang.Integer, java.lang.String>",
                new TypeOf<BiFunction<List<String>, Integer, String>>() {
                }.type().toString());
    }
}