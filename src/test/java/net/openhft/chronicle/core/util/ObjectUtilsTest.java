/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.pool.Ecn;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.*;

@SuppressWarnings("java:S1068")
public class ObjectUtilsTest extends CoreTestCommon {
    @SuppressWarnings("rawtypes")
    @Test
    public void testImmutable() {
        for (@NotNull Class<?> c : new Class[]{
                String.class,
                Integer.class,
                Date.class,
                BigDecimal.class,
                ZonedDateTime.class,
        }) {
            assertEquals(c.getName(), ObjectUtils.Immutability.MAYBE, ObjectUtils.isImmutable(c));
        }
        for (@NotNull Class<?> c : new Class[]{
                // StringBuilder.class, // StringBuilder implements Comparable in Java 11
                ArrayList.class,
                HashMap.class,
        }) {
            assertEquals(c.getName(), ObjectUtils.Immutability.NO, ObjectUtils.isImmutable(c));
        }
    }

    @Test
    public void canConvertTo() {
        assertTrue(ObjectUtils.canConvertText(String.class));
        assertTrue(ObjectUtils.canConvertText(Class.class));
        assertTrue(ObjectUtils.canConvertText(Boolean.class));
        assertTrue(ObjectUtils.canConvertText(UUID.class));
        assertTrue(ObjectUtils.canConvertText(byte[].class));
        // an Enum
        assertTrue(ObjectUtils.canConvertText(Ecn.class));
        // a primitive wrapper
        assertTrue(ObjectUtils.canConvertText(Long.class));
        // a scalar with a String constructor
        assertTrue(ObjectUtils.canConvertText(ClassWithString.class));
        // a class with valueOf method
        assertTrue(ObjectUtils.canConvertText(ClassWithValueOf.class));
        // a class with parse method
        assertTrue(ObjectUtils.canConvertText(ClassWithParse.class));

        // a class with a setter method can't be used
        assertFalse(ObjectUtils.canConvertText(ClassWithSetter.class));
    }

    @Test
    public void testConvert() throws IllegalStateException, IllegalArgumentException {
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1));
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1L));
        assertEquals(1, (int) ObjectUtils.convertTo(int.class, '1'));
        assertEquals(1L, (long) ObjectUtils.convertTo(long.class, '1'));
        assertEquals(1.0, ObjectUtils.convertTo(double.class, '1'), 0.0);
    }

    @Test
    public void testNoDefaultClassForInterfaceNewInstanceThrows() {
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.newInstance(ExceptionHandler.class));
    }

    @Test
    public void supplierForClassShouldHandleDifferentClassTypes() {
        // Example for a regular class
        Supplier<RegularClass> regularClassSupplier = ObjectUtils.supplierForClass(RegularClass.class);
        assertNotNull(regularClassSupplier.get());

        // Example for a primitive type
        Supplier<Integer> integerSupplier = ObjectUtils.supplierForClass(int.class);
        assertThrows(IllegalArgumentException.class, integerSupplier::get);
    }

    @Test
    public void immutableShouldRegisterImmutability() {
        Class<?> testClass = RegularClass.class;
        ObjectUtils.immutable(testClass, true);
        assertEquals(ObjectUtils.Immutability.YES, ObjectUtils.isImmutable(testClass));
    }

    @Test
    public void caseIgnoreLookupShouldCreateCorrectMap() {
        // Assuming MyEnum is an enum class
        Map<String, Enum<?>> map = ObjectUtils.caseIgnoreLookup(MyEnum.class);
        // Assertions to check the map contents
        assertEquals("{MY_VALUE=MY_VALUE}", map.toString());
    }

    @Test
    public void valueOfIgnoreCaseShouldReturnCorrectEnum() {
        // Assuming MyEnum is an enum class with a constant MY_VALUE
        MyEnum result = ObjectUtils.valueOfIgnoreCase(MyEnum.class, "my_value");
        assertEquals(MyEnum.MY_VALUE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void supplierForInternalPackageTest() {
        Supplier<?> supplier = ObjectUtils.supplierForInternalPackage();
        supplier.get();
    }

    @Test
    public void supplierForEnumTest() {
        Supplier<MyEnum> supplier = ObjectUtils.supplierForEnum(MyEnum.class);
        assertNotNull(supplier.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void supplierForAbstractClassTest() {
        Supplier<AbstractTestClass> supplier = ObjectUtils.supplierForAbstractClass(AbstractTestClass.class);
        supplier.get();
    }

    @Test
    public void convertCharSingleCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertChar("a"));
    }

    @Test
    public void convertCharLongStringTest() {
        assertNull(ObjectUtils.convertChar("long"));
    }

    @Test
    public void convertTo0SameClassTest() {
        String testString = "test";
        assertEquals(testString, ObjectUtils.convertTo0(String.class, testString));
    }

    @Test
    public void convertTo0NullTest() {
        assertNull(ObjectUtils.convertTo0(String.class, null));
    }

    @Test
    public void convertTo0VoidClassTest() {
        assertNull(ObjectUtils.convertTo0(Void.class, "anyValue"));
    }

    @Test
    public void convertTo0ToStringTest() {
        Object testObject = new Object();
        assertEquals(testObject.toString(), ObjectUtils.convertTo0(String.class, testObject));
    }

    @Test
    public void convertTo0ToNumberTest() {
        assertEquals(Integer.valueOf(10), ObjectUtils.convertTo0(Integer.class, "10"));
    }

    @Test
    public void convertTo0ToCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertTo0(Character.class, "a"));
    }

    @Test
    public void convertTo0ToCharSequenceUsingParserTest() {
        assertEquals("test", ObjectUtils.convertTo0(String.class, "test"));
    }

    @Test
    public void convertTo0ToDateFromLongTest() {
        long time = System.currentTimeMillis();
        Date expectedDate = new Date(time);
        assertEquals(expectedDate, ObjectUtils.convertTo0(Date.class, time));
    }

    @Test(expected = ClassCastException.class)
    public void convertTo0UnsupportedConversionTest() {
        ObjectUtils.convertTo0(Map.class, "test");
    }

    @Test
    public void asCCETest() {
        Exception exception = new Exception("Test exception");
        ClassCastException cce = ObjectUtils.asCCE(exception);
        assertEquals(exception, cce.getCause());
    }

    @Test
    public void sizeOfMapTest() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        assertEquals(2, ObjectUtils.sizeOf(map));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void sizeOfUnsupportedTypeTest() {
        ObjectUtils.sizeOf(new Object());
    }

    @Test
    public void convertToNumberTest() {
        assertEquals(1, ObjectUtils.convertToNumber(Integer.class, "1"));
    }

    @Test
    public void newInstanceWithClassNameTest() {
        RegularClass instance = ObjectUtils.newInstance(RegularClass.class.getName());
        assertNotNull(instance);
    }

    @Test
    public void newInstanceOrNullValidClassTest() {
        RegularClass instance = (RegularClass) ObjectUtils.newInstanceOrNull(RegularClass.class);
        assertNotNull(instance);
    }

    @Test
    public void addAllTest() {
        Integer[] result = ObjectUtils.addAll(1, 2, 3);
        assertArrayEquals(new Integer[]{1, 2, 3}, result);
    }

    @Test
    public void addAllSingleElementTest() {
        Integer[] result = ObjectUtils.addAll(1);
        assertArrayEquals(new Integer[]{1}, result);
    }

    @Test
    public void getAllInterfacesTest() {
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(new ImplementingClass());
        assertEquals("[interface net.openhft.chronicle.core.util.IgnoresEverything]",
                Arrays.toString(interfaces));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllInterfacesWithNullAccumulatorTest() {
        ObjectUtils.getAllInterfaces(new ImplementingClass(), null);
    }

    @Test
    public void implementationToUseNonInterfaceTest() {
        Class<?> impl = ObjectUtils.implementationToUse(RegularClass.class);
        assertEquals(RegularClass.class, impl);
    }

    @Test
    public void testDefaultValueForPrimitives() {
        assertEquals(false, ObjectUtils.defaultValue(boolean.class));
        assertEquals((byte) 0, (byte) ObjectUtils.defaultValue(byte.class));
        assertEquals((short) 0, (short) ObjectUtils.defaultValue(short.class));
        assertEquals((char) 0, (char) ObjectUtils.defaultValue(char.class));
        assertEquals(0, (int) ObjectUtils.defaultValue(int.class));
        assertEquals(0L, (long) ObjectUtils.defaultValue(long.class));
        assertEquals(0.0f, ObjectUtils.defaultValue(float.class), 0.0f);
        assertEquals(0.0d, ObjectUtils.defaultValue(double.class), 0.0d);
    }

    @Test
    public void testDefaultValueForWrapperTypes() {
        assertNull(ObjectUtils.defaultValue(Boolean.class));
        assertNull(ObjectUtils.defaultValue(Byte.class));
        assertNull(ObjectUtils.defaultValue(Short.class));
        assertNull(ObjectUtils.defaultValue(Character.class));
        assertNull(ObjectUtils.defaultValue(Integer.class));
        assertNull(ObjectUtils.defaultValue(Long.class));
        assertNull(ObjectUtils.defaultValue(Float.class));
        assertNull(ObjectUtils.defaultValue(Double.class));
    }

    @Test
    public void testDefaultValueForCustomObjects() {
        assertNull(ObjectUtils.defaultValue(String.class));
        assertNull(ObjectUtils.defaultValue(BigDecimal.class));
    }

    @Test
    public void testDefaultValueForUnsupportedType() {
        assertNull(ObjectUtils.defaultValue(Object.class));
    }

    @Test
    public void testDefaultValueWithNullClass() {
        assertNull(ObjectUtils.defaultValue(null));
    }

    enum MyEnum {
        MY_VALUE
    }

    static class ClassWithString {

        ClassWithString(String s) {
        }
    }

    static class ClassWithValueOf {

        ClassWithValueOf(String s) {
        }

        public static ClassWithValueOf valueOf(String s) {
            return new ClassWithValueOf(s);
        }
    }

    static class ClassWithParse {

        ClassWithParse(String s) {
        }

        public static ClassWithParse parse(CharSequence s) {
            return new ClassWithParse(s.toString());
        }
    }

    static class ClassWithSetter {

        public void setS(String s) {
        }
    }

    static class ImplementingClass implements IgnoresEverything {
    }

    private static class AbstractTestClass {
    }

    private static class RegularClass {
    }
}
