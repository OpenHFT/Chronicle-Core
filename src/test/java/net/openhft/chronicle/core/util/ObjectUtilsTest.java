/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
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
class ObjectUtilsTest extends CoreTestCommon {
    @Test
    @SuppressWarnings("rawtypes")
    void testImmutable() {
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
    void canConvertTo() {
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

    static class ClassWithString {
        @UsedViaReflection
        private final String s;

        ClassWithString(String s) {
            this.s = s;
        }
    }

    static class ClassWithValueOf {
        @UsedViaReflection
        private final String s;

        ClassWithValueOf(String s) {
            this.s = s;
        }

        public static ClassWithValueOf valueOf(String s) {
            return new ClassWithValueOf(s);
        }
    }

    static class ClassWithParse {
        @UsedViaReflection
        private final String s;

        ClassWithParse(String s) {
            this.s = s;
        }

        public static ClassWithParse parse(CharSequence s) {
            return new ClassWithParse(s.toString());
        }
    }

    static class ClassWithSetter {
        @UsedViaReflection
        private String s;

        public void setS(String s) {
            this.s = s;
        }
    }

    @Test
    void testConvert() throws IllegalStateException, IllegalArgumentException {
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1));
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1L));
        assertEquals(1, (int) ObjectUtils.convertTo(int.class, '1'));
        assertEquals(1L, (long) ObjectUtils.convertTo(long.class, '1'));
        assertEquals(1.0, ObjectUtils.convertTo(double.class, '1'), 0.0);
    }

    @Test
    void testNoDefaultClassForInterfaceNewInstanceThrows() {
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.newInstance(ExceptionHandler.class));
    }

    @Test
    void supplierForClassShouldHandleDifferentClassTypes() {
        // Example for a regular class
        Supplier<RegularClass> regularClassSupplier = ObjectUtils.supplierForClass(RegularClass.class);
        assertNotNull(regularClassSupplier.get());

        // Example for a primitive type
        Supplier<Integer> integerSupplier = ObjectUtils.supplierForClass(int.class);
        assertThrows(IllegalArgumentException.class, integerSupplier::get);
    }

    @Test
    void immutableShouldRegisterImmutability() {
        Class<?> testClass = RegularClass.class;
        ObjectUtils.immutable(testClass, true);
        assertEquals(ObjectUtils.Immutability.YES, ObjectUtils.isImmutable(testClass));
    }

    @Test
    void caseIgnoreLookupShouldCreateCorrectMap() {
        // Assuming MyEnum is an enum class
        Map<String, Enum<?>> map = ObjectUtils.caseIgnoreLookup(MyEnum.class);
        // Assertions to check the map contents
        assertEquals("{MY_VALUE=MY_VALUE}", map.toString());
    }

    @Test
    void valueOfIgnoreCaseShouldReturnCorrectEnum() {
        // Assuming MyEnum is an enum class with a constant MY_VALUE
        MyEnum result = ObjectUtils.valueOfIgnoreCase(MyEnum.class, "my_value");
        assertEquals(MyEnum.MY_VALUE, result);
    }

    @Test
    void supplierForInternalPackageTest() {
        Supplier<?> supplier = ObjectUtils.supplierForInternalPackage();
        assertThrows(IllegalArgumentException.class, supplier::get);
    }

    @Test
    void supplierForEnumTest() {
        Supplier<MyEnum> supplier = ObjectUtils.supplierForEnum(MyEnum.class);
        assertNotNull(supplier.get());
    }

    @Test
    void supplierForAbstractClassTest() {
        Supplier<AbstractTestClass> supplier = ObjectUtils.supplierForAbstractClass(AbstractTestClass.class);
        assertThrows(IllegalArgumentException.class, supplier::get);
    }

    @Test
    void convertCharSingleCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertChar("a"));
    }

    @Test
    void convertCharLongStringTest() {
        assertNull(ObjectUtils.convertChar("long"));
    }

    @Test
    void convertTo0SameClassTest() {
        String testString = "test";
        assertEquals(testString, ObjectUtils.convertTo0(String.class, testString));
    }

    @Test
    void convertTo0NullTest() {
        assertNull(ObjectUtils.convertTo0(String.class, null));
    }

    @Test
    void convertTo0VoidClassTest() {
        assertNull(ObjectUtils.convertTo0(Void.class, "anyValue"));
    }

    @Test
    void convertTo0ToStringTest() {
        Object testObject = new Object();
        assertEquals(testObject.toString(), ObjectUtils.convertTo0(String.class, testObject));
    }

    @Test
    void convertTo0ToNumberTest() {
        assertEquals(Integer.valueOf(10), ObjectUtils.convertTo0(Integer.class, "10"));
    }

    @Test
    void convertTo0ToCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertTo0(Character.class, "a"));
    }

    @Test
    void convertTo0ToCharSequenceUsingParserTest() {
        assertEquals("test", ObjectUtils.convertTo0(String.class, "test"));
    }

    @Test
    void convertTo0ToDateFromLongTest() {
        long time = System.currentTimeMillis();
        Date expectedDate = new Date(time);
        assertEquals(expectedDate, ObjectUtils.convertTo0(Date.class, time));
    }

    @Test
    void convertTo0UnsupportedConversionTest() {
        assertThrows(ClassCastException.class, () -> ObjectUtils.convertTo0(Map.class, "test"));
    }

    @Test
    void asCCETest() {
        Exception exception = new Exception("Test exception");
        ClassCastException cce = ObjectUtils.asCCE(exception);
        assertEquals(exception, cce.getCause());
    }

    @Test
    void sizeOfMapTest() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        assertEquals(2, ObjectUtils.sizeOf(map));
    }

    @Test
    void sizeOfUnsupportedTypeTest() {
        assertThrows(UnsupportedOperationException.class, () -> ObjectUtils.sizeOf(new Object()));
    }

    @Test
    void convertToNumberTest() {
        assertEquals(1, ObjectUtils.convertToNumber(Integer.class, "1"));
    }

    @Test
    void newInstanceWithClassNameTest() {
        RegularClass instance = ObjectUtils.newInstance(RegularClass.class.getName());
        assertNotNull(instance);
    }

    @Test
    void newInstanceOrNullValidClassTest() {
        RegularClass instance = (RegularClass) ObjectUtils.newInstanceOrNull(RegularClass.class);
        assertNotNull(instance);
    }

    @Test
    void addAllTest() {
        Integer[] result = ObjectUtils.addAll(1, 2, 3);
        assertArrayEquals(new Integer[]{1, 2, 3}, result);
    }

    @Test
    void addAllSingleElementTest() {
        Integer[] result = ObjectUtils.addAll(1);
        assertArrayEquals(new Integer[]{1}, result);
    }

    @Test
    void getAllInterfacesTest() {
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(new ImplementingClass());
        assertEquals("[interface net.openhft.chronicle.core.util.IgnoresEverything]", Arrays.toString(interfaces));
    }

    @Test
    void getAllInterfacesWithNullAccumulatorTest() {
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.getAllInterfaces(new ImplementingClass(), null));
    }

    @Test
    void implementationToUseNonInterfaceTest() {
        Class<?> impl = ObjectUtils.implementationToUse(RegularClass.class);
        assertSame(RegularClass.class, impl);
    }

    // Define MyEnum or use an existing enum for testing
    enum MyEnum {
        MY_VALUE
    }

    static class ImplementingClass implements IgnoresEverything {
    }

    private static class AbstractTestClass {
    }

    private static class RegularClass {
    }

    @Test
    void testDefaultValueForPrimitives() {
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
    void testDefaultValueForWrapperTypes() {
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
    void testDefaultValueForCustomObjects() {
        assertNull(ObjectUtils.defaultValue(String.class));
        assertNull(ObjectUtils.defaultValue(BigDecimal.class));
    }

    @Test
    void testDefaultValueForUnsupportedType() {
        assertNull(ObjectUtils.defaultValue(Object.class));
    }

    @Test
    void testDefaultValueWithNullClass() {
        assertNull(ObjectUtils.defaultValue(null));
    }
}
