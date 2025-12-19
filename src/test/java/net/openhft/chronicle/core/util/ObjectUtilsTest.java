/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.pool.Ecn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

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
            assertEquals(ObjectUtils.Immutability.MAYBE, ObjectUtils.isImmutable(c), c.getName());
        }
        for (@NotNull Class<?> c : new Class[]{
                // StringBuilder.class, // StringBuilder implements Comparable in Java 11
                ArrayList.class,
                HashMap.class,
        }) {
            assertEquals(ObjectUtils.Immutability.NO, ObjectUtils.isImmutable(c), c.getName());
        }
    }

    @Test
    public void canConvertTo() {
        assertTrue(ObjectUtils.canConvertText(String.class), "string class should be convertible from text");
        assertTrue(ObjectUtils.canConvertText(Class.class), "class type should be convertible from text");
        assertTrue(ObjectUtils.canConvertText(Boolean.class), "boolean wrapper should be convertible from text");
        assertTrue(ObjectUtils.canConvertText(UUID.class), "uuid should be convertible from text");
        assertTrue(ObjectUtils.canConvertText(byte[].class), "byte array should be convertible from text");
        // an Enum
        assertTrue(ObjectUtils.canConvertText(Ecn.class), "enum should be convertible from text");
        // a primitive wrapper
        assertTrue(ObjectUtils.canConvertText(Long.class), "long wrapper should be convertible from text");
        // a scalar with a String constructor
        assertTrue(ObjectUtils.canConvertText(ClassWithString.class), "class with string constructor should be convertible from text");
        // a class with valueOf method
        assertTrue(ObjectUtils.canConvertText(ClassWithValueOf.class), "class with valueOf method should be convertible from text");
        // a class with parse method
        assertTrue(ObjectUtils.canConvertText(ClassWithParse.class), "class with parse method should be convertible from text");

        // a class with a setter method can't be used
        assertFalse(ObjectUtils.canConvertText(ClassWithSetter.class), "class with only setter should not be convertible from text");
    }

    @Test
    public void testConvert() throws IllegalStateException, IllegalArgumentException {
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1), "integer 1 should convert to character '1'");
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1L), "long 1 should convert to character '1'");
        assertEquals(1, (int) ObjectUtils.convertTo(int.class, '1'), "character '1' should convert to integer 1");
        assertEquals(1L, (long) ObjectUtils.convertTo(long.class, '1'), "character '1' should convert to long 1");
        assertEquals(1.0, ObjectUtils.convertTo(double.class, '1'), 0.0, "character '1' should convert to double 1.0");
    }

    @Test
    public void testNoDefaultClassForInterfaceNewInstanceThrows() {
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.newInstance(ExceptionHandler.class));
    }

    @Test
    public void supplierForClassShouldHandleDifferentClassTypes() {
        // Example for a regular class
        Supplier<RegularClass> regularClassSupplier = ObjectUtils.supplierForClass(RegularClass.class);
        assertNotNull(regularClassSupplier.get(), "supplier should create instance of regular class");

        // Example for a primitive type
        Supplier<Integer> integerSupplier = ObjectUtils.supplierForClass(int.class);
        assertThrows(IllegalArgumentException.class, integerSupplier::get);
    }

    @Test
    public void immutableShouldRegisterImmutability() {
        Class<?> testClass = RegularClass.class;
        ObjectUtils.immutable(testClass, true);
        assertEquals(ObjectUtils.Immutability.YES, ObjectUtils.isImmutable(testClass), "class registered as immutable should return immutability.yes");
    }

    @Test
    public void caseIgnoreLookupShouldCreateCorrectMap() {
        // Assuming MyEnum is an enum class
        Map<String, Enum<?>> map = ObjectUtils.caseIgnoreLookup(MyEnum.class);
        // Assertions to check the map contents
        assertEquals("{MY_VALUE=MY_VALUE}", map.toString(), "case-insensitive enum lookup map should contain all enum values");
    }

    @Test
    public void valueOfIgnoreCaseShouldReturnCorrectEnum() {
        // Assuming MyEnum is an enum class with a constant MY_VALUE
        MyEnum result = ObjectUtils.valueOfIgnoreCase(MyEnum.class, "my_value");
        assertEquals(MyEnum.MY_VALUE, result, "lowercase enum name should match enum constant case-insensitively");
    }

    @Test
    public void supplierForInternalPackageTest() {
        Supplier<?> supplier = ObjectUtils.supplierForInternalPackage();
        assertThrows(IllegalArgumentException.class, supplier::get, "supplier for internal package should throw when invoked");
    }

    @Test
    public void supplierForEnumTest() {
        Supplier<MyEnum> supplier = ObjectUtils.supplierForEnum(MyEnum.class);
        assertNotNull(supplier.get(), "enum supplier should create non-null enum value");
    }

    @Test
    public void supplierForAbstractClassTest() {
        Supplier<AbstractTestClass> supplier = ObjectUtils.supplierForAbstractClass(AbstractTestClass.class);
        assertThrows(IllegalArgumentException.class, supplier::get, "supplier for abstract class should throw when attempting instantiation");
    }

    @Test
    public void convertCharSingleCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertChar("a"), "single-character string should convert to character");
    }

    @Test
    public void convertCharLongStringTest() {
        assertNull(ObjectUtils.convertChar("long"), "multi-character string should return null when converting to char");
    }

    @Test
    public void convertTo0SameClassTest() {
        String testString = "test";
        assertEquals(testString, ObjectUtils.convertTo0(String.class, testString), "converting to same class should return original instance");
    }

    @Test
    public void convertTo0NullTest() {
        assertNull(ObjectUtils.convertTo0(String.class, null), "null input should return null");
    }

    @Test
    public void convertTo0VoidClassTest() {
        assertNull(ObjectUtils.convertTo0(Void.class, "anyValue"), "converting to void class should always return null");
    }

    @Test
    public void convertTo0ToStringTest() {
        Object testObject = new Object();
        assertEquals(testObject.toString(), ObjectUtils.convertTo0(String.class, testObject), "object should convert to string using tostring method");
    }

    @Test
    public void convertTo0ToNumberTest() {
        assertEquals(Integer.valueOf(10), ObjectUtils.convertTo0(Integer.class, "10"), "numeric string should parse to integer");
    }

    @Test
    public void convertTo0ToCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertTo0(Character.class, "a"), "single-character string should convert to character wrapper");
    }

    @Test
    public void convertTo0ToCharSequenceUsingParserTest() {
        assertEquals("test", ObjectUtils.convertTo0(String.class, "test"), "string to string conversion should return identical value");
    }

    @Test
    public void convertTo0ToDateFromLongTest() {
        long time = System.currentTimeMillis();
        Date expectedDate = new Date(time);
        assertEquals(expectedDate, ObjectUtils.convertTo0(Date.class, time), "timestamp long should convert to date with same milliseconds");
    }

    @Test
    public void convertTo0UnsupportedConversionTest() {
        assertThrows(ClassCastException.class,
                () -> ObjectUtils.convertTo0(Map.class, "test"),
                "convertTo0UnsupportedConversionTest");
    }

    @Test
    public void asCCETest() {
        Exception exception = new Exception("Test exception");
        ClassCastException cce = ObjectUtils.asCCE(exception);
        assertEquals(exception, cce.getCause(), "wrapped exception should be preserved as cause of classcastexception");
    }

    @Test
    public void sizeOfMapTest() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        assertEquals(2, ObjectUtils.sizeOf(map), "map with two entries should report size of 2");
    }

    @Test
    public void sizeOfUnsupportedTypeTest() {
        assertThrows(UnsupportedOperationException.class,
                () -> ObjectUtils.sizeOf(new Object()),
                "sizeOfUnsupportedTypeTest");
    }

    @Test
    public void convertToNumberTest() {
        assertEquals(1, ObjectUtils.convertToNumber(Integer.class, "1"), "string '1' should convert to integer 1");
    }

    @Test
    public void newInstanceWithClassNameTest() {
        RegularClass instance = ObjectUtils.newInstance(RegularClass.class.getName());
        assertNotNull(instance, "new instance should be created from class name string");
    }

    @Test
    public void newInstanceOrNullValidClassTest() {
        RegularClass instance = (RegularClass) ObjectUtils.newInstanceOrNull(RegularClass.class);
        assertNotNull(instance, "new instance should be created from valid class");
    }

    @Test
    public void addAllTest() {
        Integer[] result = ObjectUtils.addAll(1, 2, 3);
        assertArrayEquals(new Integer[]{1, 2, 3}, result, "varargs should combine into array with same elements in order");
    }

    @Test
    public void addAllSingleElementTest() {
        Integer[] result = ObjectUtils.addAll(1);
        assertArrayEquals(new Integer[]{1}, result, "single vararg should create single-element array");
    }

    @Test
    public void getAllInterfacesTest() {
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(new ImplementingClass());
        assertEquals("[interface net.openhft.chronicle.core.util.IgnoresEverything]",
                Arrays.toString(interfaces),
                "implementing class should report all interfaces in hierarchy");
    }

    @Test
    public void getAllInterfacesWithNullAccumulatorTest() {
        assertThrows(IllegalArgumentException.class,
                () -> ObjectUtils.getAllInterfaces(new ImplementingClass(), null),
                "getAllInterfacesWithNullAccumulatorTest");
    }

    @Test
    public void implementationToUseNonInterfaceTest() {
        Class<?> impl = ObjectUtils.implementationToUse(RegularClass.class);
        assertEquals(RegularClass.class, impl, "concrete class should return itself as implementation");
    }

    @Test
    public void testDefaultValueForPrimitives() {
        assertEquals(false, ObjectUtils.defaultValue(boolean.class), "boolean primitive default should be false");
        assertEquals((byte) 0, (byte) ObjectUtils.defaultValue(byte.class), "byte primitive default should be 0");
        assertEquals((short) 0, (short) ObjectUtils.defaultValue(short.class), "short primitive default should be 0");
        assertEquals((char) 0, (char) ObjectUtils.defaultValue(char.class), "char primitive default should be null character");
        assertEquals(0, (int) ObjectUtils.defaultValue(int.class), "int primitive default should be 0");
        assertEquals(0L, (long) ObjectUtils.defaultValue(long.class), "long primitive default should be 0");
        assertEquals(0.0f, ObjectUtils.defaultValue(float.class), 0.0f, "float primitive default should be 0.0");
        assertEquals(0.0d, ObjectUtils.defaultValue(double.class), 0.0d, "double primitive default should be 0.0");
    }

    @Test
    public void testDefaultValueForWrapperTypes() {
        assertNull(ObjectUtils.defaultValue(Boolean.class), "boolean wrapper default should be null");
        assertNull(ObjectUtils.defaultValue(Byte.class), "byte wrapper default should be null");
        assertNull(ObjectUtils.defaultValue(Short.class), "short wrapper default should be null");
        assertNull(ObjectUtils.defaultValue(Character.class), "character wrapper default should be null");
        assertNull(ObjectUtils.defaultValue(Integer.class), "integer wrapper default should be null");
        assertNull(ObjectUtils.defaultValue(Long.class), "long wrapper default should be null");
        assertNull(ObjectUtils.defaultValue(Float.class), "float wrapper default should be null");
        assertNull(ObjectUtils.defaultValue(Double.class), "double wrapper default should be null");
    }

    @Test
    public void testDefaultValueForCustomObjects() {
        assertNull(ObjectUtils.defaultValue(String.class), "string class default should be null");
        assertNull(ObjectUtils.defaultValue(BigDecimal.class), "bigdecimal class default should be null");
    }

    @Test
    public void testDefaultValueForUnsupportedType() {
        assertNull(ObjectUtils.defaultValue(Object.class), "object class default should be null");
    }

    @Test
    public void testDefaultValueWithNullClass() {
        assertNull(ObjectUtils.defaultValue(null), "null class parameter should return null");
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
