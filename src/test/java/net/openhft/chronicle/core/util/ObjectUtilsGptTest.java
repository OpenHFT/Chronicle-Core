package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.internal.Bootstrap;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.openhft.chronicle.core.util.ObjectUtilsGptTest.MyEnum.CONSTANT_ONE;
import static org.junit.jupiter.api.Assertions.*;

public class ObjectUtilsGptTest {

    @Test
    public void testAsCCE() {
        Exception exception = new Exception("Test");
        ClassCastException cce = ObjectUtils.asCCE(exception);
        assertEquals(exception, cce.getCause(), "Cause of ClassCastException should be the original exception");
    }

    @Test
    public void testConvertToArray() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Integer[] array = ObjectUtils.convertToArray(Integer[].class, list);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, array, "Array should be equal to the original list");
    }

    @Test
    public void testElementType() {
        Class<?> elementType = ObjectUtils.elementType(Integer[].class);
        assertEquals(Integer.class, elementType, "Element type of Integer[] should be Integer");
    }

    @Test
    public void testIteratorFor() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5};
        Iterable<Integer> iterable = Arrays.asList(array);
        assertEquals(5, ObjectUtils.sizeOf(iterable), "Size of the iterable should be 5");
    }

    @Test
    public void testSizeOf() {
        Integer[] array = new Integer[]{1, 2, 3, 4, 5};
        assertEquals(5, ObjectUtils.sizeOf(array), "Size of the array should be 5");
    }

    @Test
    public void testCaseIgnoreLookup() {
        Map<String, Enum<?>> lookupMap = ObjectUtils.caseIgnoreLookup(TestEnum.class);

        // Check that the map is case-insensitive
        assertEquals(TestEnum.VALUE_ONE, lookupMap.get("VALUE_ONE"));
        assertEquals(TestEnum.VALUE_ONE, lookupMap.get("value_one"));
        assertEquals(TestEnum.VALUE_ONE, lookupMap.get("VaLuE_OnE"));
        assertEquals(TestEnum.VALUE_TWO, lookupMap.get("VALUE_TWO"));
        assertEquals(TestEnum.VALUE_TWO, lookupMap.get("value_two"));

        // Check that the map contains all enum constants
        assertEquals(2, lookupMap.size());
        assertTrue(lookupMap.containsValue(TestEnum.VALUE_ONE));
        assertTrue(lookupMap.containsValue(TestEnum.VALUE_TWO));
    }

    enum TestEnum {
        VALUE_ONE,
        VALUE_TWO {
            @Override
            public String toString() {
                return super.toString();
            }
        }
    }

    @Test
    public void testImmutable() {
        // Assuming immutable method takes an object and returns a boolean indicating if it's immutable
        ObjectUtils.immutable(SingletonEnum.class, true);
        assertEquals(ObjectUtils.Immutability.YES, ObjectUtils.isImmutable(SingletonEnum.class));
    }

    @Test
    public void testSupplierForClass() {
        // Assuming supplierForClass takes a Class and returns a Supplier for instances of that class
        Supplier<?> stringSupplier = ObjectUtils.supplierForClass(String.class);
        assertTrue(stringSupplier.get() instanceof String);

        Supplier<?> mapSupplier = ObjectUtils.supplierForClass(HashMap.class);
        assertTrue(mapSupplier.get() instanceof HashMap);
    }

    @Test
    public void testValueOfIgnoreCase() {
        // Assuming valueOfIgnoreCase takes an Enum class and a String, and retrieves the enum constant ignoring case
        assertEquals(CONSTANT_ONE, ObjectUtils.valueOfIgnoreCase(MyEnum.class, "constant_one"));
        assertEquals(MyEnum.CONSTANT_TWO, ObjectUtils.valueOfIgnoreCase(MyEnum.class, "CONSTANT_TWO"));
    }

    @Test
    public void testGetSingletonForEnum() {
        // Assuming getSingletonForEnum takes an Enum class and returns an instance of it if it's a singleton enum
        assertNotNull(ObjectUtils.getSingletonForEnum(SingletonEnum.class));
        assertEquals(CONSTANT_ONE, ObjectUtils.getSingletonForEnum(MyEnum.class));
    }

    enum MyEnum {
        CONSTANT_ONE,
        CONSTANT_TWO
    }

    enum SingletonEnum {
        INSTANCE
    }


    @Test
    public void testConvertToNumber() {
        assertEquals(5.0, ObjectUtils.convertToNumber(Double.class, 5));
        assertEquals(5L, ObjectUtils.convertToNumber(Long.class, 5));
        assertEquals(5, ObjectUtils.convertToNumber(Integer.class, 5));
        assertEquals(5.0f, ObjectUtils.convertToNumber(Float.class, 5));
        assertEquals((short) 5, ObjectUtils.convertToNumber(Short.class, 5));
        assertEquals((byte) 5, ObjectUtils.convertToNumber(Byte.class, 5));
        assertEquals(new BigDecimal("5.0"), ObjectUtils.convertToNumber(BigDecimal.class, 5));
        assertEquals(new BigInteger("5"), ObjectUtils.convertToNumber(BigInteger.class, "5"));

        assertEquals(123.45, ObjectUtils.convertToNumber(Double.class, "123.45"));
        assertEquals(12345L, ObjectUtils.convertToNumber(Long.class, "12345"));
        assertEquals(12345, ObjectUtils.convertToNumber(Integer.class, "12345"));
        assertEquals(123.45f, ObjectUtils.convertToNumber(Float.class, "123.45"));
        assertEquals((short) 12345, ObjectUtils.convertToNumber(Short.class, "12345"));
        assertEquals((byte) 123, ObjectUtils.convertToNumber(Byte.class, "123"));
        assertEquals(new BigDecimal("12345.6789"), ObjectUtils.convertToNumber(BigDecimal.class, "12345.6789"));
        assertEquals(new BigInteger("123456789"), ObjectUtils.convertToNumber(BigInteger.class, "123456789"));
    }

    @Test
    public void testUnsupportedConversion() {
        assertThrows(UnsupportedOperationException.class,
                () -> ObjectUtils.convertToNumber(String.class, 5));
    }

    @Test
    public void testNumberFormatException() {
        assertThrows(NumberFormatException.class,
                () -> ObjectUtils.convertToNumber(Integer.class, "invalidNumber"));
    }


    @Test
    public void testNewInstanceWithClassName() {
        // Test for successful instantiation
        ArrayList<?> arrayList = ObjectUtils.newInstance("java.util.ArrayList");
        assertNotNull(arrayList);
        assertTrue(arrayList instanceof ArrayList);

        // Test for unsuccessful instantiation due to incorrect class name
        assertThrows(ClassNotFoundRuntimeException.class, () -> ObjectUtils.newInstance("invalid.ClassName"));
    }

    @Test
    public void testNewInstanceWithClass() {
        // Test for successful instantiation
        Date date = ObjectUtils.newInstance(Date.class);
        assertNotNull(date);
        assertTrue(date instanceof Date);

        // Test for successful instantiation of ArrayList
        ArrayList<?> arrayList = ObjectUtils.newInstance(ArrayList.class);
        assertNotNull(arrayList);
        assertTrue(arrayList instanceof ArrayList);
    }

    @Test
    public void testNewInstanceOrNull() {
        // Test for successful instantiation
        Date date = (Date) ObjectUtils.newInstanceOrNull(Date.class);
        assertNotNull(date);
        assertTrue(date instanceof Date);

        // Test for unsuccessful instantiation due to incorrect class name, should return null
        Object obj = ObjectUtils.newInstanceOrNull(Class.class);
        assertNull(obj);
    }

    @Test
    public void testAddAll() {
        // Test with integers
        Integer[] result = ObjectUtils.addAll(1, 2, 3, 4);
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, result);

        // Test with strings
        String[] stringResult = ObjectUtils.addAll("first", "second", "third");
        assertArrayEquals(new String[]{"first", "second", "third"}, stringResult);

        // Test with a single element
        Integer[] singleElementResult = ObjectUtils.addAll(5);
        assertArrayEquals(new Integer[]{5}, singleElementResult);
    }

    @Test
    public void testMatchingClass() {
        // Test for matching classes
        assertTrue(ObjectUtils.matchingClass(String.class, String.class));
        assertTrue(ObjectUtils.matchingClass( TestEnum.class, TestEnum.VALUE_TWO.getClass()));

        // Test for non-matching classes
        assertFalse(ObjectUtils.matchingClass(Integer.class, String.class));
        assertFalse(ObjectUtils.matchingClass(EnumSet.class, Enum.class));
    }

    interface DummyInterface {
        String sayHello(String name);
    }

    @Test
    public void testDefaultValue() {
        assertNull(ObjectUtils.defaultValue(Object.class));
        assertEquals(0, ObjectUtils.defaultValue(int.class));
        assertEquals(0.0, ObjectUtils.defaultValue(double.class));
        assertEquals(false, ObjectUtils.defaultValue(boolean.class));
        assertEquals('\0', ObjectUtils.defaultValue(char.class));
    }

    @Test
    public void testOnMethodCall() {
        BiFunction<Method, Object[], Object> biFunction = (method, args) -> {
            if ("sayHello".equals(method.getName())) {
                return "Hello, " + args[0];
            }
            return null;
        };

        DummyInterface dummy = ObjectUtils.onMethodCall(biFunction, DummyInterface.class);
        assertEquals("Hello, John", dummy.sayHello("John"));
    }

    @Test
    public void testIsConcreteClass() {
        assertTrue(ObjectUtils.isConcreteClass(String.class));
        assertFalse(ObjectUtils.isConcreteClass(DummyInterface.class));
        assertFalse(ObjectUtils.isConcreteClass(java.io.Serializable.class));
    }


    static class ReadResolveExample implements Serializable {
        private String message;

        public ReadResolveExample(String message) {
            this.message = message;
        }

        // This readResolve method replaces the object after deserialization.
        private Object readResolve() {
            return new ReadResolveExample("Replaced by readResolve");
        }

        public String getMessage() {
            return message;
        }
    }

    static class WithoutReadResolve {
        private String message;

        public WithoutReadResolve(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Test
    void testReadResolve() throws InvocationTargetException, IllegalAccessException {
        ReadResolveExample original = new ReadResolveExample("Original message");
        Object result = ObjectUtils.readResolve(original);

        // Verify that the readResolve method has replaced the original object.
        assertTrue(result instanceof ReadResolveExample);
        assertEquals("Replaced by readResolve", ((ReadResolveExample) result).getMessage());

        WithoutReadResolve withoutReadResolve = new WithoutReadResolve("Without readResolve");
        Object result2 = ObjectUtils.readResolve(withoutReadResolve);

        // Verify that the object without readResolve method remains unchanged.
        assertTrue(result2 instanceof WithoutReadResolve);
        assertEquals("Without readResolve", ((WithoutReadResolve) result2).getMessage());
    }


    interface InterfaceA {
    }

    interface InterfaceB {
    }

    interface InterfaceC extends InterfaceA, InterfaceB {
    }

    static class ClassImplementingC implements InterfaceC, Serializable {
    }

    @Test
    void testGetAllInterfaces() {
        // Test with Class
        Class<?>[] interfaces = ObjectUtils.getAllInterfaces(ClassImplementingC.class);
        List<Class<?>> interfaceList = new ArrayList<>();
        for (Class<?> iface : interfaces) {
            interfaceList.add(iface);
        }

        // ClassImplementingC should have InterfaceA, InterfaceB, InterfaceC and Serializable
        assertTrue(interfaceList.contains(InterfaceA.class));
        assertTrue(interfaceList.contains(InterfaceB.class));
        assertTrue(interfaceList.contains(InterfaceC.class));
        assertTrue(interfaceList.contains(Serializable.class));
        assertEquals(4, interfaceList.size());

        // Test with Object
        ClassImplementingC object = new ClassImplementingC();
        Class<?>[] interfacesForObject = ObjectUtils.getAllInterfaces(object);
        List<Class<?>> interfaceListForObject = new ArrayList<>();
        for (Class<?> iface : interfacesForObject) {
            interfaceListForObject.add(iface);
        }

        // Should be the same results as when using Class
        assertTrue(interfaceListForObject.contains(InterfaceA.class));
        assertTrue(interfaceListForObject.contains(InterfaceB.class));
        assertTrue(interfaceListForObject.contains(InterfaceC.class));
        assertTrue(interfaceListForObject.contains(Serializable.class));
        assertEquals(4, interfaceListForObject.size());
    }

    @Test
    void testGetAllInterfacesWithAccumulator() {
        List<Class<?>> accumulatedInterfaces = new ArrayList<>();
        Function<Class<?>, Boolean> accumulator = (iface) -> {
            accumulatedInterfaces.add(iface);
            return true;
        };

        // Test with Class
        ObjectUtils.getAllInterfaces(ClassImplementingC.class, accumulator);

        // ClassImplementingC should have InterfaceA, InterfaceB, InterfaceC and Serializable
        assertTrue(accumulatedInterfaces.contains(InterfaceA.class));
        assertTrue(accumulatedInterfaces.contains(InterfaceB.class));
        assertTrue(accumulatedInterfaces.contains(InterfaceC.class));
        assertTrue(accumulatedInterfaces.contains(Serializable.class));
        assertEquals(4, accumulatedInterfaces.size());
    }

    @Test
    void testNullAccumulator() {
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.getAllInterfaces(ClassImplementingC.class, null));
    }


    interface CustomInterface {
    }

    interface AnotherCustomInterface {
    }

    static class CustomImplementation implements CustomInterface {
    }

    @Test
    void testDefaultObjectForInterface() {
        ObjectUtils.defaultObjectForInterface(cls -> {
            if (cls == CustomInterface.class) {
                return CustomImplementation.class;
            }
            throw new ClassNotFoundException();
        });

        Class<?> implementationClass = ObjectUtils.implementationToUse(CustomInterface.class);
        assertEquals(CustomImplementation.class, implementationClass);
    }

    @Test
    void testLookForImplEnum() {
        // Standard mappings
        assertEquals(LinkedHashMap.class, ObjectUtils.lookForImplEnum(Map.class));
        assertEquals(LinkedHashSet.class, ObjectUtils.lookForImplEnum(Set.class));
        assertEquals(ArrayList.class, ObjectUtils.lookForImplEnum(List.class));

        // No specific mapping, expect same class back
        assertEquals(AnotherCustomInterface.class, ObjectUtils.lookForImplEnum(AnotherCustomInterface.class));
    }

    @Test
    void testImplementationToUse() {
        // Interface with no default
        assertTrue(AnotherCustomInterface.class == ObjectUtils.implementationToUse(AnotherCustomInterface.class));

        // Interface with default
        ObjectUtils.defaultObjectForInterface(cls -> {
            if (cls == CustomInterface.class) {
                return CustomImplementation.class;
            }
            throw new ClassNotFoundException();
        });
        assertEquals(CustomImplementation.class, ObjectUtils.implementationToUse(CustomInterface.class));

        // Non-interface
        assertEquals(Integer.class, ObjectUtils.implementationToUse(Integer.class));
    }
}