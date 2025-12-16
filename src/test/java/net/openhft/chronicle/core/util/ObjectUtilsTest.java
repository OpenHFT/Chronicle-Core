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
        assertTrue(ObjectUtils.canConvertText(String.class), "canConvertTo: L44");
        assertTrue(ObjectUtils.canConvertText(Class.class), "canConvertTo: L45");
        assertTrue(ObjectUtils.canConvertText(Boolean.class), "canConvertTo: L46");
        assertTrue(ObjectUtils.canConvertText(UUID.class), "canConvertTo: L47");
        assertTrue(ObjectUtils.canConvertText(byte[].class), "canConvertTo: L48");
        // an Enum
        assertTrue(ObjectUtils.canConvertText(Ecn.class), "canConvertTo: L50");
        // a primitive wrapper
        assertTrue(ObjectUtils.canConvertText(Long.class), "canConvertTo: L52");
        // a scalar with a String constructor
        assertTrue(ObjectUtils.canConvertText(ClassWithString.class), "canConvertTo: L54");
        // a class with valueOf method
        assertTrue(ObjectUtils.canConvertText(ClassWithValueOf.class), "canConvertTo: L56");
        // a class with parse method
        assertTrue(ObjectUtils.canConvertText(ClassWithParse.class), "canConvertTo: L58");

        // a class with a setter method can't be used
        assertFalse(ObjectUtils.canConvertText(ClassWithSetter.class), "canConvertTo: L61");
    }

    @Test
    public void testConvert() throws IllegalStateException, IllegalArgumentException {
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1), "testConvert: L66");
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1L), "testConvert: L67");
        assertEquals(1, (int) ObjectUtils.convertTo(int.class, '1'), "testConvert: L68");
        assertEquals(1L, (long) ObjectUtils.convertTo(long.class, '1'), "testConvert: L69");
        assertEquals(1.0, ObjectUtils.convertTo(double.class, '1'), 0.0, "testConvert: L70");
    }

    @Test
    public void testNoDefaultClassForInterfaceNewInstanceThrows() {
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.newInstance(ExceptionHandler.class));
    }

    @Test
    public void supplierForClassShouldHandleDifferentClassTypes() {
        // Example for a regular class
        Supplier<RegularClass> regularClassSupplier = ObjectUtils.supplierForClass(RegularClass.class);
        assertNotNull(regularClassSupplier.get(), "supplierForClassShouldHandleDifferentClassTypes: L82");

        // Example for a primitive type
        Supplier<Integer> integerSupplier = ObjectUtils.supplierForClass(int.class);
        assertThrows(IllegalArgumentException.class, integerSupplier::get);
    }

    @Test
    public void immutableShouldRegisterImmutability() {
        Class<?> testClass = RegularClass.class;
        ObjectUtils.immutable(testClass, true);
        assertEquals(ObjectUtils.Immutability.YES, ObjectUtils.isImmutable(testClass), "immutableShouldRegisterImmutability: L93");
    }

    @Test
    public void caseIgnoreLookupShouldCreateCorrectMap() {
        // Assuming MyEnum is an enum class
        Map<String, Enum<?>> map = ObjectUtils.caseIgnoreLookup(MyEnum.class);
        // Assertions to check the map contents
        assertEquals("{MY_VALUE=MY_VALUE}", map.toString(), "caseIgnoreLookupShouldCreateCorrectMap: L101");
    }

    @Test
    public void valueOfIgnoreCaseShouldReturnCorrectEnum() {
        // Assuming MyEnum is an enum class with a constant MY_VALUE
        MyEnum result = ObjectUtils.valueOfIgnoreCase(MyEnum.class, "my_value");
        assertEquals(MyEnum.MY_VALUE, result, "valueOfIgnoreCaseShouldReturnCorrectEnum: L108");
    }

    @Test
    public void supplierForInternalPackageTest() {
        Supplier<?> supplier = ObjectUtils.supplierForInternalPackage();
        assertThrows(IllegalArgumentException.class, supplier::get, "supplierForInternalPackageTest");
    }

    @Test
    public void supplierForEnumTest() {
        Supplier<MyEnum> supplier = ObjectUtils.supplierForEnum(MyEnum.class);
        assertNotNull(supplier.get(), "supplierForEnumTest: L120");
    }

    @Test
    public void supplierForAbstractClassTest() {
        Supplier<AbstractTestClass> supplier = ObjectUtils.supplierForAbstractClass(AbstractTestClass.class);
        assertThrows(IllegalArgumentException.class, supplier::get, "supplierForAbstractClassTest");
    }

    @Test
    public void convertCharSingleCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertChar("a"), "convertCharSingleCharacterTest: L131");
    }

    @Test
    public void convertCharLongStringTest() {
        assertNull(ObjectUtils.convertChar("long"), "convertCharLongStringTest: L136");
    }

    @Test
    public void convertTo0SameClassTest() {
        String testString = "test";
        assertEquals(testString, ObjectUtils.convertTo0(String.class, testString), "convertTo0SameClassTest: L142");
    }

    @Test
    public void convertTo0NullTest() {
        assertNull(ObjectUtils.convertTo0(String.class, null), "convertTo0NullTest: L147");
    }

    @Test
    public void convertTo0VoidClassTest() {
        assertNull(ObjectUtils.convertTo0(Void.class, "anyValue"), "convertTo0VoidClassTest: L152");
    }

    @Test
    public void convertTo0ToStringTest() {
        Object testObject = new Object();
        assertEquals(testObject.toString(), ObjectUtils.convertTo0(String.class, testObject), "convertTo0ToStringTest: L158");
    }

    @Test
    public void convertTo0ToNumberTest() {
        assertEquals(Integer.valueOf(10), ObjectUtils.convertTo0(Integer.class, "10"), "convertTo0ToNumberTest: L163");
    }

    @Test
    public void convertTo0ToCharacterTest() {
        assertEquals(Character.valueOf('a'), ObjectUtils.convertTo0(Character.class, "a"), "convertTo0ToCharacterTest: L168");
    }

    @Test
    public void convertTo0ToCharSequenceUsingParserTest() {
        assertEquals("test", ObjectUtils.convertTo0(String.class, "test"), "convertTo0ToCharSequenceUsingParserTest: L173");
    }

    @Test
    public void convertTo0ToDateFromLongTest() {
        long time = System.currentTimeMillis();
        Date expectedDate = new Date(time);
        assertEquals(expectedDate, ObjectUtils.convertTo0(Date.class, time), "convertTo0ToDateFromLongTest: L180");
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
        assertEquals(exception, cce.getCause(), "asCCETest: L192");
    }

    @Test
    public void sizeOfMapTest() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        assertEquals(2, ObjectUtils.sizeOf(map), "sizeOfMapTest: L200");
    }

    @Test
    public void sizeOfUnsupportedTypeTest() {
        assertThrows(UnsupportedOperationException.class,
                () -> ObjectUtils.sizeOf(new Object()),
                "sizeOfUnsupportedTypeTest");
    }

    @Test
    public void convertToNumberTest() {
        assertEquals(1, ObjectUtils.convertToNumber(Integer.class, "1"), "convertToNumberTest: L210");
    }

    @Test
    public void newInstanceWithClassNameTest() {
        RegularClass instance = ObjectUtils.newInstance(RegularClass.class.getName());
        assertNotNull(instance, "newInstanceWithClassNameTest: L216");
    }

    @Test
    public void newInstanceOrNullValidClassTest() {
        RegularClass instance = (RegularClass) ObjectUtils.newInstanceOrNull(RegularClass.class);
        assertNotNull(instance, "newInstanceOrNullValidClassTest: L222");
    }

    @Test
    public void addAllTest() {
        Integer[] result = ObjectUtils.addAll(1, 2, 3);
        assertArrayEquals(new Integer[]{1, 2, 3}, result, "addAllTest: L228");
    }

    @Test
    public void addAllSingleElementTest() {
        Integer[] result = ObjectUtils.addAll(1);
        assertArrayEquals(new Integer[]{1}, result, "addAllSingleElementTest: L234");
    }

    @Test
    public void getAllInterfacesTest() {
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(new ImplementingClass());
        assertEquals("[interface net.openhft.chronicle.core.util.IgnoresEverything]",
                Arrays.toString(interfaces),
                "getAllInterfacesTest: L240");
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
        assertEquals(RegularClass.class, impl, "implementationToUseNonInterfaceTest: L252");
    }

    @Test
    public void testDefaultValueForPrimitives() {
        assertEquals(false, ObjectUtils.defaultValue(boolean.class), "testDefaultValueForPrimitives: L257");
        assertEquals((byte) 0, (byte) ObjectUtils.defaultValue(byte.class), "testDefaultValueForPrimitives: L258");
        assertEquals((short) 0, (short) ObjectUtils.defaultValue(short.class), "testDefaultValueForPrimitives: L259");
        assertEquals((char) 0, (char) ObjectUtils.defaultValue(char.class), "testDefaultValueForPrimitives: L260");
        assertEquals(0, (int) ObjectUtils.defaultValue(int.class), "testDefaultValueForPrimitives: L261");
        assertEquals(0L, (long) ObjectUtils.defaultValue(long.class), "testDefaultValueForPrimitives: L262");
        assertEquals(0.0f, ObjectUtils.defaultValue(float.class), 0.0f, "testDefaultValueForPrimitives: L263");
        assertEquals(0.0d, ObjectUtils.defaultValue(double.class), 0.0d, "testDefaultValueForPrimitives: L264");
    }

    @Test
    public void testDefaultValueForWrapperTypes() {
        assertNull(ObjectUtils.defaultValue(Boolean.class), "testDefaultValueForWrapperTypes: L269");
        assertNull(ObjectUtils.defaultValue(Byte.class), "testDefaultValueForWrapperTypes: L270");
        assertNull(ObjectUtils.defaultValue(Short.class), "testDefaultValueForWrapperTypes: L271");
        assertNull(ObjectUtils.defaultValue(Character.class), "testDefaultValueForWrapperTypes: L272");
        assertNull(ObjectUtils.defaultValue(Integer.class), "testDefaultValueForWrapperTypes: L273");
        assertNull(ObjectUtils.defaultValue(Long.class), "testDefaultValueForWrapperTypes: L274");
        assertNull(ObjectUtils.defaultValue(Float.class), "testDefaultValueForWrapperTypes: L275");
        assertNull(ObjectUtils.defaultValue(Double.class), "testDefaultValueForWrapperTypes: L276");
    }

    @Test
    public void testDefaultValueForCustomObjects() {
        assertNull(ObjectUtils.defaultValue(String.class), "testDefaultValueForCustomObjects: L281");
        assertNull(ObjectUtils.defaultValue(BigDecimal.class), "testDefaultValueForCustomObjects: L282");
    }

    @Test
    public void testDefaultValueForUnsupportedType() {
        assertNull(ObjectUtils.defaultValue(Object.class), "testDefaultValueForUnsupportedType: L287");
    }

    @Test
    public void testDefaultValueWithNullClass() {
        assertNull(ObjectUtils.defaultValue(null), "testDefaultValueWithNullClass: L292");
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
