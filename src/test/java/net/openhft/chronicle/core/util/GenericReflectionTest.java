/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.core.util;

import net.openhft.chronicle.core.CoreTestCommon;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenericReflectionTest extends CoreTestCommon {

    @Test
    void getReturnType() {
        assertEquals(String.class,
                GenericReflection.getReturnType(Returns.class.getMethods()[0], ReturnsString.class), "getReturnType: L20");

        assertEquals(Integer.class,
                GenericReflection.getReturnType(Returns.class.getMethods()[0], ReturnsInteger.class), "getReturnType: L23");

        assertEquals(Double.class,
                GenericReflection.getReturnType(Returns2.class.getMethods()[0], Returns2Double.class), "getReturnType: L26");

        Type t = GenericReflection.getReturnType(ReturnsReturnsAString.class.getMethods()[0], ReturnsReturnsAString.class);
        assertEquals(new TypeOf<Returns<String>>() {
        }.type(), t, "getReturnType: L30");
    }

    @Test
    void getMethodReturnTypes() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Set<Type> methodReturnTypes = GenericReflection.getMethodReturnTypes(ReturnsString.class);
        assertEquals(methodReturnTypes,
                GenericReflection.getMethodReturnTypes(returnString), "getMethodReturnTypes: L39");
    }

    @Test
    void getGenericClassesSuperclassesAndInterfaces() {
        final Type returnString = new TypeOf<Returns<String>>() {
        }.type();
        final Object[] genericInterfaces = GenericReflection.getGenericClassesSuperclassesAndInterfaces(ReturnsString.class).toArray();
        assertArrayEquals(genericInterfaces,
                GenericReflection.getGenericClassesSuperclassesAndInterfaces(returnString).toArray(), "getGenericClassesSuperclassesAndInterfaces: L48");
    }

    @Test
    void getParameterTypes() throws NoSuchMethodException {
        final Method method = GenericMethod.class.getDeclaredMethod("method", Object.class, Object.class);
        final String expected = "[class java.lang.Byte, class java.lang.Short]";
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, ExtendsGenericMethod.class)), "getParameterTypes: L56");
        final Method omethod = OverridesGenericMethod.class.getDeclaredMethod("method", Byte.class, Short.class);
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(omethod, OverridesGenericMethod.class)), "getParameterTypes: L59");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, OverridesGenericMethod.class)), "getParameterTypes: L61");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, NestedExtendsGenericMethod.class)), "getParameterTypes: L63");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, OverlyNestedExtendsGenericMethod.class)), "getParameterTypes: L65");
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, MassivelyNestedExtendsGenericMethod.class)), "getParameterTypes: L67");

        final Method method0 = OverlyNestedExtendsGenericMethod.class.getDeclaredMethod("method0", Object.class);
        assertEquals("[class java.lang.Long]",
                Arrays.toString(GenericReflection.getParameterTypes(method0, MassivelyNestedExtendsGenericMethod.class)), "getParameterTypes: L71");
    }

    @Test
    void getParameterTypesExtends() {
        Method method = null;
        for (Method m : GenericMethodExtends.class.getMethods()) {
            if (m.getName().equals("method")) {
                method = m;
                break;
            }
        }
        assertNotNull(method, "getParameterTypesExtends: L84");
        final String expected = "[" + Number.class + ", " + CharSequence.class + "]";
        assertEquals(expected,
                Arrays.toString(GenericReflection.getParameterTypes(method, GenericMethodExtends.class)), "getParameterTypesExtends: L86");
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
