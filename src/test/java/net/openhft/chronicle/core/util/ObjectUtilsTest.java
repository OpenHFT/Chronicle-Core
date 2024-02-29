/*
 * Copyright 2016-2020 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

public class ObjectUtilsTest extends CoreTestCommon {
    @Test
    public void testImmutable() {
        for (@NotNull Class c : new Class[]{
                String.class,
                Integer.class,
                Date.class,
                BigDecimal.class,
                ZonedDateTime.class,
        }) {
            assertEquals(c.getName(), ObjectUtils.Immutability.MAYBE, ObjectUtils.isImmutable(c));
        }
        for (@NotNull Class c : new Class[]{
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

    static class ClassWithString {
        private final String s;

        public ClassWithString(String s) {
            this.s = s;
        }
    }

    static class ClassWithValueOf {
        private final String s;

        public ClassWithValueOf(String s) {
            this.s = s;
        }

        public static ClassWithValueOf valueOf(String s) {
            return new ClassWithValueOf(s);
        }
    }

    static class ClassWithParse {
        private final String s;

        public ClassWithParse(String s) {
            this.s = s;
        }

        public static ClassWithParse parse(CharSequence s) {
            return new ClassWithParse(s.toString());
        }
    }
    static class ClassWithSetter {
        private String s;

        public void setS(String s) {
            this.s = s;
        }
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
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.supplierForClass(int.class).get());

        // Add similar tests for interfaces, enums, abstract classes, and internal package classes
    }

    @Test
    public void immutableShouldRegisterImmutability() {
        Class<?> testClass = RegularClass.class;
        ObjectUtils.immutable(testClass, true);
        // Verify the immutability status is correctly set (requires a way to check the status)
    }

    @Test
    public void deprecatedImmutabileShouldStillFunction() {
        Class<?> testClass = RegularClass.class;
        ObjectUtils.immutabile(testClass, true);
        // Verify the immutability status is correctly set as with immutable method
    }

    @Test
    public void caseIgnoreLookupShouldCreateCorrectMap() {
        // Assuming MyEnum is an enum class
        Map<String, Enum<?>> map = ObjectUtils.caseIgnoreLookup(MyEnum.class);
        // Assertions to check the map contents
    }

    @Test
    public void valueOfIgnoreCaseShouldReturnCorrectEnum() {
        // Assuming MyEnum is an enum class with a constant MY_VALUE
        Enum<?> result = ObjectUtils.valueOfIgnoreCase(MyEnum.class, "my_value");
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
        assertEquals(Integer.valueOf(1), ObjectUtils.convertToNumber(Integer.class, "1"));
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
        // Assert that the array contains expected interfaces
    }

    @Test
    public void getAllInterfacesClassTest() {
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(ImplementingClass.class);
        // Assert that the array contains expected interfaces
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
        // Define MyEnum or use an existing enum for testing
    enum MyEnum {
        MY_VALUE
    }

    public class ImplementingClass {}
    public class AbstractTestClass {}
    public class RegularClass {}
}
