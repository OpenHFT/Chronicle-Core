/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenericReflectionTest extends CoreTestCommon {

    @DisplayName("getReturnType behaviour under expected input and output conditions")
    @Test
    void getReturnType() {
        assertEquals(String.class,
                GenericReflection.getReturnType(Returns.class.getMethods()[0], ReturnsString.class), "return type should be resolved to String from generic interface");

        assertEquals(Integer.class,
                GenericReflection.getReturnType(Returns.class.getMethods()[0], ReturnsInteger.class), "return type should be resolved to Integer from generic interface");

        assertEquals(Double.class,
                GenericReflection.getReturnType(Returns2.class.getMethods()[0], Returns2Double.class), "return type should be resolved to Double from generic class");

        Type t = GenericReflection.getReturnType(ReturnsReturnsAString.class.getMethods()[0], ReturnsReturnsAString.class);
        assertEquals(new TypeOf<Returns<String>>() {
        }.type(), t, "return type should be resolved to Returns<String> parameterized type");
    }

    @DisplayName("getMethodReturnTypes behaviour under expected input and output conditions")
    @Test
    void getMethodReturnTypes() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Set<Type> methodReturnTypes = GenericReflection.getMethodReturnTypes(ReturnsString.class);
        assertEquals(methodReturnTypes,
                GenericReflection.getMethodReturnTypes(returnString), "method return types should match between class and parameterized type");
    }

    @DisplayName("getGenericClassesSuperclassesAndInterfaces behaviour under expected input and output conditions")
    @Test
    void getGenericClassesSuperclassesAndInterfaces() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Object[] genericInterfaces = GenericReflection.getGenericClassesSuperclassesAndInterfaces(ReturnsString.class).toArray();
        assertArrayEquals(genericInterfaces,
                GenericReflection.getGenericClassesSuperclassesAndInterfaces(returnString).toArray(), "generic hierarchy should match between class and parameterized type");
    }

    @DisplayName("getParameterTypes behaviour under expected input and output conditions")
    @Test
    void getParameterTypes() throws NoSuchMethodException {
        final Method method = GenericMethod.class.getDeclaredMethod("method", Object.class, Object.class);
        final String expected = "[class java.lang.Byte, class java.lang.Short]";
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, ExtendsGenericMethod.class)), "parameter types should resolve to Byte and Short in extending interface");
        final Method omethod = OverridesGenericMethod.class.getDeclaredMethod("method", Byte.class, Short.class);
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(omethod, OverridesGenericMethod.class)), "parameter types should resolve to Byte and Short in overriding method");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, OverridesGenericMethod.class)), "parameter types should resolve correctly with overriding interface");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, NestedExtendsGenericMethod.class)), "parameter types should resolve through nested interface hierarchy");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, OverlyNestedExtendsGenericMethod.class)), "parameter types should resolve through overly nested interface hierarchy");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, MassivelyNestedExtendsGenericMethod.class)), "parameter types should resolve through massively nested interface hierarchy");

        final Method method0 = OverlyNestedExtendsGenericMethod.class.getDeclaredMethod("method0", Object.class);
        assertEquals("[class java.lang.Long]",
                Arrays.toString(GenericReflection.getParameterTypes(method0, MassivelyNestedExtendsGenericMethod.class)), "parameter type should resolve to Long in nested generic method");
    }

    @DisplayName("getParameterTypesExtends behaviour under expected input and output conditions")
    @Test
    void getParameterTypesExtends() {
        Method method = null;
        for (Method m : GenericMethodExtends.class.getMethods()) {
            if (m.getName().equals("method")) {
                method = m;
                break;
            }
        }
        assertNotNull(method, "reflection should find method");
        final String expected = "[" + Number.class + ", " + CharSequence.class + "]";
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, GenericMethodExtends.class)), "parameter types should resolve to bounded type parameters Number and CharSequence");
    }

    interface Returns<A> {
        A ret();
    }

    interface ReturnsReturnsAString {
        Returns<String> retRS();
    }

    private interface ReturnsString extends Returns<String> {
    }

    interface GenericMethodExtends<A extends Number, B extends CharSequence> {
        void method(A a, B b);
    }

    interface GenericMethod<A, B> {
        @SuppressWarnings("EmptyMethod")
        void method(A a, B b);
    }

    interface ExtendsGenericMethod extends GenericMethod<Byte, Short> {

    }

    interface OverridesGenericMethod extends GenericMethod<Byte, Short> {
        @Override
        void method(Byte b, Short s);
    }

    interface NestedExtendsGenericMethod extends ExtendsGenericMethod {

    }

    interface OverlyNestedExtendsGenericMethod<A> extends NestedExtendsGenericMethod {
        void method0(A a);
    }

    private interface MassivelyNestedExtendsGenericMethod extends OverlyNestedExtendsGenericMethod<Long>, OverridesGenericMethod {
    }

    static class ReturnsInteger implements Returns<Integer> {
        @Override
        public Integer ret() {
            return null;
        }
    }

    static class Returns2<A> {
        public A ret() {
            return null;
        }
    }

    static class Returns2Double extends Returns2<Double> {
        @Override
        public Double ret() {
            return 1.0;
        }
    }
}
