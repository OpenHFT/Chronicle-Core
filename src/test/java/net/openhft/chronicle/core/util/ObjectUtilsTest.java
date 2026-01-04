/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import net.openhft.chronicle.core.onoes.ExceptionHandler;
import net.openhft.chronicle.core.pool.Ecn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("java:S1068")
class ObjectUtilsTest extends CoreTestCommon {
    @Test
    @SuppressWarnings("rawtypes")
    @DisplayName("Immutability lookup reports expected defaults for common types")
    void testImmutable() {
        for (@NotNull Class<?> c : new Class[]{
                String.class,
                Integer.class,
                Date.class,
                BigDecimal.class,
                ZonedDateTime.class,
        }) {
            assertEquals(ObjectUtils.Immutability.MAYBE, ObjectUtils.isImmutable(c),
                    "immutability default should be MAYBE for " + c.getName());
        }
        for (@NotNull Class<?> c : new Class[]{
                // StringBuilder.class, // StringBuilder implements Comparable in Java 11
                ArrayList.class,
                HashMap.class,
        }) {
            assertEquals(ObjectUtils.Immutability.NO, ObjectUtils.isImmutable(c),
                    "immutability default should be NO for " + c.getName());
        }
    }

    @Test
    @DisplayName("Text conversion availability is reported correctly")
    void canConvertTo() {
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
    @DisplayName("Conversion between primitive types returns expected values")
    void testConvert() throws IllegalStateException, IllegalArgumentException {
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1), "integer 1 should convert to character '1'");
        assertEquals('1', (char) ObjectUtils.convertTo(char.class, 1L), "long 1 should convert to character '1'");
        assertEquals(1, (int) ObjectUtils.convertTo(int.class, '1'), "character '1' should convert to integer 1");
        assertEquals(1L, (long) ObjectUtils.convertTo(long.class, '1'), "character '1' should convert to long 1");
        assertEquals(1.0, ObjectUtils.convertTo(double.class, '1'), 0.0, "character '1' should convert to double 1.0");
    }

    @Test
    @DisplayName("Interface without default implementation throws on new instance request")
    void testNoDefaultClassForInterfaceNewInstanceThrows() {
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.newInstance(ExceptionHandler.class),
                "newInstance should throw for interfaces without default implementation");
    }

    @Test
    @DisplayName("Supplier for class handles primitives and regular types")
    void supplierForClassShouldHandleDifferentClassTypes() {
        // Example for a regular class
        Supplier<RegularClass> regularClassSupplier = ObjectUtils.supplierForClass(RegularClass.class);
        assertNotNull(regularClassSupplier.get(), "supplier should create instance of regular class");

        // Example for a primitive type
        Supplier<Integer> integerSupplier = ObjectUtils.supplierForClass(int.class);
        assertThrows(IllegalArgumentException.class, integerSupplier::get,
                "supplier for primitive class should throw on get");
    }

    @Test
    @DisplayName("Registering immutability updates lookup results for class metadata")
    void immutableShouldRegisterImmutability() {
        Class<?> testClass = RegularClass.class;
        ObjectUtils.immutable(testClass, true);
        assertEquals(ObjectUtils.Immutability.YES, ObjectUtils.isImmutable(testClass), "class registered as immutable should return immutability.yes");
    }

    @Test
    @DisplayName("Case insensitive enum lookup builds expected map")
    void caseIgnoreLookupShouldCreateCorrectMap() {
        // Assuming MyEnum is an enum class
        Map<String, Enum<?>> map = ObjectUtils.caseIgnoreLookup(MyEnum.class);
        // Assertions to check the map contents
        assertEquals("{MY_VALUE=MY_VALUE}", map.toString(), "case-insensitive enum lookup map should contain all enum values");
    }

    @Test
    @DisplayName("Case insensitive enum lookup returns correct value")
    void valueOfIgnoreCaseShouldReturnCorrectEnum() {
        // Assuming MyEnum is an enum class with a constant MY_VALUE
        MyEnum result = ObjectUtils.valueOfIgnoreCase(MyEnum.class, "my_value");
        assertEquals(MyEnum.MY_VALUE, result, "lowercase enum name should match enum constant case-insensitively");
    }

    @Test
    @DisplayName("Brace wrapped enum names resolve to singleton instance")
    void valueOfIgnoreCaseHandlesBraceWrappedName() {
        MyEnum result = ObjectUtils.valueOfIgnoreCase(MyEnum.class, "{ignored}");
        assertEquals(MyEnum.MY_VALUE, result, "brace-wrapped enum name should resolve to the singleton enum constant");
    }

    @Test
    @DisplayName("Supplier for internal package throws on use")
    void supplierForInternalPackageTest() {
        Supplier<?> supplier = ObjectUtils.supplierForInternalPackage();
        assertThrows(IllegalArgumentException.class, supplier::get, "supplier for internal package should throw when invoked");
    }

    @Test
    @DisplayName("Enum supplier yields non null constant")
    void supplierForEnumTest() {
        Supplier<MyEnum> supplier = ObjectUtils.supplierForEnum(MyEnum.class);
        assertNotNull(supplier.get(), "enum supplier should create non-null enum value");
    }

    @Test
    @DisplayName("Supplier for abstract class throws on instantiation")
    void supplierForAbstractClassTest() {
        Supplier<AbstractTestClass> supplier = ObjectUtils.supplierForAbstractClass(AbstractTestClass.class);
        assertThrows(IllegalArgumentException.class, supplier::get, "supplier for abstract class should throw when attempting instantiation");
    }

    @Test
    @DisplayName("Single character string converts to char")
    void convertCharSingleCharacterTest() {
        assertEquals((Character) 'a', (Character) ObjectUtils.convertChar("a"), "single-character string should convert to character");
    }

    @Test
    @DisplayName("Multi character string converts to null")
    void convertCharLongStringTest() {
        assertNull(ObjectUtils.convertChar("long"), "multi-character string should return null when converting to char");
    }

    @Test
    @DisplayName("Same class conversion keeps original instance")
    void convertTo0SameClassTest() {
        String testString = "test";
        assertEquals(testString, ObjectUtils.convertTo0(String.class, testString), "converting to same class should return original instance");
    }

    @Test
    @DisplayName("Null conversion uses fallback path for defaults")
    void convertTo0NullTest() {
        assertNull(ObjectUtils.convertTo0(String.class, null), "null input should return null conversion result for target type");
    }

    @Test
    @DisplayName("Void class conversion yields null marker output")
    void convertTo0VoidClassTest() {
        assertNull(ObjectUtils.convertTo0(Void.class, "anyValue"), "converting to void class should always return null");
    }

    @Test
    @DisplayName("Object converts to string using toString")
    void convertTo0ToStringTest() {
        Object testObject = new Object();
        assertEquals(testObject.toString(), ObjectUtils.convertTo0(String.class, testObject), "object should convert to string using tostring method");
    }

    @Test
    @DisplayName("Numeric string converts to integer value")
    void convertTo0ToNumberTest() {
        assertEquals(10, ObjectUtils.convertTo0(Integer.class, "10"), "numeric string should parse to integer");
    }

    @Test
    @DisplayName("Single character converts to Character wrapper")
    void convertTo0ToCharacterTest() {
        assertEquals('a', ObjectUtils.convertTo0(Character.class, "a"), "single-character string should convert to character wrapper");
    }

    @Test
    @DisplayName("String conversion uses CharSequence parser for strings")
    void convertTo0ToCharSequenceUsingParserTest() {
        assertEquals("test", ObjectUtils.convertTo0(String.class, "test"), "string to string conversion should return identical value");
    }

    @Test
    @DisplayName("Set conversion removes duplicates and preserves order")
    void convertTo0SetConversionPreservesOrder() {
        List<String> values = Arrays.asList("a", "b", "a");
        @SuppressWarnings("unchecked")
        Set<String> result = ObjectUtils.convertTo(Set.class, values);
        assertEquals(2, result.size(), "set conversion should remove duplicate elements");
        assertTrue(result.contains("a"), "set conversion result " + result + " should include 'a'");
        assertTrue(result.contains("b"), "set conversion result " + result + " should include 'b'");
    }

    @Test
    @DisplayName("Long timestamp converts to Date instance")
    void convertTo0ToDateFromLongTest() {
        long time = System.currentTimeMillis();
        Date expectedDate = new Date(time);
        assertEquals(expectedDate, ObjectUtils.convertTo0(Date.class, time), "timestamp long should convert to date with same milliseconds");
    }

    @Test
    @DisplayName("Unsupported conversion throws ClassCastException during conversion")
    void convertTo0UnsupportedConversionTest() {
        assertThrows(ClassCastException.class,
                () -> ObjectUtils.convertTo0(Map.class, "test"),
                "unsupported conversion should raise ClassCastException");
    }

    @Test
    @DisplayName("asCCE wraps exception in ClassCastException with cause")
    void asCCETest() {
        Exception exception = new Exception("Test exception");
        ClassCastException cce = ObjectUtils.asCCE(exception);
        assertEquals(exception, cce.getCause(), "wrapped exception should be preserved as cause of classcastexception");
    }

    @Test
    @DisplayName("sizeOf reports map entry count for maps")
    void sizeOfMapTest() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        assertEquals(2, ObjectUtils.sizeOf(map), "map with two entries should report size of 2");
    }

    @Test
    @DisplayName("sizeOf throws exception for unsupported types")
    void sizeOfUnsupportedTypeTest() {
        assertThrows(UnsupportedOperationException.class,
                () -> ObjectUtils.sizeOf(new Object()),
                "unsupported type should raise UnsupportedOperationException");
    }

    @Test
    @DisplayName("String converts to numeric type value correctly")
    void convertToNumberTest() {
        assertEquals(1, ObjectUtils.convertToNumber(Integer.class, "1"), "string '1' should convert to integer 1");
    }

    @Test
    @DisplayName("Class name instantiation creates reflection object")
    void newInstanceWithClassNameTest() {
        RegularClass instance = ObjectUtils.newInstance(RegularClass.class.getName());
        assertNotNull(instance, "new instance should be created from class name string");
    }

    @Test
    @DisplayName("Factory creates concrete type instance from supplier")
    void newInstanceOrNullValidClassTest() {
        RegularClass instance = (RegularClass) ObjectUtils.newInstanceOrNull(RegularClass.class);
        assertNotNull(instance, "new instance should be created from valid class constructor input");
    }

    @Test
    @DisplayName("addAll combines varargs into array in order")
    void addAllTest() {
        Integer[] result = ObjectUtils.addAll(1, 2, 3);
        assertArrayEquals(new Integer[]{1, 2, 3}, result, "varargs should combine into array with same elements in order");
    }

    @Test
    @DisplayName("addAll with single element returns array")
    void addAllSingleElementTest() {
        Integer[] result = ObjectUtils.addAll(1);
        assertArrayEquals(new Integer[]{1}, result, "single vararg should create single-element array");
    }

    @Test
    @DisplayName("getAllInterfaces returns interface hierarchy for implementation")
    void getAllInterfacesTest() {
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(new ImplementingClass());
        assertEquals("[interface net.openhft.chronicle.core.util.IgnoresEverything]",
                Arrays.toString(interfaces),
                "implementing class should report all interfaces in hierarchy");
    }

    @Test
    @DisplayName("Interface collection rejects null accumulator argument")
    void getAllInterfacesWithNullAccumulatorTest() {
        assertThrows(IllegalArgumentException.class,
                () -> ObjectUtils.getAllInterfaces(new ImplementingClass(), null),
                "null accumulator should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Implementation lookup keeps concrete type selection")
    void implementationToUseNonInterfaceTest() {
        Class<?> impl = ObjectUtils.implementationToUse(RegularClass.class);
        assertEquals(RegularClass.class, impl, "concrete class should return itself as implementation");
    }

    @Test
    @DisplayName("Default values for primitives match expectations")
    void testDefaultValueForPrimitives() {
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
    @DisplayName("Default values for wrapper types are null")
    void testDefaultValueForWrapperTypes() {
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
    @DisplayName("Default values for object types are null")
    void testDefaultValueForCustomObjects() {
        assertNull(ObjectUtils.defaultValue(String.class), "string class default should be null");
        assertNull(ObjectUtils.defaultValue(BigDecimal.class), "bigdecimal class default should be null");
    }

    @Test
    @DisplayName("Unsupported type default yields null sentinel")
    void testDefaultValueForUnsupportedType() {
        assertNull(ObjectUtils.defaultValue(Object.class), "object class default should be null");
    }

    @Test
    @DisplayName("Null class parameter yields default null sentinel")
    void testDefaultValueWithNullClass() {
        assertNull(ObjectUtils.defaultValue(null), "null class parameter should return null default value");
    }

    @Test
    @DisplayName("requireNonNull with valid value returns same argument value")
    void requireNonNullWithValidValue() {
        String value = "test";
        assertEquals(value, ObjectUtils.requireNonNull(value), "requireNonNull should return the same non-null value");
    }

    @Test
    @DisplayName("ObjectUtils requireNonNull throws NullPointerException for null input argument")
    void requireNonNullWithNull() {
        assertThrows(NullPointerException.class, () -> ObjectUtils.requireNonNull(null),
                "requireNonNull should throw NullPointerException for null input");
    }

    @Test
    @DisplayName("sizeOf reports collection size for lists")
    void sizeOfListTest() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals(3, ObjectUtils.sizeOf(list), "list with three elements should report size of 3");
    }

    @Test
    @DisplayName("sizeOf reports array length for arrays")
    void sizeOfArrayTest() {
        String[] array = {"a", "b", "c", "d"};
        assertEquals(4, ObjectUtils.sizeOf(array), "array with four elements should report size of 4");
    }

    @Test
    @DisplayName("Boolean string converts to boolean value type")
    void convertToBooleanTest() {
        assertTrue(ObjectUtils.convertTo(Boolean.class, "true"), "string 'true' should convert to boolean true");
        assertFalse(ObjectUtils.convertTo(Boolean.class, "false"), "string 'false' should convert to boolean false");
    }

    @Test
    @DisplayName("Number converts to different numeric types")
    void convertNumberToNumberTest() {
        assertEquals(10L, (long) ObjectUtils.convertTo(Long.class, 10), "integer 10 should convert to long 10");
        assertEquals(10.0, (double) ObjectUtils.convertTo(Double.class, 10), 0.001, "integer 10 should convert to double 10.0");
        assertEquals(10.0f, (float) ObjectUtils.convertTo(Float.class, 10), 0.001f, "integer 10 should convert to float 10.0");
    }

    @Test
    @DisplayName("Byte array converts from string value")
    void convertToByteArrayTest() {
        byte[] result = ObjectUtils.convertTo(byte[].class, "hello");
        assertNotNull(result, "string should convert to byte array");
        assertTrue(result.length > 0,
                "converted byte array length " + result.length + " should be > 0");
    }

    @Test
    @DisplayName("convertTo returns null when conversion parameter for target is null")
    void convertToNullInput() {
        assertNull(ObjectUtils.convertTo(String.class, null), "null input should return null for any target type");
    }

    @Test
    @DisplayName("Class from string name lookup works for known classes")
    void convertToClassTest() {
        assertEquals(String.class, ObjectUtils.convertTo(Class.class, "java.lang.String"),
                "class name string should convert to corresponding class object");
    }

    @Test
    @DisplayName("convertTo converts UUID from string representation")
    void convertToUUIDTest() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, ObjectUtils.convertTo(UUID.class, uuid.toString()),
                "UUID string should convert back to same UUID");
    }

    @Test
    @DisplayName("convertTo converts byte from numeric value")
    void convertToByteTest() {
        assertEquals((byte) 42, (byte) ObjectUtils.convertTo(Byte.class, 42),
                "integer should convert to byte");
        assertEquals((byte) 127, (byte) ObjectUtils.convertTo(byte.class, "127"),
                "string should convert to primitive byte");
    }

    @Test
    @DisplayName("convertTo converts short from numeric value")
    void convertToShortTest() {
        assertEquals((short) 1234, (short) ObjectUtils.convertTo(Short.class, 1234),
                "integer should convert to short");
        assertEquals((short) 32000, (short) ObjectUtils.convertTo(short.class, "32000"),
                "string should convert to primitive short");
    }

    @Test
    @DisplayName("convertTo converts double from numeric and string sources")
    void convertToDoubleTest() {
        assertEquals(3.14159, ObjectUtils.convertTo(Double.class, "3.14159"), 0.00001,
                "string should convert to double");
        assertEquals(100.0, ObjectUtils.convertTo(double.class, 100L), 0.0,
                "long should convert to primitive double");
    }

    @Test
    @DisplayName("convertTo converts float from numeric and string sources")
    void convertToFloatTest() {
        assertEquals(2.71828f, ObjectUtils.convertTo(Float.class, "2.71828"), 0.0001f,
                "string should convert to float");
        assertEquals(50.0f, ObjectUtils.convertTo(float.class, 50), 0.0f,
                "integer should convert to primitive float");
    }

    @Test
    @DisplayName("convertTo converts enum from string name")
    void convertToEnumTest() {
        assertEquals(MyEnum.MY_VALUE, ObjectUtils.convertTo(MyEnum.class, "MY_VALUE"),
                "string should convert to enum constant");
    }

    @Test
    @DisplayName("Number to character conversion via digit value")
    void convertNumberToCharTest() {
        assertEquals('5', (char) ObjectUtils.convertTo(char.class, 5),
                "integer 5 should convert to character '5'");
        assertEquals('9', (char) ObjectUtils.convertTo(Character.class, 9L),
                "long 9 should convert to character '9'");
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

    // --- Additional tests for branch coverage ---

    @Test
    @DisplayName("ObjectUtils.isTrue returns true for 't' token input")
    void isTrueSingleCharT() {
        assertTrue(ObjectUtils.isTrue("t"), "lowercase 't' should be true");
        assertTrue(ObjectUtils.isTrue("T"), "uppercase 'T' should be true");
    }

    @Test
    @DisplayName("ObjectUtils.isTrue returns true for 'y' token input")
    void isTrueSingleCharY() {
        assertTrue(ObjectUtils.isTrue("y"), "lowercase 'y' should be true");
        assertTrue(ObjectUtils.isTrue("Y"), "uppercase 'Y' should be true");
    }

    @Test
    @DisplayName("ObjectUtils.isTrue returns true for 'yes' token input")
    void isTrueYesString() {
        assertTrue(ObjectUtils.isTrue("yes"), "lowercase 'yes' should be true");
        assertTrue(ObjectUtils.isTrue("YES"), "uppercase 'YES' should be true");
        assertTrue(ObjectUtils.isTrue("Yes"), "mixed case 'Yes' should be true");
    }

    @Test
    @DisplayName("ObjectUtils.isTrue returns true for 'true' token string input")
    void isTrueTrueString() {
        assertTrue(ObjectUtils.isTrue("true"), "isTrue should return true for lowercase 'true' token");
        assertTrue(ObjectUtils.isTrue("TRUE"), "isTrue should return true for uppercase 'TRUE' token");
    }

    @Test
    @DisplayName("ObjectUtils.isTrue returns false when input string parameter is null")
    void isTrueNull() {
        assertFalse(ObjectUtils.isTrue(null), "isTrue should return false when input string is null");
    }

    @Test
    @DisplayName("ObjectUtils.isTrue returns false for non-true token strings")
    void isTrueOtherStrings() {
        assertFalse(ObjectUtils.isTrue("x"), "isTrue should return false for single char 'x' token");
        assertFalse(ObjectUtils.isTrue("no"), "isTrue should return false for two char 'no' token");
        assertFalse(ObjectUtils.isTrue("maybe"), "isTrue should return false for non-true token 'maybe'");
        assertFalse(ObjectUtils.isTrue("yess"), "isTrue should return false for non-true token 'yess'");
    }

    @Test
    @DisplayName("ObjectUtils.isFalse returns true for 'f' token input")
    void isFalseSingleCharF() {
        assertTrue(ObjectUtils.isFalse("f"), "isFalse should return true for lowercase 'f' token");
        assertTrue(ObjectUtils.isFalse("F"), "isFalse should return true for uppercase 'F' token");
    }

    @Test
    @DisplayName("ObjectUtils.isFalse returns true for 'n' token input")
    void isFalseSingleCharN() {
        assertTrue(ObjectUtils.isFalse("n"), "isFalse should return true for lowercase 'n' token");
        assertTrue(ObjectUtils.isFalse("N"), "isFalse should return true for uppercase 'N' token");
    }

    @Test
    @DisplayName("ObjectUtils.isFalse returns true for 'no' token string input")
    void isFalseNoString() {
        assertTrue(ObjectUtils.isFalse("no"), "isFalse should return true for lowercase 'no' token");
        assertTrue(ObjectUtils.isFalse("NO"), "isFalse should return true for uppercase 'NO' token");
        assertTrue(ObjectUtils.isFalse("No"), "isFalse should return true for mixed case 'No' token");
    }

    @Test
    @DisplayName("ObjectUtils.isFalse returns true for 'false' token string input")
    void isFalseFalseString() {
        assertTrue(ObjectUtils.isFalse("false"), "isFalse should return true for lowercase 'false' token");
        assertTrue(ObjectUtils.isFalse("FALSE"), "isFalse should return true for uppercase 'FALSE' token");
    }

    @Test
    @DisplayName("ObjectUtils.isFalse returns false when input string parameter is null")
    void isFalseNull() {
        assertFalse(ObjectUtils.isFalse(null), "isFalse should return false when input string is null");
    }

    @Test
    @DisplayName("ObjectUtils.isFalse returns false for non-false token strings")
    void isFalseOtherStrings() {
        assertFalse(ObjectUtils.isFalse("x"), "isFalse should return false for single char 'x' token");
        assertFalse(ObjectUtils.isFalse("yes"), "isFalse should return false for non-false token 'yes'");
        assertFalse(ObjectUtils.isFalse("maybe"), "isFalse should return false for non-false token 'maybe'");
    }

    @Test
    @DisplayName("toBoolean returns null when conversion string parameter is null")
    void toBooleanNull() {
        assertNull(ObjectUtils.toBoolean(null), "toBoolean should return null when input string is null");
    }

    @Test
    @DisplayName("toBoolean returns null for empty or whitespace string parameter")
    void toBooleanEmpty() {
        assertNull(ObjectUtils.toBoolean(""), "toBoolean should return null for empty string input");
        assertNull(ObjectUtils.toBoolean("   "), "toBoolean should return null for whitespace-only input");
    }

    @Test
    @DisplayName("toBoolean returns TRUE for true-like keyword token strings")
    void toBooleanTrue() {
        assertEquals(Boolean.TRUE, ObjectUtils.toBoolean("true"),
                "toBoolean should return TRUE for 'true' token");
        assertEquals(Boolean.TRUE, ObjectUtils.toBoolean("  yes  "),
                "toBoolean should trim whitespace and return TRUE for 'yes'");
    }

    @Test
    @DisplayName("toBoolean returns FALSE for false-like keyword token strings")
    void toBooleanFalse() {
        assertEquals(Boolean.FALSE, ObjectUtils.toBoolean("false"),
                "toBoolean should return FALSE for 'false' token");
        assertEquals(Boolean.FALSE, ObjectUtils.toBoolean("  no  "),
                "toBoolean should trim whitespace and return FALSE for 'no'");
    }

    @Test
    @DisplayName("toBoolean returns FALSE for unknown keyword token strings")
    void toBooleanUnknown() {
        assertEquals(Boolean.FALSE, ObjectUtils.toBoolean("maybe"),
                "toBoolean should return FALSE for unknown token 'maybe'");
        assertEquals(Boolean.FALSE, ObjectUtils.toBoolean("unknown"),
                "toBoolean should return FALSE for unknown token 'unknown'");
    }

    @Test
    @DisplayName("convertChar returns null character for empty string parameter")
    void convertCharEmpty() {
        assertEquals('\0', (char) ObjectUtils.convertChar(""),
                "convertChar should return null character for empty string input");
    }

    @Test
    @DisplayName("lookForImplEnum returns LinkedHashMap for Map interface")
    void lookForImplEnumMap() {
        assertEquals(LinkedHashMap.class, ObjectUtils.lookForImplEnum(Map.class),
                "lookForImplEnum should map Map interface to LinkedHashMap");
    }

    @Test
    @DisplayName("lookForImplEnum maps Set interface to LinkedHashSet")
    void lookForImplEnumSet() {
        assertEquals(LinkedHashSet.class, ObjectUtils.lookForImplEnum(Set.class),
                "lookForImplEnum should map Set interface to LinkedHashSet");
    }

    @Test
    @DisplayName("lookForImplEnum returns ArrayList for List interface")
    void lookForImplEnumList() {
        assertEquals(ArrayList.class, ObjectUtils.lookForImplEnum(List.class),
                "lookForImplEnum should map List interface to ArrayList");
    }

    @Test
    @DisplayName("lookForImplEnum returns input class for non-interface concrete types")
    void lookForImplEnumNonInterface() {
        assertEquals(String.class, ObjectUtils.lookForImplEnum(String.class),
                "lookForImplEnum should return input class for non-interface type");
    }

    @Test
    @DisplayName("convertToNumber converts numeric inputs to target type")
    void convertToNumberFromNumber() {
        assertEquals(10L, ObjectUtils.convertToNumber(Long.class, 10),
                "convertToNumber should convert Integer 10 to Long 10");
        assertEquals(3.14, (Double) ObjectUtils.convertToNumber(Double.class, 3.14f), 0.01,
                "convertToNumber should convert Float 3.14f to Double 3.14");
    }

    @Test
    @DisplayName("convertToNumber handles BigDecimal conversion from Long")
    void convertToNumberBigDecimalFromLong() {
        Number result = ObjectUtils.convertToNumber(BigDecimal.class, 123L);
        assertEquals(new BigDecimal("123"), result,
                "convertToNumber should convert Long 123 to BigDecimal 123");
    }

    @Test
    @DisplayName("convertToNumber handles BigDecimal conversion from Double")
    void convertToNumberBigDecimalFromDouble() {
        Number result = ObjectUtils.convertToNumber(BigDecimal.class, 3.14);
        assertEquals(BigDecimal.valueOf(3.14), result,
                "convertToNumber should convert Double 3.14 to BigDecimal 3.14");
    }

    @Test
    @DisplayName("convertToNumber converts integer input to BigInteger")
    void convertToNumberBigInteger() {
        Number result = ObjectUtils.convertToNumber(BigInteger.class, 999);
        assertEquals(new BigInteger("999"), result,
                "convertToNumber should convert Integer 999 to BigInteger 999");
    }

    @Test
    @DisplayName("convertToNumber converts string input to numeric type")
    void convertToNumberFromString() {
        assertEquals(42, ObjectUtils.convertToNumber(Integer.class, "42"),
                "convertToNumber should convert string '42' to Integer 42");
        assertEquals(3.14, (Double) ObjectUtils.convertToNumber(Double.class, "3.14"), 0.001,
                "convertToNumber should convert string '3.14' to Double 3.14");
    }

    @Test
    @DisplayName("convertToNumber throws for unsupported target type")
    void convertToNumberUnsupported() {
        assertThrows(UnsupportedOperationException.class,
                () -> ObjectUtils.convertToNumber(Object.class, 123),
                "convertToNumber should throw for unsupported target type");
    }

    @Test
    @DisplayName("getSingletonForEnum returns first constant for multi-value enum")
    void getSingletonForEnumMultiValue() {
        expectException("has multiple INSTANCEs");
        assertEquals(MultiValueEnum.FIRST, ObjectUtils.getSingletonForEnum(MultiValueEnum.class),
                "getSingletonForEnum should return first constant when multiple exist");
    }

    @Test
    @DisplayName("getSingletonForEnum throws AssertionError for empty enum")
    void getSingletonForEnumEmpty() {
        assertThrows(AssertionError.class,
                () -> ObjectUtils.getSingletonForEnum(EmptyEnum.class),
                "getSingletonForEnum should throw AssertionError for empty enum");
    }

    @Test
    @DisplayName("getAllInterfaces returns interface list for interface input type")
    void getAllInterfacesForInterface() {
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(Runnable.class);
        assertTrue(interfaces.length > 0,
                "getAllInterfaces should return at least one interface for Runnable, count=" + interfaces.length);
    }

    @Test
    @DisplayName("getAllInterfaces ignores null class parameter and leaves interface collection empty")
    void getAllInterfacesNull() {
        Set<Class<?>> result = new HashSet<>();
        ObjectUtils.getAllInterfaces(null, result::add);
        assertTrue(result.isEmpty(), "getAllInterfaces should not add interfaces when input is null");
    }

    @Test
    @DisplayName("getAllInterfaces with accumulator returning false stops traversal")
    void getAllInterfacesAccumulatorFalse() {
        Set<Class<?>> result = new HashSet<>();
        // Accumulator always returns false - should still process but not recurse
        ObjectUtils.getAllInterfaces(Comparable.class, c -> {
            result.add(c);
            return Boolean.FALSE;
        });
        assertEquals(1, result.size(),
                "getAllInterfaces should stop recursion when accumulator returns false");
    }

    @Test
    @DisplayName("primToWrapper maps primitive types to wrapper classes")
    void primToWrapperPrimitives() {
        assertEquals(Boolean.class, ObjectUtils.primToWrapper(boolean.class),
                "primToWrapper should map boolean.class to Boolean.class");
        assertEquals(Byte.class, ObjectUtils.primToWrapper(byte.class),
                "primToWrapper should map byte.class to Byte.class");
        assertEquals(Character.class, ObjectUtils.primToWrapper(char.class),
                "primToWrapper should map char.class to Character.class");
        assertEquals(Short.class, ObjectUtils.primToWrapper(short.class),
                "primToWrapper should map short.class to Short.class");
        assertEquals(Integer.class, ObjectUtils.primToWrapper(int.class),
                "primToWrapper should map int.class to Integer.class");
        assertEquals(Long.class, ObjectUtils.primToWrapper(long.class),
                "primToWrapper should map long.class to Long.class");
        assertEquals(Float.class, ObjectUtils.primToWrapper(float.class),
                "primToWrapper should map float.class to Float.class");
        assertEquals(Double.class, ObjectUtils.primToWrapper(double.class),
                "primToWrapper should map double.class to Double.class");
        assertEquals(Void.class, ObjectUtils.primToWrapper(void.class),
                "primToWrapper should map void.class to Void.class");
    }

    @Test
    @DisplayName("primToWrapper returns same class for non-primitive parameter type")
    void primToWrapperNonPrimitive() {
        assertEquals(String.class, ObjectUtils.primToWrapper(String.class),
                "primToWrapper should return String.class for String.class input");
        assertEquals(Object.class, ObjectUtils.primToWrapper(Object.class),
                "primToWrapper should return Object.class for Object.class input");
    }

    @Test
    @DisplayName("sizeOf reports length for primitive arrays")
    void sizeOfPrimitiveArray() {
        int[] intArray = {1, 2, 3, 4, 5};
        assertEquals(5, ObjectUtils.sizeOf(intArray), "sizeOf should report length 5 for int array");
    }

    @Test
    @DisplayName("matchingClass returns true for identical class comparison types")
    void matchingClassSame() {
        assertTrue(ObjectUtils.matchingClass(String.class, String.class),
                "matchingClass should return true for identical classes");
    }

    @Test
    @DisplayName("matchingClass returns false for differing class comparison types")
    void matchingClassDifferent() {
        assertFalse(ObjectUtils.matchingClass(String.class, Integer.class),
                "matchingClass should return false for different classes");
    }

    @Test
    @DisplayName("isConcreteClass returns true for concrete implementation class types")
    void isConcreteClassTrue() {
        assertTrue(ObjectUtils.isConcreteClass(String.class), "isConcreteClass should return true for String.class");
        assertTrue(ObjectUtils.isConcreteClass(ArrayList.class),
                "isConcreteClass should return true for ArrayList.class");
    }

    @Test
    @DisplayName("isConcreteClass returns false for abstract classes and interfaces")
    void isConcreteClassFalse() {
        assertFalse(ObjectUtils.isConcreteClass(List.class), "isConcreteClass should return false for List.class");
        assertFalse(ObjectUtils.isConcreteClass(AbstractList.class),
                "isConcreteClass should return false for AbstractList.class");
    }

    @Test
    @DisplayName("implementationToUse returns concrete impl for Map interface")
    void implementationToUseMap() {
        Class<?> impl = ObjectUtils.implementationToUse(Map.class);
        assertEquals(LinkedHashMap.class, impl, "implementationToUse should map Map to LinkedHashMap");
    }

    @Test
    @DisplayName("implementationToUse returns concrete impl for List interface")
    void implementationToUseList() {
        Class<?> impl = ObjectUtils.implementationToUse(List.class);
        assertEquals(ArrayList.class, impl, "implementationToUse should map List to ArrayList");
    }

    @Test
    @DisplayName("implementationToUse returns same class for concrete types")
    void implementationToUseConcrete() {
        Class<?> impl = ObjectUtils.implementationToUse(HashMap.class);
        assertEquals(HashMap.class, impl, "implementationToUse should return HashMap.class for HashMap input");
    }

    @Test
    @DisplayName("convertTo handles array conversion from list")
    void convertToArrayFromList() {
        List<String> list = Arrays.asList("a", "b", "c");
        String[] result = ObjectUtils.convertTo(String[].class, list);
        assertArrayEquals(new String[]{"a", "b", "c"}, result,
                "convertTo should convert List<String> to String array");
    }

    @Test
    @DisplayName("convertTo handles array conversion from array")
    void convertToArrayFromArray() {
        Object[] source = {"x", "y"};
        String[] result = ObjectUtils.convertTo(String[].class, source);
        assertArrayEquals(new String[]{"x", "y"}, result,
                "convertTo should convert Object array to String array");
    }

    @Test
    @DisplayName("onMethodCall creates proxy that implements interface methods")
    void onMethodCallProxy() {
        Runnable proxy = ObjectUtils.onMethodCall(
                (method, args) -> null,
                Runnable.class
        );
        assertNotNull(proxy, "onMethodCall should return non-null proxy instance");
        assertDoesNotThrow(proxy::run, "Proxy run invocation should not throw");
    }

    @Test
    @DisplayName("onMethodCall proxy delegates Object method calls")
    void onMethodCallProxyObjectMethods() {
        TestComparable proxy = ObjectUtils.onMethodCall(
                (method, args) -> 0,
                TestComparable.class
        );
        assertNotNull(proxy.toString(), "Proxy toString should return non-null result");
        assertNotNull(proxy.hashCode(), "Proxy hashCode should return non-null result");
    }

    interface TestComparable extends Comparable<TestComparable> {
    }

    enum MultiValueEnum {
        FIRST, SECOND, THIRD
    }

    enum EmptyEnum {
        // intentionally empty for testing
    }
}
